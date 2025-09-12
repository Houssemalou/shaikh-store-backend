package com.shaikh.webStore.controller;


import com.shaikh.webStore.dto.OrderDTO;
import com.shaikh.webStore.records.StatusRequest;
import com.shaikh.webStore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @GetMapping
    public Page<OrderDTO> list(Pageable pageable) {
        return service.listAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> get(@PathVariable String id) {
        OrderDTO o = service.get(id);
        return o == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(o);
    }

    @PostMapping
    public ResponseEntity<OrderDTO> create(@RequestBody OrderDTO dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> status(@PathVariable String id, @RequestBody StatusRequest req) {
        OrderDTO updated = service.updateStatus(id, req.status());
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
