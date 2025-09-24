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
    @Embedded
    private CustomerInfo CustomerInfo;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderProduct> items;

    private String total;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.status = "PENDING";
    }

}
