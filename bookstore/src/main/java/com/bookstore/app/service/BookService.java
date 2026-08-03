package com.bookstore.app.service;

import com.bookstore.app.dto.GoogleBookResult;
import com.bookstore.app.model.Book;
import com.bookstore.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Page<Book> search(String keyword, String category, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String cat = (category == null || category.isBlank()) ? null : category.trim();
        return bookRepository.search(kw, cat, pageable);
    }

    public List<String> allCategories() {
        return bookRepository.findAllCategories();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    public boolean existsByGoogleId(String googleBooksId) {
        return bookRepository.findByGoogleBooksId(googleBooksId).isPresent();
    }

    /** Converts a Google Books search result into a persisted local Book (used by admin import). */
    public Book importFromGoogle(GoogleBookResult g, BigDecimal price, Integer stock) {
        return bookRepository.findByGoogleBooksId(g.getGoogleBooksId()).orElseGet(() -> {
            Book book = new Book();
            book.setGoogleBooksId(g.getGoogleBooksId());
            book.setTitle(g.getTitle() != null ? g.getTitle() : "Untitled");
            book.setAuthor(g.getAuthor());
            book.setDescription(g.getDescription());
            book.setIsbn(g.getIsbn());
            book.setPublisher(g.getPublisher());
            book.setPublishedDate(g.getPublishedDate());
            book.setCategory(g.getCategory());
            book.setLanguage(g.getLanguage());
            book.setCoverImageUrl(g.getCoverImageUrl());
            book.setRating(g.getRating());
            book.setPrice(price != null ? price : g.getSuggestedPrice());
            book.setStockQuantity(stock != null ? stock : 10);
            return bookRepository.save(book);
        });
    }

    public long countAll() {
        return bookRepository.count();
    }

    /**
     * Builds up to {@code maxCategories} curated shelves (category -> up to {@code perShelf} books),
     * used to power the homepage's tabbed "Bestsellers by Category" section.
     */
    public Map<String, List<Book>> shelvesByCategory(int maxCategories, int perShelf) {
        Map<String, List<Book>> shelves = new LinkedHashMap<>();
        List<String> categories = allCategories();
        for (String category : categories) {
            if (shelves.size() >= maxCategories) break;
            List<Book> books = bookRepository.findByCategoryOrderByIdDesc(category, PageRequest.of(0, perShelf));
            if (!books.isEmpty()) {
                shelves.put(category, books);
            }
        }
        return shelves;
    }
}
