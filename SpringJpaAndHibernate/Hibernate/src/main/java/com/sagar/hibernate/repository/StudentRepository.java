package com.sagar.hibernate.repository;

import com.sagar.hibernate.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Student student){

        em.persist(student);

    }

    public Student getStudent(int id){
        return em.find(Student.class,id);

    }

}
