package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.dto.HousingUnitDetailDto;
import com.example.refactored_student_housing.dto.HousingUnitDto;
import com.example.refactored_student_housing.dto.HousingUnitListItemDto;
import com.example.refactored_student_housing.dto.HousingUnitResponseDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface HousingUnitService {

    List<HousingUnitListItemDto> findAll();

    /** Filtered listing; both parameters are optional and applied in the database. */
    List<HousingUnitListItemDto> search(Integer minCapacity, String addressContains);

    /** Detail page data including the residents, in a single query. */
    HousingUnitDetailDto findDetail(Long id, UserDetails currentUser);

    /** Pre-fills the edit form. */
    HousingUnitDto findFormData(Long id);

    HousingUnitResponseDto create(HousingUnitDto dto);

    void update(Long id, HousingUnitDto dto);

    void deleteById(Long id);
}
