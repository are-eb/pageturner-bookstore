package com.bookstore.app.service;

import com.bookstore.app.model.Book;
import com.bookstore.app.model.CartItem;
import com.bookstore.app.model.User;
import com.bookstore.app.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookService bookService;

    public List<CartItem> getItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    public void addToCart(User user, Long bookId, int quantity) {
        Book book = bookService.findById(bookId);
        CartItem item = cartItemRepository.findByUserAndBookId(user, bookId)
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setUser(user);
                    ci.setBook(book);
                    ci.setQuantity(0);
                    return ci;
                });
        int newQty = item.getQuantity() + Math.max(quantity, 1);
        item.setQuantity(Math.min(newQty, book.getStockQuantity() > 0 ? book.getStockQuantity() : newQty));
        cartItemRepository.save(item);
    }

    public void updateQuantity(User user, Long itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not your cart item");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    public void removeItem(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Not your cart item");
        }
        cartItemRepository.delete(item);
    }

    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    public BigDecimal getTotal(User user) {
        return getItems(user).stream()
                .map(i -> i.getBook().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount(User user) {
        return getItems(user).stream().mapToInt(CartItem::getQuantity).sum();
    }

    /** Total saved vs. list price, across all items that have an active discount. Used for the cart's price breakdown. */
    public BigDecimal getSavings(User user) {
        return getItems(user).stream()
                .filter(i -> i.getBook().getDiscountPercent() != null)
                .map(i -> i.getBook().getOriginalPrice().subtract(i.getBook().getPrice()).multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
