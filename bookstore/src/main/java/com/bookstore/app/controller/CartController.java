package com.bookstore.app.controller;

import com.bookstore.app.model.User;
import com.bookstore.app.service.CartService;
import com.bookstore.app.service.OrderService;
import com.bookstore.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;

    private User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @GetMapping
    public String viewCart(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("items", cartService.getItems(user));
        model.addAttribute("total", cartService.getTotal(user));
        model.addAttribute("savings", cartService.getSavings(user));
        model.addAttribute("itemCount", cartService.getItemCount(user));
        // Recent order history, shown below the cart itself (Amazon/Flipkart-style),
        // with a link through to the full order history page.
        model.addAttribute("recentOrders", orderService.getOrdersForUser(user).stream().limit(5).toList());
        return "cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long bookId,
                       @RequestParam(defaultValue = "1") int quantity,
                       Authentication auth,
                       RedirectAttributes redirectAttributes) {
        cartService.addToCart(currentUser(auth), bookId, quantity);
        redirectAttributes.addFlashAttribute("success", "Added to cart!");
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/update")
    public String update(@RequestParam Long itemId, @RequestParam int quantity, Authentication auth,
                          RedirectAttributes redirectAttributes) {
        cartService.updateQuantity(currentUser(auth), itemId, quantity);
        redirectAttributes.addFlashAttribute("success", "Cart updated.");
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long itemId, Authentication auth, RedirectAttributes redirectAttributes) {
        cartService.removeItem(currentUser(auth), itemId);
        redirectAttributes.addFlashAttribute("success", "Item removed from cart.");
        return "redirect:/cart";
    }
}
