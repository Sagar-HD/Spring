package com.sagar.service;

import com.sagar.dto.DepartmentDto;
import com.sagar.dto.Mappers.DepartmentMapper;
import com.sagar.entities.Department;
import com.sagar.entities.Profile;
import com.sagar.entities.Student;
import com.sagar.repository.MyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private MyRepository repository;
    private DepartmentMapper mapper;

    public DepartmentService(MyRepository repo,DepartmentMapper mapper){
        this.repository=repo;
        this.mapper=mapper;
    }

    @Transactional
    public DepartmentDto addDepartmentWithStudents(DepartmentDto departmentDto){
        Department department= mapper.toEntity(departmentDto);
        //creating student 1
        Student student1=new Student();
        student1.setName("sagar");
        student1.setEmail("sagar@email.com");
        student1.setDepartment(department);
        Profile profile1=new Profile();
        profile1.setBio("life is chill");
        profile1.setHobbies(List.of("coding","drawing"));
        student1.setProfile(profile1);



        //creating student 2
        Student student2=new Student();
        student2.setName("ram");
        student2.setEmail("ram@gmail.com");
        Profile profile2=new Profile();
        profile2.setBio("life is not chill");
        profile2.setHobbies(List.of("not coding","not drawing"));
        student2.setProfile(profile2);
        student2.setDepartment(department);

        department.getStudents().add(student1);
        department.getStudents().add(student2);

        System.out.println("Department ID = " + department.getId());
        department.setId(null);
        //mapper added deault value so setting to null
        System.out.println("Students = " + department.getStudents());

        repository.saveDepartment(department);
        System.out.println("BEFORE RESPONSE MAPPING");

        for (Student s : department.getStudents()) {
            System.out.println(
                    "Student = " + s.getName() +
                            ", Email = " + s.getEmail()
            );
        }

        return mapper.toResponse(department);


    }
}
