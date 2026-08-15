package com.sagar.repository;

import com.sagar.entities.Department;
import com.sagar.entities.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MyRepository {
    @PersistenceContext
    private EntityManager em;

    public void saveDepartment(Department department){
        em.persist(department);
    }

    public  void saveStudent(Student student){
        em.persist(student);
    }

}
