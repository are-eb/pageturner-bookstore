package com.bookstore.app.controller;

import com.bookstore.app.model.Order;
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
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    private User currentUser(Authentication auth) {
        return userService.findByEmail(auth.getName());
    }

    @GetMapping("/checkout")
    public String checkoutForm(Authentication auth, Model model) {
        User user = currentUser(auth);
        model.addAttribute("items", cartService.getItems(user));
        model.addAttribute("total", cartService.getTotal(user));
        model.addAttribute("address", user.getAddress());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String shippingAddress,
                              Authentication auth,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        User user = currentUser(auth);
        try {
            Order order = orderService.placeOrder(user, shippingAddress);
            redirectAttributes.addFlashAttribute("success", "Order #" + order.getId() + " placed successfully!");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("items", cartService.getItems(user));
            model.addAttribute("total", cartService.getTotal(user));
            model.addAttribute("address", shippingAddress);
            return "checkout";
        }
    }

    @GetMapping("/orders")
    public String myOrders(Authentication auth, Model model) {
        model.addAttribute("orders", orderService.getOrdersForUser(currentUser(auth)));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Authentication auth, Model model) {
        Order order = orderService.findForDetailView(id);
        User user = currentUser(auth);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!order.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new SecurityException("Not your order");
        }
        model.addAttribute("order", order);
        return "order-detail";
    }
}
