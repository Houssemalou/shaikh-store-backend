package com.shaikh.webStore.model;

import com.shaikh.webStore.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProduct {
    private String productName;
    private Integer quantity;
    private Double price;

}
