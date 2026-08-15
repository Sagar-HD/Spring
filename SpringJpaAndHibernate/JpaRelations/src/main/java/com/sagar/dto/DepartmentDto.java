package com.sagar.dto;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentDto {
    private long id;
    private String name;
    private List<StudentSummaryDto> students;

}
