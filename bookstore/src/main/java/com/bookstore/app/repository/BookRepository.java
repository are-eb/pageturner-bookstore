package com.bookstore.app.repository;

import com.bookstore.app.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByGoogleBooksId(String googleBooksId);

    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR :category = '' OR b.category = :category)")
    Page<Book> search(@Param("keyword") String keyword, @Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    java.util.List<String> findAllCategories();

    java.util.List<Book> findByCategoryOrderByIdDesc(String category, Pageable pageable);
}
