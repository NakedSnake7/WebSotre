package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // =========================
    // SINGLE ORDER
    // =========================
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);

    @Query("""
        SELECT o 
        FROM Order o 
        JOIN FETCH o.user 
        JOIN FETCH o.items i 
        JOIN FETCH i.producto 
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithUserAndItems(@Param("id") Long id);

    // =========================
    // LISTS
    // =========================
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT o FROM Order o JOIN FETCH o.user")
    List<Order> findAllWithUser();

    // =========================
    // 🔥 ÓRDENES PENDIENTES (CREATED) PARA EXPIRAR
    // =========================
    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.producto
        WHERE o.orderStatus = com.WeedTitlan.server.model.OrderStatus.CREATED
    """)
    List<Order> findPendingOrdersWithItems();

    // =========================
    // STRIPE
    // =========================
    Optional<Order> findByStripeSessionId(String stripeSessionId);
}