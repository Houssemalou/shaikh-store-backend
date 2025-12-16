package com.shaikh.webStore.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String photoPath;
    private String category;
    private Double price;
    private Double originalPrice;
    private Integer stock;
    private String status;
    private Integer discount;
    private Boolean promo ;
    private List<String> images;
}
