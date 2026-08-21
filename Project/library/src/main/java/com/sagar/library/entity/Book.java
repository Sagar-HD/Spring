package com.sagar.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "no_of_copies")
    @Min(value = 1 ,message = "there must be at least 1 book while adding a book")
    private Integer noOfCopies;

    @Column(name = "available_copies")
    private Integer availableCopies;



}
