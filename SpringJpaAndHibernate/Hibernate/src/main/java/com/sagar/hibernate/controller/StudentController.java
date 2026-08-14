package com.sagar.hibernate.controller;

import com.sagar.hibernate.dto.StudentDto;
import com.sagar.hibernate.entity.Student;
import com.sagar.hibernate.service.StudentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/")
@RestController
public class StudentController {

    private StudentService service;

    public StudentController(StudentService service){
        this.service=service;
    }

    @GetMapping("/check")
    public String check(){
        return "running..";
    }




    @PostMapping("/contextDemo")
    public String contextDemo(@RequestBody StudentDto studentDto){

        service.saveAndUpdateDemo(studentDto);
        return "student saved";

    }


}
