package com.thesis.demowebapp.controller;

import com.thesis.demowebapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin panel controller.
 *
 * VULNERABILITY-TP [AUTH-002] (see SecurityConfig):
 * This controller's /admin/** path is configured with permitAll()
 * in SecurityConfig, meaning anyone — including unauthenticated
 * users — can view the full list of registered users and their
 * roles.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }
}
