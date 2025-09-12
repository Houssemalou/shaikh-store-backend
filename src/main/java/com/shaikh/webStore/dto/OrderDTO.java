package com.shaikh.webStore.dto;


import com.shaikh.webStore.model.CustomerInfo;
import com.shaikh.webStore.model.OrderProduct;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private String id;
    private String status;
    private String date;
    private String time;
    private Integer items;
    private CustomerInfo customer;
    private List<OrderProduct> products;
    private String total;

}
