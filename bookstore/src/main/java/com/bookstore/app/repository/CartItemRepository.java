package com.bookstore.app.repository;

import com.bookstore.app.model.CartItem;
import com.bookstore.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Eagerly fetch the book so cart.html / checkout.html can safely read
    // item.book.title, .author, .price, .coverImageUrl, .stockQuantity, etc.
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.book WHERE ci.user = :user")
    List<CartItem> findByUser(@Param("user") User user);

    Optional<CartItem> findByUserAndBookId(User user, Long bookId);

    void deleteByUser(User user);
}
