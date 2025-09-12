package com.shaikh.webStore.projection;

public interface ProductProjection {
    Long getId();
    String getName();
    String getCategory();
    String getPrice();
    Integer getStock();
    String getStatus();
}
