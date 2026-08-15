package com.sagar.dto.Mappers;

import com.sagar.dto.StudentDto;
import com.sagar.dto.StudentSummaryDto;
import com.sagar.entities.Student;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDto toResponse(Student student);

    StudentSummaryDto toSummary(Student student);

    Student toEntity(StudentDto studentDto);
}