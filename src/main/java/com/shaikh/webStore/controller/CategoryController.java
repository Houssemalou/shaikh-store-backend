package com.shaikh.webStore.controller;


import com.shaikh.webStore.dto.CategoryDiscountRequest;
import com.shaikh.webStore.dto.CategoryReqDTO;
import com.shaikh.webStore.dto.CategoryResDTO;
import com.shaikh.webStore.dto.ProductDTO;
import com.shaikh.webStore.service.CategoryService;
import com.shaikh.webStore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    private final ProductService productService;

    @GetMapping
    public List<CategoryResDTO> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResDTO> get(@PathVariable Long id) {
        CategoryResDTO c = service.get(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CategoryReqDTO dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResDTO> update(@PathVariable Long id, @RequestBody CategoryReqDTO dto) {
        CategoryResDTO updated = service.update(id, dto);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/discount")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductDTO> applyDiscountToCategoryByName(@RequestBody CategoryDiscountRequest request) {
        if (request.categoryName == null || request.categoryName.isEmpty()) {
            throw new IllegalArgumentException("Le nom de la catégorie est requis");
        }
        return productService.applyDiscountToCategoryByName(request.categoryName, request.discount);
    }
}

