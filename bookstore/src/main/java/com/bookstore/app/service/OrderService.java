package com.bookstore.app.service;

import com.bookstore.app.model.*;
import com.bookstore.app.repository.BookRepository;
import com.bookstore.app.repository.CartItemRepository;
import com.bookstore.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Order placeOrder(User user, String shippingAddress) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Book book = ci.getBook();
            if (book.getStockQuantity() < ci.getQuantity()) {
                throw new IllegalStateException("'" + book.getTitle() + "' has insufficient stock.");
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setBook(book);
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(book.getPrice());
            order.getItems().add(oi);

            total = total.add(book.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));

            book.setStockQuantity(book.getStockQuantity() - ci.getQuantity());
            bookRepository.save(book);
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUser(user);
        return saved;
    }

    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    /** Used by the order detail page: eagerly loads user + items + each item's book. */
    public Order findForDetailView(Long id) {
        return orderRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = findById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public BigDecimal totalRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countAll() {
        return orderRepository.count();
    }
}
