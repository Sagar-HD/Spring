package com.sagar.repository;

import com.sagar.entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BooksRepository extends JpaRepository<Books,Long> {
}
