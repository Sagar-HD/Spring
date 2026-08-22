package com.sagar.library.service;

import com.sagar.library.annotation.MeasureTime;
import com.sagar.library.constant.BorrowStatus;
import com.sagar.library.entity.Book;
import com.sagar.library.entity.BorrowRecord;
import com.sagar.library.entity.User;
import com.sagar.library.exception.*;
import com.sagar.library.repository.BookRepository;
import com.sagar.library.repository.BorrowRecordRepository;
import com.sagar.library.repository.UserRepository;
import com.sagar.library.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class BorrowRecordService {
    private UserRepository userRepository;
    private BorrowRecordRepository borrowRecordRepository;
    private BookRepository bookRepository;

    public BorrowRecordService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @MeasureTime(threshold = 10)
    public void borrowBook(Long userId, Long bookId){
        User user=userRepository.findById(userId).orElseThrow(()->new UsernameNotFoundException("user not found"));
        Book book=bookRepository.findById(bookId).orElseThrow(()->new BookNotFoundException("book not found"));
        if(book.getAvailableCopies()==0){
     throw  new BookNotAvailableException("Book is currently not available");
        }
        if(borrowRecordRepository.existsByBorrowedByIdAndBorrowedBookIdAndBorrowStatusIn(userId,bookId, List.of(BorrowStatus.BORROWED,BorrowStatus.REQUESTED))){
      throw new AlreadyBorrowedOrInRequestedException("book is already borrowed or in request list");
        }



        BorrowRecord borrowRecord=BorrowRecord.builder().borrowStatus(BorrowStatus.REQUESTED).borrowedBook(book).borrowedBy(user).borrowedAt(LocalDate.now())
                .dueDate(LocalDate.now().plusWeeks(2L)).build();

        borrowRecordRepository.save(borrowRecord);
        log.info("book id : {} borrowed by user id : {} ",bookId,userId);



    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @Transactional
    public void approveBorrow(Long borrowId){

        BorrowRecord borrowRecord=borrowRecordRepository.findById(borrowId).orElseThrow(()->new IllegalArgumentException("invalid borrow id"));
        if (borrowRecord.getBorrowStatus() != BorrowStatus.REQUESTED) {
            throw new IllegalStateException(
                    "Only requested books can be approved"
            );
        }

        Book borrowedBook=borrowRecord.getBorrowedBook();
        if (borrowedBook.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("Book is no longer available");
        }
        borrowRecord.setBorrowStatus(BorrowStatus.BORROWED);
        borrowedBook.setAvailableCopies(borrowedBook.getAvailableCopies()-1);
        borrowRecord.setDueDate(borrowRecord.getBorrowedAt().plusWeeks(2L));
        CustomUserDetails userDetails= (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("borrow for borrow id : {} approved by librarian id : {}",borrowId,userDetails.getUserId());


    }

    @Transactional
    @MeasureTime(threshold = 10)
    public void returnBook(Long borrowId,Long authenticatedUserId){
        BorrowRecord borrowRecord=borrowRecordRepository.findById(borrowId).orElseThrow(()->new IllegalArgumentException("invalid borrow id"));
        if(!authenticatedUserId.equals(borrowRecord.getBorrowedBy().getId()) ) throw new MaliciousAccessExcpetion("malicious access");
        if(borrowRecord.getBorrowStatus()!=BorrowStatus.BORROWED) throw new BusinessException("borrow id dont have any unreturned book");

        borrowRecord.setBorrowStatus(BorrowStatus.RETURNED);
        Book bookToReturn=borrowRecord.getBorrowedBook();
        bookToReturn.setAvailableCopies(bookToReturn.getAvailableCopies()+1);

    }

    @PreAuthorize("hasAnyRole('ADMIN','LIBRARIAN')")
    @MeasureTime(threshold = 10)
    public List<BorrowRecord> getAllBorrowRecords(Boolean sortByDueDate,String status){

        if((sortByDueDate==null || !sortByDueDate) && status==null)   return borrowRecordRepository.findAll();

        if((sortByDueDate!=null && sortByDueDate) && status!=null){
            Sort sort=Sort.by("dueDate");
            return borrowRecordRepository.getAllByBorrowStatus(BorrowStatus.valueOf(status.toUpperCase()),sort);

        }
        if(sortByDueDate!=null && sortByDueDate){
            Sort sort=Sort.by("dueDate");
            return borrowRecordRepository.findAll(sort);
        }

        return borrowRecordRepository.getAllByBorrowStatus(BorrowStatus.valueOf(status.toUpperCase()));


    }

    @MeasureTime(threshold = 10)
    public List<BorrowRecord> getUserBorrowRecords(Long authenticatedUserId,Boolean sortByDueDate,String status){
        if((sortByDueDate==null || !sortByDueDate) && status==null)  return  borrowRecordRepository.getAllByBorrowedById(authenticatedUserId);

        if((sortByDueDate!=null && sortByDueDate) && status!=null){
            Sort sort=Sort.by("dueDate");
            return borrowRecordRepository.getAllByBorrowedByIdAndBorrowStatus(authenticatedUserId,BorrowStatus.valueOf(status.toUpperCase()),sort);

        }
        if(sortByDueDate!=null && sortByDueDate){
            Sort sort=Sort.by("dueDate");
            return borrowRecordRepository.getAllByBorrowedById(authenticatedUserId,sort);
        }

        return borrowRecordRepository.getAllByBorrowedByIdAndBorrowStatus(authenticatedUserId,BorrowStatus.valueOf(status.toUpperCase()));

    }

}
