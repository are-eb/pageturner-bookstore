package com.bookstore.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String googleBooksId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String author;

    @Column(length = 4000)
    private String description;

    private String isbn;

    private String publisher;

    private String publishedDate;

    private String category;

    private String language;

    @Column(length = 1000)
    private String coverImageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.valueOf(9.99);

    /** Optional "was" price. When set higher than price, the storefront shows a strikethrough + % off badge. */
    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    private Double rating;

    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /** Rounded percentage discount vs. originalPrice, or null if there is no active discount. */
    public Integer getDiscountPercent() {
        if (originalPrice == null || price == null || originalPrice.compareTo(price) <= 0) {
            return null;
        }
        return originalPrice.subtract(price)
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
