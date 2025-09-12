package com.shaikh.webStore.repository;



import com.shaikh.webStore.model.Order;
import com.shaikh.webStore.projection.OrderProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<OrderProjection> findAllBy();
}
