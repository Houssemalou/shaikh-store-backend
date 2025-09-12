package com.shaikh.webStore.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryReqDTO {
    private Long id;
    private String name;
    private String description;
}
