package com.shaikh.webStore.service;

import com.shaikh.webStore.dto.ProductDTO;
import com.shaikh.webStore.model.Category;
import com.shaikh.webStore.model.Product;
import com.shaikh.webStore.repository.CategoryRepository;
import com.shaikh.webStore.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${upload.dir}")
    private String uploadDir;
    private final CategoryRepository categoryRepository;
    private final ProductRepository repo;



    public List<ProductDTO> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductDTO get(Long id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public ProductDTO saveProduct(ProductDTO dto, MultipartFile photo, HttpServletRequest request) throws IOException {
        String photoPath = null;

        if (photo != null && !photo.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.createDirectories(filePath.getParent());
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String baseUrl = String.format("%s://%s:%d",
                    request.getScheme(),    // http ou https
                    request.getServerName(), // localhost ou domaine
                    request.getServerPort()  // port du backend (ex: 8080)
            );

            photoPath = baseUrl + request.getContextPath() + "/products/images/" + fileName;

        }
        Category category = categoryRepository.findByName(dto.getCategory())
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable avec name=" + dto.getCategory()));
        dto.setPrice(dto.getOriginalPrice());
        Product product = toEntity(dto, photoPath);
        product.setCategory(category);
        product.setPromo(dto.getPromo());
        recallPrice(product);
        Product saved = repo.save(product);
        return toDto(saved);
    }

    public ProductDTO update(Long id, ProductDTO dto) {
        return repo.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setOriginalPrice(dto.getOriginalPrice());
            existing.setStock(dto.getStock());
            existing.setStatus(dto.getStatus());
            existing.setDiscount(dto.getDiscount());
            existing.setPromo(dto.getPromo());

            // Mise à jour de la catégorie par nom
            if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
                Category category = categoryRepository.findByName(dto.getCategory())
                        .orElseThrow(() -> new RuntimeException("Category not found with name: " + dto.getCategory()));
                existing.setCategory(category);
            }

            recallPrice(existing);

            return toDto(repo.save(existing));
        }).orElse(null);
    }


    public void delete(Long id) {
        repo.deleteById(id);
    }

    public ProductDTO applyDiscount(Long id, Integer discount) {
        return repo.findById(id).map(p -> {
            p.setDiscount(discount);
            recallPrice(p);
            return toDto(repo.save(p));
        }).orElse(null);
    }

    public ProductDTO updateStatus(Long id, String status) {
        return repo.findById(id).map(p -> {
            p.setStatus(status);
            return toDto(repo.save(p));
        }).orElse(null);
    }

    public List<ProductDTO> applyDiscountToCategoryByName(String categoryName, Integer discount) {
        if (discount == null || discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Le pourcentage de remise doit être compris entre 0 et 100");
        }

        // Recherche de la catégorie par nom
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new EntityNotFoundException("Category introuvable avec name=" + categoryName));

        // Récupération des produits
        List<Product> products = repo.findByCategory(category);

        // Appliquer le discount et recalculer le prix
        products.forEach(p -> {
            p.setDiscount(discount);
            recallPrice(p); // recalcul du prix après remise
        });

        // Sauvegarder les produits
        List<Product> savedProducts = repo.saveAll(products);

        // Retourner les DTO
        return savedProducts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProductDTO toDto(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .photoPath(p.getPhotoPath())
                .category(p.getCategory().getName())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .stock(p.getStock())
                .status(p.getStatus())
                .promo(p.getPromo())
                .discount(p.getDiscount())
                .build();
    }

    public Product toEntity(ProductDTO dto, String photoPath) {
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .category(categoryRepository.findByName(dto.getCategory())
                        .orElseThrow(() -> new RuntimeException("Catégorie introuvable avec id=" + dto.getCategory())))
                .price(dto.getPrice())
                .originalPrice(dto.getOriginalPrice())
                .stock(dto.getStock())
                .status(dto.getStatus())
                .discount(dto.getDiscount())
                .photoPath(photoPath)
                .build();
    }

    private void recallPrice(Product product) {
        try {
            if (product.getOriginalPrice() != null) {
                BigDecimal original = new BigDecimal(product.getOriginalPrice());
                Integer discount = product.getDiscount();

                BigDecimal finalPrice = original;

                if (discount != null && discount > 0) {
                    BigDecimal reduction = original
                            .multiply(BigDecimal.valueOf(discount))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    finalPrice = original.subtract(reduction);
                }

                product.setPrice(Double.valueOf(finalPrice.toString()));
            }
        } catch (NumberFormatException e) {
            product.setPrice(product.getOriginalPrice());
        }
    }
}
