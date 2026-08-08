package com.sagar.SpringBootCrud.service;

import com.sagar.SpringBootCrud.entity.Student;
import com.sagar.SpringBootCrud.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class StudentService {

    private StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        student.setDeleted(false);
        return studentRepository.save(student);

    }

    public Optional<Student> getStudentById(Long id) {

        return studentRepository.findByIdAndIsDeletedFalse(id);
    }
    public Optional<List<Student>> getAllStudents() {
        return Optional.of(studentRepository.findByIsDeletedFalse());
    }
    public Student updateStudent(Student student) {
        student.setDeleted(false);
        return studentRepository.save(student);
    }
    public boolean deleteStudent(Long id) {
         boolean isPresent=studentRepository.existsById(id);
         if(isPresent) {
             studentRepository.deleteById(id);
             return true;
         }
         return false;
    }
    public boolean softDeleteStudent(Long id) {
        Optional<Student> studentToDelete=studentRepository.findByIdAndIsDeletedFalse(id);
        if(studentToDelete.isPresent()) {

            studentToDelete.get().setDeleted(true);

            studentRepository.save(studentToDelete.get());

            return true;
        }
        return false;
    }
}
