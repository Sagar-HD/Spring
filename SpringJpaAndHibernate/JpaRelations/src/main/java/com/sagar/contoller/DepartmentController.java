package com.sagar.contoller;

import com.sagar.dto.DepartmentDto;
import com.sagar.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/departments")
public class DepartmentController {
    private DepartmentService service;
    public DepartmentController(DepartmentService service){
        this.service=service;
    }

    @PostMapping("/withStudents")
    public ResponseEntity<DepartmentDto> addDepartmentWithStudents(@RequestBody DepartmentDto departmentDto){
        return ResponseEntity.ok(service.addDepartmentWithStudents(departmentDto));
    }
}
