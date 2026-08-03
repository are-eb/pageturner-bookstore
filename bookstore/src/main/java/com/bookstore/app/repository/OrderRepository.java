package com.bookstore.app.repository;

import com.bookstore.app.model.Order;
import com.bookstore.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Eagerly fetch the associated user so templates can safely read order.user.name
    // even outside an active Hibernate session (e.g. admin dashboard, order lists).
    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.orderDate DESC")
    List<Order> findAllByOrderByOrderDateDesc();

    // Eagerly fetch user + items + each item's book for the single-order detail view,
    // so it never depends on open-in-view / a still-open session.
    @Query("SELECT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book WHERE o.id = :id")
    Optional<Order> findDetailById(Long id);
}
