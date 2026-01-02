package com.shaikh.webStore.controller;


import com.shaikh.webStore.dto.ProductDTO;
import com.shaikh.webStore.records.DiscountRequest;
import com.shaikh.webStore.records.StatusRequest;
import com.shaikh.webStore.service.MinioService;
import com.shaikh.webStore.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final MinioService minioService;


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
            @RequestPart(value = "photos", required = false) MultipartFile[] photos,
            HttpServletRequest request
    ) throws IOException {
        return service.saveProduct(productDTO, photos, request);
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
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try (InputStream inputStream = minioService.downloadFile(filename)) {
            ByteArrayResource resource = new ByteArrayResource(inputStream.readAllBytes());

            // Détecter automatiquement le type du fichier (jpeg, png, avif, webp, etc.)
            String contentType = Files.probeContentType(Paths.get(filename));
            if (contentType == null) {
                contentType = "application/octet-stream"; // fallback par défaut
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductDTO> addImages(
            @PathVariable Long id,
            @RequestPart("photos") MultipartFile[] photos,
            HttpServletRequest request
    ) {
        try {
            ProductDTO updated = service.addImagesToProduct(id, photos, request);
            return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{productId}/images/{filename:.+}")
    public ResponseEntity<ProductDTO> deleteImage(@PathVariable Long productId, @PathVariable String filename) {
        try {
            ProductDTO updated = service.deleteImageFromProductByFilename(productId, filename);
            return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
