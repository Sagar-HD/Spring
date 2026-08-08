package com.sagar.SpringBootCrud.contoller;

import com.sagar.SpringBootCrud.entity.Student;
import com.sagar.SpringBootCrud.service.StudentService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/students")
public class StudentContoller {
    private StudentService studentService;

    public StudentContoller(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/create")
    public ResponseEntity<Student> createStudent(@RequestBody Student student){
        Student createdStudent = studentService.createStudent(student);
        return ok(createdStudent);

    }
    @GetMapping("/get/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id){
        Optional<Student> student = studentService.getStudentById(id);
        if(student.isPresent()) {
            return ok(student.get());
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/getall")
    public ResponseEntity<List<Student>> getAllStudents() {

        Optional<List<Student>> students=studentService.getAllStudents();
        if(students.isPresent()){
            return ResponseEntity.ok(students.get());
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Student> updateStudent(@RequestBody Student student){
        Student updatedStudent = studentService.updateStudent(student);
        return ok(updatedStudent);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id){
        boolean isDeleted = studentService.deleteStudent(id);
        if(isDeleted) {
            return ResponseEntity.ok("student deleted");
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }

    @PatchMapping("/delete-soft")
    public ResponseEntity<String> softDeleteStudent(@Param("id") Long id){
        boolean isDeleted=studentService.softDeleteStudent(id);
        if(isDeleted){
            return ResponseEntity.ok("student soft deleted");
        }
        else{
            return ResponseEntity.notFound().build();
        }


    }
}
