package com.shaikh.webStore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private String id;

    private String status;
    private String date;
    private String time;
    private Integer items;

    @Embedded
    private CustomerInfo customer;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderProduct> products;

    private String total;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.status = "PENDING";
    }

}
