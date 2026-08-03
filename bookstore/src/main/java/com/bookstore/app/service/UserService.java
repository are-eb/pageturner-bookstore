package com.bookstore.app.service;

import com.bookstore.app.model.Role;
import com.bookstore.app.model.User;
import com.bookstore.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(String name, String email, String rawPassword, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setAddress(address);
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User updateProfile(User user, String name, String address) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Please enter your name.");
        }
        user.setName(name.trim());
        user.setAddress(address == null || address.isBlank() ? null : address.trim());
        return userRepository.save(user);
    }

    public void changePassword(User user, String currentPassword, String newPassword, String confirmPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Your current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Your new password must contain at least 8 characters.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Your new passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public long countAll() {
        return userRepository.count();
    }

    public void toggleEnabled(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setEnabled(!u.isEnabled());
        userRepository.save(u);
    }
}
