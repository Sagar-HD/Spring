package com.sagar.service;

import com.sagar.repository.BooksRepository;
import com.sagar.entity.Books;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.List;

@Service
public class BooksService {
    @Autowired
    private BooksRepository repo;
     @Transactional
    public void saveBook(Books book){
        repo.save(book);
    }

    @Transactional
    public Books getBook(Long id){
         return repo.findById(id).orElseThrow(()->new RuntimeException("data not found"));

    }

    @Transactional
    public void deleteBook(Long id){
         repo.deleteById(id);
    }

    @Transactional
    public List<Books> getBooksSorted(){
         Sort sort=Sort.by("book_name").descending();
        return repo.findAll(sort);

    }

    @Transactional
    public List<Books> getPaginatedBooks(int pageSize,int pageNo){
        Pageable pageable=PageRequest.of(pageNo,pageSize);
        Page<Books> page=repo.findAll(pageable);
        return page.getContent();

    }
}
