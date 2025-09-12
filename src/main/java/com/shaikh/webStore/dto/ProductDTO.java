package com.shaikh.webStore.dto;

import com.fasterxml.jackson.databind.node.DoubleNode;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String photoPath;
    private String category;
    private Double price;
    private Double originalPrice;
    private Integer stock;
    private String status;
    private Integer discount;
}
