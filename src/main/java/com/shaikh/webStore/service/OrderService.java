package com.shaikh.webStore.service;


import com.shaikh.webStore.dto.OrderDTO;
import com.shaikh.webStore.model.Order;
import com.shaikh.webStore.model.OrderProduct;
import com.shaikh.webStore.model.Product;
import com.shaikh.webStore.repository.OrderRepository;
import com.shaikh.webStore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repo;
    private final ProductRepository productRepo;

    public Page<OrderDTO> listAll(Pageable pageable) {
        return repo.findAll(pageable)
                .map(this::toDto);
    }

    public OrderDTO get(String id) {
        return repo.findById(id).map(this::toDto).orElse(null);
    }

    public OrderDTO create(OrderDTO dto) {
        updateProductStock(dto.getItems());

        // reconstruire les items avec le prix depuis la BDD
        List<OrderProduct> items = dto.getItems().stream().map(i -> {
            // récupération du produit depuis la BDD par son nom (ou par ID si dispo)
            Product product =  productRepo.findByName(i.getProductName())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable: " + i.getProductName()));

            return OrderProduct.builder()
                    .productName(product.getName())
                    .quantity(i.getQuantity())
                    .price(product.getPrice())   // ⚡ prix récupéré depuis la BDD
                    .build();
        }).collect(Collectors.toList());

        Order o = Order.builder()
                .id(generateId())
                .items(items)
                .CustomerInfo(dto.getCustomerInfo())
                .total(dto.getTotal())
                .build();

        return toDto(repo.save(o));
    }

    public OrderDTO updateStatus(String id, String status) {
        return repo.findById(id).map(o -> {
            o.setStatus(status);
            return toDto(repo.save(o));
        }).orElse(null);
    }

    public void delete(String id) { repo.deleteById(id); }

    private String generateId() {
        return "ORD-" + System.currentTimeMillis();
    }

    private OrderDTO toDto(Order o) {
        return OrderDTO.builder()
                .orderId(o.getId())
                .status(o.getStatus())
                .date(o.getDate())
                .time(o.getTime())
                .items(o.getItems())
                .customerInfo(o.getCustomerInfo())
                .items(o.getItems())
                .total(o.getTotal())
                .build();
    }

    private void updateProductStock(List<OrderProduct> orderProducts) {
        for (OrderProduct op : orderProducts) {
            productRepo.findByName(op.getProductName()).ifPresent(product -> {
                int newStock = product.getStock() - op.getQuantity();
                product.setStock(Math.max(newStock, 0));
                if (product.getStock() == 0) {
                    product.setStatus("rupture du stock");
                } else if (product.getStock() < 8) {
                    product.setStatus("stock faible");
                } else {
                    product.setStatus("disponible");
                }

                productRepo.save(product);
            });
        }
    }
}
