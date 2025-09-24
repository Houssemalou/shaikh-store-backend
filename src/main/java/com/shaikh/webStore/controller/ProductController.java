package com.shaikh.webStore.controller;


import com.shaikh.webStore.dto.ProductDTO;
import com.shaikh.webStore.records.CategoryDiscountRequest;
import com.shaikh.webStore.records.DiscountRequest;
import com.shaikh.webStore.records.StatusRequest;
import com.shaikh.webStore.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    @Value("${upload.dir}")
    private String uploadDir;
    private final ProductService service;


    @GetMapping
    public List<ProductDTO> list() { return service.listAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> get(@PathVariable Long id) {
        ProductDTO p = service.get(id);
        return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductDTO addProduct(
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            HttpServletRequest request
    ) throws Exception {
        return service.saveProduct(productDTO, photo, request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
        ProductDTO updated = service.update(id, dto);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/discount")
    public ResponseEntity<ProductDTO> discount(@PathVariable Long id, @RequestBody DiscountRequest req) {
        ProductDTO updated = service.applyDiscount(id, req.discount());
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductDTO> statusPatch(@PathVariable Long id, @RequestBody StatusRequest req) {
        ProductDTO updated = service.updateStatus(id, req.status());
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }



    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws Exception {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Détecter automatiquement le type du fichier (jpeg, png, avif, webp, etc.)
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream"; // fallback par défaut
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }



}
