package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.converter.HousingUnitMapper;
import com.example.refactored_student_housing.domain.HousingUnit;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.HousingUnitDetailDto;
import com.example.refactored_student_housing.dto.HousingUnitDto;
import com.example.refactored_student_housing.dto.HousingUnitListItemDto;
import com.example.refactored_student_housing.dto.HousingUnitResponseDto;
import com.example.refactored_student_housing.dto.ResidentDto;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.repository.HousingUnitRepository;
import com.example.refactored_student_housing.repository.SearchPatterns;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HousingUnitServiceImpl implements HousingUnitService {

    private final HousingUnitRepository repository;
    private final HousingUnitMapper mapper;

    public HousingUnitServiceImpl(HousingUnitRepository repository, HousingUnitMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HousingUnitListItemDto> findAll() {
        return mapper.toListItems(repository.findAllOrderedByAddress());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HousingUnitListItemDto> search(Integer minCapacity, String addressContains) {
        return mapper.toListItems(
                repository.search(minCapacity, SearchPatterns.contains(addressContains)));
    }

    @Override
    @Transactional(readOnly = true)
    public HousingUnitDetailDto findDetail(Long id, UserDetails currentUser) {
        HousingUnit unit = repository.findDetailById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Housing unit", id));

        boolean admin = isAdmin(currentUser);
        String username = currentUser == null ? null : currentUser.getUsername();

        List<ResidentDto> residents = unit.getLeases().stream()
                .map(lease -> {
                    Student student = lease.getStudent();
                    boolean visible = admin || (username != null
                            && student.getAdvisor() != null
                            && username.equals(student.getAdvisor().getUsername()));
                    return new ResidentDto(
                            student.getId(),
                            visible ? student.getName() : null,
                            lease.getStartDate(), lease.getEndDate(), lease.isActive(), visible);
                })
                .toList();

        return new HousingUnitDetailDto(unit.getId(), unit.getAddress(), unit.getCapacity(), unit.getRent(),
                unit.getUnitType(), unit.getImageUrl(), unit.isFurnished(), unit.getManagerName(), residents);
    }

    @Override
    @Transactional(readOnly = true)
    public HousingUnitDto findFormData(Long id) {
        return repository.findById(id)
                .map(mapper::toFormDto)
                .orElseThrow(() -> ResourceNotFoundException.of("Housing unit", id));
    }

    @Override
    public HousingUnitResponseDto create(HousingUnitDto dto) {
        return mapper.toResponse(repository.save(mapper.toEntity(dto)));
    }

    @Override
    public void update(Long id, HousingUnitDto dto) {
        HousingUnit unit = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Housing unit", id));
        mapper.updateEntityFromDto(dto, unit);
        repository.save(unit);
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.of("Housing unit", id);
        }
        repository.deleteById(id);
    }

    private static boolean isAdmin(UserDetails user) {
        return user != null && user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
