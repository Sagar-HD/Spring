package com.sagar.hibernate.service;

import com.sagar.hibernate.dto.StudentDto;
import com.sagar.hibernate.entity.Student;
import com.sagar.hibernate.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class StudentService {
    private StudentRepository repo;

    public StudentService(StudentRepository repo){
        this.repo=repo;
    }

    @Transactional
    public void saveAndUpdateDemo(StudentDto studentDto) {

        //create object

        Student student = new Student();

        student.setName(studentDto.getName());
        student.setFeesPaid(studentDto.getFeesPaid());
        student.setCreatedAt(LocalDateTime.now());

        System.out.println("Before save: " + student);

        repo.save(student);

        System.out.println("After save: " + student);

        student.setName("changed name");

        System.out.println("Modified managed entity");

        Student sameStudent =
                repo.getStudent(student.getId());

        System.out.println(
                "Same object? " +
                        (student == sameStudent)
        );


        sameStudent.setName(studentDto.getName());

        sameStudent.setCreatedAt(LocalDateTime.now());

       //even though in between i changed name it finally saves last state
        //only date gets updated by update query
        //it dirty checks before commit and  flushes to db
    }


    @Transactional
    public void multiOperation(){

    }




}
