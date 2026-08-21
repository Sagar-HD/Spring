package com.sagar.library.entity;

import com.sagar.library.constant.BorrowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long borrowId;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book borrowedBook;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User borrowedBy;

    @Column(name = "borrowed_at")
    private LocalDate borrowedAt;

    @Column(name = "borrow_status")
    private BorrowStatus borrowStatus;

    @Column(name = "due_date")
    private LocalDate dueDate;



}
