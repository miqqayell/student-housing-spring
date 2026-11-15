package com.example.refactored_student_housing.web;

import com.example.refactored_student_housing.dto.StudentFilter;
import com.example.refactored_student_housing.dto.StudentFormDto;
import com.example.refactored_student_housing.service.CsvImportService;
import com.example.refactored_student_housing.service.HousingUnitService;
import com.example.refactored_student_housing.service.StudentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Presentation layer for the server-rendered student pages.
 *
 * It only talks to services, receives ready-made DTOs, and contains no domain
 * rules: no entity is created here, no authorization decision is taken here,
 * and no collection is filtered here.
 */
@Controller
@RequestMapping("/students")
public class StudentController {

 private static final Logger log = LoggerFactory.getLogger(StudentController.class);
 private static final double TOP_STUDENT_GPA = 3.5;

 private final StudentService studentService;
 private final HousingUnitService unitService;
 private final CsvImportService csvImportService;

 public StudentController(StudentService studentService,
                          HousingUnitService unitService,
                          CsvImportService csvImportService) {
  this.studentService = studentService;
  this.unitService = unitService;
  this.csvImportService = csvImportService;
 }

 @GetMapping
 public String list(@RequestParam(required = false) String nameContains,
                    @RequestParam(required = false) Integer minYear,
                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bornBefore,
                    @AuthenticationPrincipal UserDetails currentUser,
                    Model model) {
  model.addAttribute("students",
          studentService.findStudents(StudentFilter.of(nameContains, minYear, bornBefore), currentUser));
  // Echo the submitted filter back so the form keeps its values.
  model.addAttribute("nameContains", nameContains);
  model.addAttribute("minYear", minYear);
  model.addAttribute("bornBefore", bornBefore);
  return "students/list";
 }

 @GetMapping("/international")
 public String listInternational(@AuthenticationPrincipal UserDetails currentUser, Model model) {
  model.addAttribute("students", studentService.findStudents(StudentFilter.onlyInternational(), currentUser));
  model.addAttribute("filterTitle", "International Students");
  return "students/list";
 }

 @GetMapping("/top")
 public String listTopStudents(@AuthenticationPrincipal UserDetails currentUser, Model model) {
  model.addAttribute("students", studentService.findStudents(StudentFilter.withMinGpa(TOP_STUDENT_GPA), currentUser));
  model.addAttribute("filterTitle", "Top Students (GPA > " + TOP_STUDENT_GPA + ")");
  return "students/list";
 }

 @GetMapping("/{id}")
 public String detail(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser, Model model) {
  model.addAttribute("student", studentService.findStudentDetail(id, currentUser));
  return "students/detail";
 }

 @GetMapping("/add")
 public String addForm(Model model) {
  model.addAttribute("studentForm", new StudentFormDto());
  model.addAttribute("allUnits", unitService.findAll());
  return "students/add";
 }

 @PostMapping("/add")
 public String addSubmit(@Valid @ModelAttribute("studentForm") StudentFormDto form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails currentUser,
                         Model model) {
  if (bindingResult.hasErrors()) {
   model.addAttribute("allUnits", unitService.findAll());
   return "students/add";
  }

  Long id = studentService.createFromForm(form, currentUser == null ? null : currentUser.getUsername());
  return "redirect:/students/" + id;
 }

 // ---------------------------------------------------------------------
 // Week 12 - admin-only bulk import of students from a CSV file.
 //
 // The parsing + persisting happens on a dedicated thread pool (see
 // CsvImportService#importStudents, which is @Async); this controller returns
 // immediately so the browser never waits for processing.
 // Access is restricted to ROLE_ADMIN in SecurityConfig.
 // ---------------------------------------------------------------------
 @GetMapping("/upload")
 public String uploadForm() {
  return "students/upload";
 }

 @PostMapping("/upload")
 public String uploadSubmit(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
  if (file == null || file.isEmpty()) {
   redirectAttributes.addFlashAttribute("importError", "Please choose a non-empty CSV file.");
   return "redirect:/students/upload";
  }
  try {
   // The MultipartFile is bound to the request, so it is read here on the
   // request thread; only the resulting String is handed to the @Async service.
   String csv = new String(file.getBytes(), StandardCharsets.UTF_8);
   csvImportService.importStudents(csv);
  } catch (IOException ex) {
   log.warn("CSV upload read failure", ex);
   redirectAttributes.addFlashAttribute("importError", "Could not read the uploaded file.");
   return "redirect:/students/upload";
  }
  redirectAttributes.addFlashAttribute("importMessage",
          "Import started in the background. Refresh the students list in a moment to see the new records.");
  return "redirect:/students/upload";
 }
}
