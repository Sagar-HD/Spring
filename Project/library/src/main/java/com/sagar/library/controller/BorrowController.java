package com.sagar.library.controller;

import com.sagar.library.entity.BorrowRecord;
import com.sagar.library.security.CustomUserDetails;
import com.sagar.library.service.BorrowRecordService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("borrow")
@Validated
public class BorrowController {
    private BorrowRecordService borrowRecordService;

    public BorrowController(BorrowRecordService borrowRecordService) {
        this.borrowRecordService = borrowRecordService;
    }

    @PostMapping("/{id}")
    public String borrowBook(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("id") @NotNull Long bookId){
        borrowRecordService.borrowBook(userDetails.getUserId(),bookId);
        return "Book borrowed succesfully";
    }

    @PostMapping("/{id}/approve")
    public String approveBook(@PathVariable("id") Long borrowId){
        borrowRecordService.approveBorrow(borrowId);
        return "Book Approved";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable(name = "id"  ) Long borrowId){
        borrowRecordService.returnBook(borrowId,userDetails.getUserId());
        return "Book returned Succesfully";
    }

    @GetMapping
    public ResponseEntity<List<BorrowRecord>> getAllBorrowRecords(@RequestParam(name = "sortByDueDate" , required = false) Boolean sortBy ,@RequestParam(name = "status",required = false)String status){
        return ResponseEntity.ok(borrowRecordService.getAllBorrowRecords(sortBy,status));
    }

    @GetMapping("/my")
    public  ResponseEntity<List<BorrowRecord>> getMyBorrows(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestParam(name = "sortByDueDate" , required = false) Boolean sortBy ,@RequestParam(name = "status",required = false)String status){
        return ResponseEntity.ok(borrowRecordService.getUserBorrowRecords(userDetails.getUserId(),sortBy,status));
    }


}
