package com.sagar.library.service;

import com.sagar.library.dto.BookDto;
import com.sagar.library.entity.Book;
import com.sagar.library.exception.BookLoanNotClearedBeforeDeleteException;
import com.sagar.library.exception.BookNotFoundException;
import com.sagar.library.repository.BookRepository;
import com.sagar.library.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class BookService {
    private BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookDto addBook(BookDto bookDto){
        Book newBook = new Book();
        newBook.setName(bookDto.getName());
        newBook.setAuthorName(bookDto.getAuthorName());
        newBook.setNoOfCopies(bookDto.getNoOfCopies());
        newBook.setAvailableCopies(bookDto.getNoOfCopies());
        bookRepository.save(newBook);
        log.info("new book added with book id {}",newBook.getId());
        return bookDto;

    }

    public BookDto getBook(Long id){
       Book book=bookRepository.findById(id).orElseThrow(()->new BookNotFoundException("book not found"));
       BookDto bookDto=BookDto.builder().build();
       bookDto.setAuthorName(book.getAuthorName());
       bookDto.setId(book.getId());
       bookDto.setNoOfCopies(book.getNoOfCopies());
       bookDto.setAvailableCopies(book.getAvailableCopies());
       bookDto.setName(book.getName());

        return bookDto;

    }

    public List<BookDto> getAllBooks(String title,String authorName){
        List<Book> books=new ArrayList<>();

        if(title==null && authorName==null) {
            books = bookRepository.findAll();
        }
        else if(title!=null && authorName!=null){
            books=bookRepository.findByNameContainingIgnoreCaseAndAuthorNameContainingIgnoreCase(title,authorName);

        }
        else if(title!=null){
            books=bookRepository.findByNameContainingIgnoreCase(title);
        }
        else{
            books=bookRepository.findByAuthorNameContainingIgnoreCase(authorName);
        }
        List<BookDto> booksResp=books.stream().map((book)->BookDto.builder().id(book.getId()).name(book.getName()).authorName(book.getAuthorName()).noOfCopies(book.getNoOfCopies()).availableCopies(book.getAvailableCopies()).build()).toList();
        return  booksResp;
    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @Transactional
    public BookDto updateBook(BookDto bookDto,Long id){
        Book book=bookRepository.findById(id).orElseThrow(()->new BookNotFoundException("book not found"));
       if(bookDto.getAuthorName()!=null) book.setAuthorName(bookDto.getAuthorName());
       if(bookDto.getName()!=null) book.setName(bookDto.getName());
       if(bookDto.getAvailableCopies()!=null) {book.setAvailableCopies(bookDto.getAvailableCopies());}
        int borrowedCopies =
                book.getNoOfCopies() - book.getAvailableCopies();

        if (bookDto.getNoOfCopies() != null) {

            if (bookDto.getNoOfCopies() < borrowedCopies) {
                throw new IllegalArgumentException(
                        "Total copies cannot be less than borrowed copies"
                );
            }

            int difference =
                    bookDto.getNoOfCopies() - book.getNoOfCopies();

            book.setNoOfCopies(bookDto.getNoOfCopies());
            book.setAvailableCopies(
                    book.getAvailableCopies() + difference
            );
        }



        log.info("book with book id : {} updated by user id : {} ",book.getId(),id);
        return BookDto.builder().name(book.getName()).authorName(book.getAuthorName()).id(book.getId()).availableCopies(book.getAvailableCopies()).noOfCopies(book.getNoOfCopies()).build();
    }
    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    public void deleteBook(Long id){
        Book book=bookRepository.findById(id).orElseThrow(()->new BookNotFoundException("book not found"));
        if(book.getNoOfCopies()== book.getAvailableCopies()) {
            bookRepository.delete(book);
        }
        else{
            throw new BookLoanNotClearedBeforeDeleteException("before deleting book make sure all lones are cleared");
        }
        CustomUserDetails userDetails= (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("book with id : {} deleted by user id : {} ",id,userDetails.getUserId());

         }


}
