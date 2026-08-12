package com.sagar.SpringJdbc.controller;

import com.sagar.SpringJdbc.dto.Student;
import com.sagar.SpringJdbc.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private StudentService studentService;
    public StudentController( StudentService studentService) {
        this.studentService = studentService;
    }
    @PostMapping
    public ResponseEntity<String> save(@Valid @RequestBody Student student) {
        studentService.save(student);
        return ResponseEntity.ok("Student saved successfully");
    }

    @PutMapping
    public ResponseEntity<String> update(@Valid @RequestBody Student student) {
        studentService.update(student);
        return ResponseEntity.ok("Student updated successfully");
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable long id) {
        return studentService.getStudent(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id) {
        studentService.delete(id);
        return ResponseEntity.ok("Student deleted successfully");
    }






}
