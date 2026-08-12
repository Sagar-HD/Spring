package com.sagar.SpringJdbc.service;

import com.sagar.SpringJdbc.dto.Student;
import com.sagar.SpringJdbc.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private StudentRepository studentRepository;
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void save(Student student) {
        studentRepository.save(student);
    }

    public ResponseEntity<Student> update(Student student) {
        studentRepository.update(student);
        return ResponseEntity.ok(student);
    }

    public ResponseEntity<Student> getStudent(long id) {
        return ResponseEntity.ok(studentRepository.findById(id));
    }

    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    public void delete(long id) {
        studentRepository.delete(id);
    }


}
