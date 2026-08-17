package com.sagar.controller;

import com.sagar.entity.Books;
import com.sagar.service.BooksService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BooksController {
    @Autowired
    private BooksService service;



    @PostMapping
    public String saveBook(@Valid @RequestBody Books book){
        System.out.println(book.getBookName());
        service.saveBook(book);
        return "Book saved";


    }

    @GetMapping("/{id}")
    public Books getBook(@PathVariable("id") Long id){
        return service.getBook(id);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable("id") Long id){
        service.deleteBook(id);
    }

    @GetMapping("/sorted")
    public List<Books> getSorted(){
        return service.getBooksSorted();
    }

    @GetMapping
    public List<Books> getPaginatedBooks(@RequestParam("pageNo") int pageNo,@RequestParam("pageSize") int pageSize){
        return service.getPaginatedBooks(pageSize,pageNo);
    }


}
