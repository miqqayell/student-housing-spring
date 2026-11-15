package com.example.refactored_student_housing.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavController {

 @GetMapping("/")
 public String index() {
  return "redirect:/home";
 }

 @GetMapping("/home")
 public String home() {
  return "home";
 }

 @GetMapping("/campus-life")
 public String campusLife() {
  return "campus_life";
 }

 @GetMapping("/login")
 public String login() {
  return "login";
 }
}