package com.sagar.library.repository;

import com.sagar.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book,Long> {
    List<Book> findByNameContainingIgnoreCase(String title);

    List<Book> findByAuthorNameContainingIgnoreCase(String author);

    List<Book> findByNameContainingIgnoreCaseAndAuthorNameContainingIgnoreCase(
            String title,
            String author
    );
}
