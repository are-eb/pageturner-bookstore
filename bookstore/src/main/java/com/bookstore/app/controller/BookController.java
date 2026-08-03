package com.bookstore.app.controller;

import com.bookstore.app.model.Book;
import com.bookstore.app.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public String listBooks(@RequestParam(required = false) String q,
                             @RequestParam(required = false) String category,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             Model model) {
        Page<Book> books = bookService.search(q, category, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("books", books);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("category", category == null ? "" : category);
        model.addAttribute("categories", bookService.allCategories());
        return "books";
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        model.addAttribute("book", book);
        return "book-detail";
    }
}
