package com.example.refactored_student_housing.web;

import com.example.refactored_student_housing.dto.HousingUnitDto;
import com.example.refactored_student_housing.dto.LeaseFormDto;
import com.example.refactored_student_housing.dto.StudentFilter;
import com.example.refactored_student_housing.exception.ServiceException;
import com.example.refactored_student_housing.service.HousingUnitService;
import com.example.refactored_student_housing.service.LeaseService;
import com.example.refactored_student_housing.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/units")
public class HousingUnitController {

 private final HousingUnitService unitService;
 private final LeaseService leaseService;
 private final StudentService studentService;

 public HousingUnitController(HousingUnitService unitService,
                              LeaseService leaseService,
                              StudentService studentService) {
  this.unitService = unitService;
  this.leaseService = leaseService;
  this.studentService = studentService;
 }

 @GetMapping
 public String listAll(@RequestParam(required = false) Integer minCapacity,
                       @RequestParam(required = false) String addressContains,
                       Model model) {
  model.addAttribute("units", unitService.search(minCapacity, addressContains));
  model.addAttribute("minCapacity", minCapacity);
  model.addAttribute("addressContains", addressContains);
  return "units/list";
 }

 @GetMapping("/{id}")
 public String detail(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser, Model model) {
  model.addAttribute("unit", unitService.findDetail(id, currentUser));
  return "units/detail";
 }

 @GetMapping("/add")
 public String addForm(Model model) {
  model.addAttribute("housingUnitDto", new HousingUnitDto());
  return "units/add";
 }

 @PostMapping("/add")
 public String addSubmit(@Valid @ModelAttribute("housingUnitDto") HousingUnitDto dto, BindingResult bindingResult) {
  if (bindingResult.hasErrors()) {
   return "units/add";
  }
  Long id = unitService.create(dto).getId();
  return "redirect:/units/" + id;
 }

 @GetMapping("/{id}/edit")
 public String editForm(@PathVariable Long id, Model model) {
  model.addAttribute("housingUnitDto", unitService.findFormData(id));
  model.addAttribute("unitId", id);
  return "units/edit";
 }

 @PostMapping("/{id}/edit")
 public String editSubmit(@PathVariable Long id,
                          @Valid @ModelAttribute("housingUnitDto") HousingUnitDto dto,
                          BindingResult bindingResult,
                          Model model) {
  if (bindingResult.hasErrors()) {
   model.addAttribute("unitId", id);
   return "units/edit";
  }
  unitService.update(id, dto);
  return "redirect:/units/" + id;
 }

 /**
  * Deleting is a state change, so it uses POST (and therefore carries a CSRF
  * token) instead of a plain GET link that any prefetch could trigger.
  */
 @PostMapping("/{id}/delete")
 public String delete(@PathVariable Long id) {
  unitService.deleteById(id);
  return "redirect:/units";
 }

 @GetMapping("/{id}/rent")
 public String rentForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser, Model model) {
  model.addAttribute("lease", new LeaseFormDto());
  return renderRentForm(id, currentUser, model);
 }

 @PostMapping("/{id}/rent")
 public String rentSubmit(@PathVariable Long id,
                          @Valid @ModelAttribute("lease") LeaseFormDto form,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetails currentUser,
                          Model model) {
  if (bindingResult.hasErrors()) {
   return renderRentForm(id, currentUser, model);
  }
  try {
   leaseService.rentUnit(id, form);
  } catch (ServiceException ex) {
   // The rule itself lives in the service; deciding to re-render the form
   // (so the user keeps their input) is a presentation concern.
   model.addAttribute("error", ex.getMessage());
   return renderRentForm(id, currentUser, model);
  }
  return "redirect:/units/" + id;
 }

 private String renderRentForm(Long unitId, UserDetails currentUser, Model model) {
  model.addAttribute("unit", unitService.findDetail(unitId, currentUser));
  model.addAttribute("students", studentService.findStudents(StudentFilter.of(null, null, null), currentUser));
  return "units/rent";
 }
}
