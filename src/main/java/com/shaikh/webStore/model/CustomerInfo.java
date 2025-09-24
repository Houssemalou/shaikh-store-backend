package com.shaikh.webStore.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInfo {
    private String name;
    private String phone;
    private String address;

}
