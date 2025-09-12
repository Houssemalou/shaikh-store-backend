package com.shaikh.webStore.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProduct {
    private String name;
    private Integer quantity;
    private String price;
}
