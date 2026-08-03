package com.bookstore.app.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Simplified, flattened representation of a Google Books API volume,
 * ready to be shown in the admin "import from Google" screen or
 * converted straight into a Book entity.
 */
@Data
public class GoogleBookResult {
    private String googleBooksId;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private String publisher;
    private String publishedDate;
    private String category;
    private String language;
    private String coverImageUrl;
    private Double rating;
    private BigDecimal suggestedPrice;
}
