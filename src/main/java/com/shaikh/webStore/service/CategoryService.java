package com.shaikh.webStore.service;


import com.shaikh.webStore.dto.CategoryReqDTO;
import com.shaikh.webStore.dto.CategoryResDTO;
import com.shaikh.webStore.model.Category;
import com.shaikh.webStore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repo;
    private final ProductService productService;

    public List<CategoryResDTO> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public CategoryResDTO get(Long id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public String create(CategoryReqDTO dto) {
        Category c = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        repo.save(c);
        return "catégorie créer avec succès";
    }

    public CategoryResDTO update(Long id, CategoryReqDTO dto) {
        return repo.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setDescription(dto.getDescription());
            return toDto(repo.save(existing));
        }).orElse(null);
    }

    public void delete(Long id) { repo.deleteById(id); }

    private CategoryResDTO toDto(Category c) {
        return CategoryResDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .products(!c.getFoodItems().isEmpty() ? c.getFoodItems().stream().map(productService::toDto).toList() : List.of())
                .build();
    }
}
