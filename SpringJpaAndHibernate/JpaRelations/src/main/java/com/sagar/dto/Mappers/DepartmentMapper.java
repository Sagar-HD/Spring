package com.sagar.dto.Mappers;

import com.sagar.dto.DepartmentDto;
import com.sagar.dto.DepartmentSummaryDto;
import com.sagar.entities.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" ,uses = StudentMapper.class)
public interface DepartmentMapper {



        DepartmentSummaryDto toSummary(Department department);
        @Mapping(target = "students", source = "students")
        DepartmentDto toResponse(Department department);

        @Mapping(target = "students", ignore = true)
        Department toEntity(DepartmentDto dto);

}
