package com.shaikh.webStore.controller;


import com.shaikh.webStore.dto.CategoryReqDTO;
import com.shaikh.webStore.dto.CategoryResDTO;
import com.shaikh.webStore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @GetMapping
    public List<CategoryResDTO> list() { return service.listAll(); }

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
}
