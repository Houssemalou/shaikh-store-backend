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
    private String orderId;
    private String status;
    private String date;
    private String time;
    private CustomerInfo customerInfo;
    private List<OrderProduct> items;
    private String total;

}
