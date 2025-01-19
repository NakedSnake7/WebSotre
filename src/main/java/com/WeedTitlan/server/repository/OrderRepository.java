package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.Order;  
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {
    
	@Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
	Optional<Order> findByIdWithUser(@Param("id") Long id);

    // Buscar órdenes por estado
    List<Order> findByStatus(String status);
}
