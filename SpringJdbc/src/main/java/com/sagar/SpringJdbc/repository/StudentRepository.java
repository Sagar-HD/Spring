package com.sagar.SpringJdbc.repository;

import com.sagar.SpringJdbc.dto.Student;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
public class StudentRepository {

    private JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        System.out.println("JdbcTemplate setting !!!!!!");
        System.out.println(this.jdbcTemplate);
    }

    public void save(Student student) {
        String sql = "INSERT INTO student (id, name, age, email, subject, rollno) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, student.getId(), student.getName(), student.getAge(), student.getEmail(), student.getSubject(), student.getRollno());
    }

    public void update(Student student) {
        String sql = "UPDATE student SET name = ?, age = ?, email = ?, subject = ?, rollno = ? WHERE id = ?";
        jdbcTemplate.update(sql, student.getName(), student.getAge(),  student.getEmail(), student.getSubject(), student.getRollno(), student.getId());
    }

    public void delete(long id) {
        String sql = "DELETE FROM student WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Student findById(long id) {
        String sql = "SELECT * FROM student WHERE id = ?";
        return jdbcTemplate.queryForObject(sql,  BeanPropertyRowMapper.newInstance(Student.class),id);
    }

    public List<Student> findAll() {
        String sql = "SELECT * FROM student";
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Student.class));
    }
}
