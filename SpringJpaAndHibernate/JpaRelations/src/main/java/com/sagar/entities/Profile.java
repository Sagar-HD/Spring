package com.sagar.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String bio;

    @ElementCollection
    @CollectionTable(name = "hobbies")
    private List<String>  hobbies;

    @OneToOne(mappedBy = "profile")
    private Student student;

}
