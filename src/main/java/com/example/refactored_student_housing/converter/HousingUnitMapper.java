package com.example.refactored_student_housing.converter;

import com.example.refactored_student_housing.domain.HousingUnit;
import com.example.refactored_student_housing.dto.HousingUnitDto;
import com.example.refactored_student_housing.dto.HousingUnitListItemDto;
import com.example.refactored_student_housing.dto.HousingUnitResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapping for housing units. This replaces the property-by-property
 * copying that used to sit inside the controllers.
 */
@Mapper(componentModel = "spring")
public interface HousingUnitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leases", ignore = true)
    HousingUnit toEntity(HousingUnitDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "leases", ignore = true)
    void updateEntityFromDto(HousingUnitDto dto, @MappingTarget HousingUnit entity);

    HousingUnitResponseDto toResponse(HousingUnit entity);

    HousingUnitListItemDto toListItem(HousingUnit entity);

    List<HousingUnitListItemDto> toListItems(List<HousingUnit> entities);

    /** Entity -> form backing object, used to pre-fill the edit form. */
    HousingUnitDto toFormDto(HousingUnit entity);
}
