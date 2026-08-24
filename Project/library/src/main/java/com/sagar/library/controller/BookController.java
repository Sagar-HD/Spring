package com.sagar.library.controller;

import com.sagar.library.dto.BookDto;
import com.sagar.library.repository.BookRepository;
import com.sagar.library.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("books")
@Tag(name = "02 - Books Operations" ,description = "add new book, delete book and update book (ADMIN,LIBRARIAN) ,get book , get all book (authenticated user) ")
public class BookController {
    private BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDto> addBook(@RequestBody BookDto bookDto){
        return ResponseEntity.ok(bookService.addBook(bookDto));

    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable("id") Long id){
        return ResponseEntity.ok(bookService.getBook(id));
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks( @RequestParam(name = "title", required = false) String title,
                                                       @RequestParam(name = "authorName", required = false) String author){
        return ResponseEntity.ok(bookService.getAllBooks(title,author));
    }
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@RequestBody BookDto bookDto,@PathVariable("id") Long id){
        return ResponseEntity.ok(bookService.updateBook(bookDto,id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable("id") Long id){
        bookService.deleteBook(id);
        return ResponseEntity.ok("book deleted");
    }

}
