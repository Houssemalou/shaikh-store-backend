package com.shaikh.webStore.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductDTO {
    private String productName;
    private Integer quantity;
    private Double price;
}
