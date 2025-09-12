package com.shaikh.webStore.projection;

public interface OrderProjection {
    String getId();
    String getStatus();
    String getDate();
    String getTime();
    Integer getItems();
    String getTotal();
}
