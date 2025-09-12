package com.shaikh.webStore.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResDTO {
    private Long id;
    private String name;
    private String description;
    private List<ProductDTO> products;
}
