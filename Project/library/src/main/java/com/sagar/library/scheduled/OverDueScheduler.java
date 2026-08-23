package com.sagar.library.scheduled;

import com.sagar.library.constant.BorrowStatus;
import com.sagar.library.entity.BorrowRecord;
import com.sagar.library.repository.BorrowRecordRepository;
import com.sagar.library.util.EmailUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class OverDueScheduler {
    private final BorrowRecordRepository borrowRecordRepository;
    private final EmailUtil emailUtil;

    public OverDueScheduler(BorrowRecordRepository borrowRecordRepository, EmailUtil emailUtil) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.emailUtil = emailUtil;
    }



     @Transactional
     @Scheduled(cron = "0 0 0 * * *")
    public void sendOverDueMails(){
        log.info("checking for overdues");
        List<BorrowRecord> overdues=borrowRecordRepository.findByDueDateBeforeAndBorrowStatusNot(LocalDate.now(), BorrowStatus.RETURNED);
         for(BorrowRecord borrowRecord : overdues){
             borrowRecord.setBorrowStatus(BorrowStatus.OVERDUE);

         }
         for(BorrowRecord borrowRecord : overdues){
            emailUtil.sendMail(borrowRecord.getBorrowedBy().getEmail(),"Your borrow is Overdue","Please return the Book you borrowed its over due book id : "+borrowRecord.getBorrowedBook().getId()+" borrow id :"+borrowRecord.getBorrowId());

         }
         log.info("mail sent for all over dues");


    }
}
