package com.sagar.optimisticandpessimisticlocking;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private final BookingTestService bookingTestService;

    public Controller(BookingTestService bookingTestService) {
        this.bookingTestService = bookingTestService;
    }

    @PostMapping("optimistic/{id}")
    public void bookSeatOptimistically(@PathVariable("id") Long id){
        bookingTestService.testOptimisticBooking(id);
    }
    @PostMapping("pessimistic/{id}")
    public void bookSeatPessimistically(@PathVariable("id") Long id) throws InterruptedException {
        bookingTestService.testPessimisticBooking(id);
    }
}
