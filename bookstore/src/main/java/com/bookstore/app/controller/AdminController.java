package com.bookstore.app.controller;

import com.bookstore.app.dto.GoogleBookResult;
import com.bookstore.app.model.Book;
import com.bookstore.app.model.Order;
import com.bookstore.app.model.OrderStatus;
import com.bookstore.app.service.BookService;
import com.bookstore.app.service.GoogleBooksService;
import com.bookstore.app.service.OrderService;
import com.bookstore.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;
    private final GoogleBooksService googleBooksService;

    /** Allows optional numeric fields (Book.originalPrice, GoogleBookResult.rating, etc.) to bind cleanly from a blank form field instead of throwing. */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("bookCount", bookService.countAll());
        model.addAttribute("orderCount", orderService.countAll());
        model.addAttribute("userCount", userService.countAll());
        model.addAttribute("revenue", orderService.totalRevenue());
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(8).toList());
        return "admin/dashboard";
    }

    // ---------------- Book management ----------------

    @GetMapping("/books")
    public String manageBooks(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("books", bookService.search(q, null, PageRequest.of(0, 200, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        model.addAttribute("q", q == null ? "" : q);
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        return "admin/book-form";
    }

    @PostMapping("/books/save")
    public String saveBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        if (book.getId() != null) {
            Book existing = bookService.findById(book.getId());
            existing.setTitle(book.getTitle());
            existing.setAuthor(book.getAuthor());
            existing.setDescription(book.getDescription());
            existing.setCategory(book.getCategory());
            existing.setIsbn(book.getIsbn());
            existing.setPublisher(book.getPublisher());
            existing.setPublishedDate(book.getPublishedDate());
            existing.setCoverImageUrl(book.getCoverImageUrl());
            existing.setPrice(book.getPrice());
            existing.setOriginalPrice(book.getOriginalPrice());
            existing.setStockQuantity(book.getStockQuantity());
            bookService.save(existing);
        } else {
            bookService.save(book);
        }
        redirectAttributes.addFlashAttribute("success", "Book saved successfully.");
        return "redirect:/admin/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Book removed.");
        return "redirect:/admin/books";
    }

    // ---------------- Google Books import ----------------

    @GetMapping("/import")
    public String importSearchForm() {
        return "admin/import";
    }

    @GetMapping("/import/search")
    public String searchGoogle(@RequestParam String q, Model model) {
        try {
            model.addAttribute("results", googleBooksService.search(q, 20));
        } catch (GoogleBooksService.GoogleBooksApiException ex) {
            model.addAttribute("results", java.util.List.of());
            model.addAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Unexpected error during Google Books search for '{}': {}", q, ex.getMessage());
            model.addAttribute("results", java.util.List.of());
            model.addAttribute("error", "Something went wrong searching Google Books. Please try again.");
        }
        model.addAttribute("q", q);
        return "admin/import";
    }

    @PostMapping("/import/add")
    public String importBook(@RequestParam String googleBooksId,
                              @RequestParam String title,
                              @RequestParam(required = false) String author,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String isbn,
                              @RequestParam(required = false) String publisher,
                              @RequestParam(required = false) String publishedDate,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String language,
                              @RequestParam(required = false) String coverImageUrl,
                              @RequestParam(required = false) Double rating,
                              @RequestParam(required = false) BigDecimal price,
                              @RequestParam(required = false) Integer stock,
                              @RequestParam(defaultValue = "false") boolean onSale,
                              RedirectAttributes redirectAttributes) {
        // Build the result directly from the data already shown in the search results,
        // instead of re-querying Google Books by ID (that second call was failing
        // whenever the free tier's rate limit was hit between the two requests).
        GoogleBookResult result = new GoogleBookResult();
        result.setGoogleBooksId(googleBooksId);
        result.setTitle(title);
        result.setAuthor(author);
        result.setDescription(description);
        result.setIsbn(isbn);
        result.setPublisher(publisher);
        result.setPublishedDate(publishedDate);
        result.setCategory(category);
        result.setLanguage(language);
        result.setCoverImageUrl(coverImageUrl);
        result.setRating(rating);

        Book saved = bookService.importFromGoogle(result, price, stock);
        if (onSale) {
            BigDecimal finalPrice = saved.getPrice();
            saved.setOriginalPrice(finalPrice.multiply(BigDecimal.valueOf(1.25)).setScale(2, java.math.RoundingMode.HALF_UP));
            bookService.save(saved);
        }
        redirectAttributes.addFlashAttribute("success", "Imported \"" + result.getTitle() + "\" into your catalog.");
        return "redirect:/admin/books";
    }

    // ---------------- Orders ----------------

    @GetMapping("/orders")
    public String manageOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status,
                                     RedirectAttributes redirectAttributes) {
        orderService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Order #" + id + " updated to " + status + ".");
        return "redirect:/admin/orders";
    }

    // ---------------- Users ----------------

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }
}
