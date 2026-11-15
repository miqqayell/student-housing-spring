package com.example.refactored_student_housing.converter;

import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.StudentCreateDto;
import com.example.refactored_student_housing.dto.StudentDto;
import com.example.refactored_student_housing.dto.StudentSearchDto;
import com.example.refactored_student_housing.dto.StudentUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapping for the Web API. Trivial field-for-field mappings are
 * generated; the two non-trivial ones (counting the leases without exposing the
 * association, and a partial PATCH update) are expressed explicitly.
 */
@Mapper(componentModel = "spring")
public interface StudentApiMapper {

    /** POST body -> new entity. Identity and associations are never bound from the request. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gpa", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "international", ignore = true)
    @Mapping(target = "profileImageUrl", ignore = true)
    @Mapping(target = "leases", ignore = true)
    @Mapping(target = "advisor", ignore = true)
    Student toEntity(StudentCreateDto dto);

    /**
     * Entity -> API read model. Custom logic: the lease association is collapsed
     * into a count so the response never serializes (or lazily triggers) it.
     */
    @Mapping(target = "leaseCount", source = "leases", qualifiedByName = "leaseCount")
    StudentDto toDto(Student entity);

    StudentSearchDto toSearchDto(Student entity);

    List<StudentSearchDto> toSearchDtos(List<Student> entities);

    /**
     * PATCH: copy only the properties actually present in the request body.
     * Everything the update DTO does not carry is deliberately left untouched,
     * which is exactly what the unmapped-target policy expresses here.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
                 unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leases", ignore = true)
    @Mapping(target = "advisor", ignore = true)
    void updateEntityFromDto(StudentUpdateDto dto, @MappingTarget Student entity);

    @Named("leaseCount")
    static int leaseCount(List<?> leases) {
        return leases == null ? 0 : leases.size();
    }
}
