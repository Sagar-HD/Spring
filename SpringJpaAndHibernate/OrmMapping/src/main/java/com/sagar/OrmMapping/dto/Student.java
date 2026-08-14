package com.sagar.OrmMapping.dto;

import com.sagar.OrmMapping.constant.AdmissionType;
import com.sagar.OrmMapping.dto.convertors.FeesConvertor;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(name="date_of_birth")
    private LocalDate dob;

    private LocalDateTime createdAt;

    @Convert(converter = FeesConvertor.class)
    private Boolean feesPaid;

    @Enumerated(EnumType.STRING)
    private AdmissionType admissionType;



    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="city",column = @Column(name="primary_city")),
        @AttributeOverride(name="pincode",column = @Column(name="primary_pincode"))
    })
    private Address primaryAddress;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="city",column = @Column(name="secondary_city")),
            @AttributeOverride(name="pincode",column = @Column(name="secondary_pincode"))
    })
    private Address secondaryAddress;

    @ElementCollection
    @CollectionTable(
            name="student_skills",
            joinColumns = @JoinColumn(name="student_id")
    )
    private List<String> studentSkills;









}
