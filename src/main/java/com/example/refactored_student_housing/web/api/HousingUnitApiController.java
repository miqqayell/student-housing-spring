package com.example.refactored_student_housing.web.api;

import com.example.refactored_student_housing.dto.HousingUnitDto;
import com.example.refactored_student_housing.dto.HousingUnitResponseDto;
import com.example.refactored_student_housing.service.HousingUnitService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Web API for housing units (week 10).
 *
 * Provides the "Add" capability that did not previously exist over REST
 * (housing units could only be created through the server-rendered MVC form).
 * It is consumed by the separate Client project.
 */
@RestController
@RequestMapping("/api/units")
public class HousingUnitApiController {

    private final HousingUnitService unitService;

    public HousingUnitApiController(HousingUnitService unitService) {
        this.unitService = unitService;
    }

    /**
     * POST /api/units -> 201 Created, with a Location header for the new unit.
     *
     * Security note: this endpoint is configured as permitAll and has CSRF
     * disabled (see SecurityConfig) so the separate Client project can call it
     * without a login session or CSRF token.
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<HousingUnitResponseDto> createUnit(@Valid @RequestBody HousingUnitDto dto) {
        HousingUnitResponseDto created = unitService.create(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
