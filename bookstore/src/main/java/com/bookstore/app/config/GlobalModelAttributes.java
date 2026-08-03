package com.bookstore.app.config;

import com.bookstore.app.service.CartService;
import com.bookstore.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes the current user's cart item count available to every view as "cartItemCount",
 * so the navbar badge (fragments :: navbar) can render it on any page without every
 * controller needing to remember to add it manually.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final CartService cartService;
    private final UserService userService;

    @ModelAttribute("cartItemCount")
    public int cartItemCount(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0;
        }
        try {
            return cartService.getItemCount(userService.findByEmail(auth.getName()));
        } catch (Exception ex) {
            // Never let a badge calculation break page rendering.
            return 0;
        }
    }
}
