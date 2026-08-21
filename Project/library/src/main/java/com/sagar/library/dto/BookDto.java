package com.sagar.library.dto;

import jakarta.persistence.Column;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;

    private String name;


    private String authorName;

    @Min(1)
    private Integer noOfCopies;

    private Integer availableCopies;

}
