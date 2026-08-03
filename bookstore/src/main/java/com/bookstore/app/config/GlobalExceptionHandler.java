package com.bookstore.app.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException ex, Model model, HttpServletRequest req) {
        log.warn("Illegal argument on {}: {}", req.getRequestURI(), ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(SecurityException.class)
    public String handleForbidden(SecurityException ex, Model model, HttpServletRequest req) {
        log.warn("Access denied on {}: {}", req.getRequestURI(), ex.getMessage());
        model.addAttribute("message", "You don't have permission to view that.");
        return "error";
    }

    // Defensive fallback: any exception not already handled above still gets a clean
    // error page (when it occurs before the response has started streaming) instead
    // of an unhandled server error, and always gets logged with a full stack trace.
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Model model, HttpServletRequest req) {
        log.error("Unhandled exception on {}", req.getRequestURI(), ex);
        model.addAttribute("message", "Something unexpected happened on our end. Please try again.");
        return "error";
    }
}
