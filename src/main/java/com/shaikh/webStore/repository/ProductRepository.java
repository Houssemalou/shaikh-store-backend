package com.shaikh.webStore.repository;


import com.shaikh.webStore.model.Category;
import com.shaikh.webStore.model.Product;
import com.shaikh.webStore.projection.ProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    Optional<Product> findByName(String name);
    boolean existsByName(String name);
    @Query("select p.id as id, p.name as name, p.category as category, p.price as price, p.stock as stock, p.status as status from Product p where p.name like %:q%")
    List<ProductProjection> searchProjection(@Param("q") String q);
}
