package com.sagar.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentDto {
    private long id;
    private String name;
    private List<DepartmentSummaryDto> departments;

}
