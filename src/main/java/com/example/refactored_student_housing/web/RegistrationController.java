package com.example.refactored_student_housing.web;

import com.example.refactored_student_housing.dto.UserRegistrationDto;
import com.example.refactored_student_housing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto, BindingResult result) {
        // Uniqueness is a business rule owned by the service; turning it into a
        // field error on the form is the controller's job.
        if (userService.isUsernameTaken(userDto.getUsername())) {
            result.rejectValue("username", "error.userDto", "That username is already taken.");
        }
        if (result.hasErrors()) {
            return "register";
        }

        userService.register(userDto);
        return "redirect:/login?registered";
    }
}
