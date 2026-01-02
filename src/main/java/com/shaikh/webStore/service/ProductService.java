package com.shaikh.webStore.service;

import com.shaikh.webStore.dto.ProductDTO;
import com.shaikh.webStore.model.Category;
import com.shaikh.webStore.model.Product;
import com.shaikh.webStore.model.ProductImage;
import com.shaikh.webStore.repository.CategoryRepository;
import com.shaikh.webStore.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository repo;
    private final MinioService minioService;

    public List<ProductDTO> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProductDTO get(Long id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public ProductDTO saveProduct(ProductDTO dto, MultipartFile[] photos, HttpServletRequest request) throws IOException {
        List<String> imagePaths = new ArrayList<>();

        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    try {
                        String objectName = minioService.uploadFile(photo);

                        String baseUrl = String.format("%s://%s:%d",
                                request.getScheme(),
                                request.getServerName(),
                                request.getServerPort()
                        );

                        String imageUrl = baseUrl + request.getContextPath() + "/products/images/" + objectName;
                        imagePaths.add(imageUrl);
                    } catch (Exception e) {
                        throw new IOException("Erreur lors de l'upload vers MinIO", e);
                    }
                }
            }
        }

        Category category = categoryRepository.findByName(dto.getCategory())
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable avec name=" + dto.getCategory()));

        dto.setPrice(dto.getOriginalPrice());

        String mainPhotoPath = imagePaths.isEmpty() ? null : imagePaths.get(0);
        Product product = toEntity(dto, mainPhotoPath);
        product.setCategory(category);
        product.setPromo(dto.getPromo());
        recallPrice(product);

        Product saved = repo.save(product);

        for (String imagePath : imagePaths) {
            ProductImage productImage = ProductImage.builder()
                    .imagePath(imagePath)
                    .product(saved)
                    .build();
            saved.getImages().add(productImage);
        }

        saved = repo.save(saved);
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

            if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
                Category category = categoryRepository.findByName(dto.getCategory())
                        .orElseThrow(() -> new RuntimeException("Category not found with name: " + dto.getCategory()));
                existing.setCategory(category);
            }

            recallPrice(existing);

            return toDto(repo.save(existing));
        }).orElse(null);
    }

    public ProductDTO addImagesToProduct(Long id, MultipartFile[] photos, HttpServletRequest request) throws IOException {
        return repo.findById(id).map(product -> {
            try {
                if (photos != null) {
                    for (MultipartFile photo : photos) {
                        if (!photo.isEmpty()) {
                            try {
                                String objectName = minioService.uploadFile(photo);

                                String baseUrl = String.format("%s://%s:%d",
                                        request.getScheme(),
                                        request.getServerName(),
                                        request.getServerPort()
                                );

                                String imageUrl = baseUrl + request.getContextPath() + "/products/images/" + objectName;

                                ProductImage productImage = ProductImage.builder()
                                        .imagePath(imageUrl)
                                        .product(product)
                                        .build();
                                product.getImages().add(productImage);
                            } catch (Exception e) {
                                throw new IOException("Erreur lors de l'upload vers MinIO", e);
                            }
                        }
                    }
                }

                // Si photoPath est null et qu'on a des images, définir la première comme photoPath
                if ((product.getPhotoPath() == null || product.getPhotoPath().isEmpty()) && !product.getImages().isEmpty()) {
                    product.setPhotoPath(product.getImages().get(0).getImagePath());
                }

                return toDto(repo.save(product));
            } catch (IOException e) {
                throw new RuntimeException("Erreur lors de l'upload des images", e);
            }
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

        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new EntityNotFoundException("Category introuvable avec name=" + categoryName));

        List<Product> products = repo.findByCategory(category);

        products.forEach(p -> {
            p.setDiscount(discount);
            recallPrice(p);
        });

        List<Product> savedProducts = repo.saveAll(products);

        return savedProducts.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProductDTO toDto(Product p) {
        List<String> imageUrls = p.getImages().stream()
                .map(ProductImage::getImagePath)
                .collect(Collectors.toList());

        // Si photoPath est null ou vide, utiliser la première image de la liste (compatibilité avec le scraper)
        String resolvedPhotoPath = p.getPhotoPath();
        if ((resolvedPhotoPath == null || resolvedPhotoPath.isEmpty()) && !imageUrls.isEmpty()) {
            resolvedPhotoPath = imageUrls.get(0);
        }

        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .photoPath(resolvedPhotoPath)
                .category(p.getCategory() != null ? p.getCategory().getName() : null)
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .stock(p.getStock())
                .status(p.getStatus())
                .promo(p.getPromo())
                .discount(p.getDiscount())
                .images(imageUrls)
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
                BigDecimal original = BigDecimal.valueOf(product.getOriginalPrice());
                Integer discount = product.getDiscount();

                BigDecimal finalPrice = original;

                if (discount != null && discount > 0) {
                    BigDecimal reduction = original
                            .multiply(BigDecimal.valueOf(discount))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    finalPrice = original.subtract(reduction);
                }

                product.setPrice(finalPrice.doubleValue());
            }
        } catch (NumberFormatException e) {
            product.setPrice(product.getOriginalPrice());
        }
    }

    public ProductDTO deleteImageFromProductByFilename(Long productId, String filename) {
        return repo.findById(productId).map(product -> {
            String imageUrlSuffix = "/products/images/" + filename;
            ProductImage imageToDelete = product.getImages().stream()
                    .filter(img -> img.getImagePath().endsWith(imageUrlSuffix))
                    .findFirst()
                    .orElse(null);

            if (imageToDelete != null) {
                // Extract objectName, which is filename
                String objectName = filename;

                try {
                    minioService.deleteFile(objectName);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de la suppression de l'image dans MinIO", e);
                }

                product.getImages().remove(imageToDelete);

                // If the deleted image was the main photo, set a new one
                if (product.getPhotoPath() != null && product.getPhotoPath().endsWith(imageUrlSuffix)) {
                    if (!product.getImages().isEmpty()) {
                        product.setPhotoPath(product.getImages().get(0).getImagePath());
                    } else {
                        product.setPhotoPath(null);
                    }
                }

                return toDto(repo.save(product));
            } else {
                throw new RuntimeException("Image not found");
            }
        }).orElse(null);
    }
}

