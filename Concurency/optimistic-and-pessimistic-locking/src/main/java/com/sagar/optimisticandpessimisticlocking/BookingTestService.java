package com.sagar.optimisticandpessimisticlocking;

import org.springframework.stereotype.Service;

@Service
public class BookingTestService {
    private final BookingService bookingService;

    public BookingTestService(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    public void testOptimisticBooking(Long seatId){

        Thread t1 = new Thread(() -> {
            try {
                bookingService.bookSeatOptimistically(seatId);
                System.out.println("seat booked by "+Thread.currentThread().getName());
            } catch (Exception e) {
                System.out.println("T1 failed: " + e.getMessage());
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                bookingService.bookSeatOptimistically(seatId);
                System.out.println("seat booked by "+Thread.currentThread().getName());
            } catch (Exception e) {
                System.out.println("T2 failed: " + e.getMessage());
            }
        });

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }



    public void testPessimisticBooking(Long seatId) throws InterruptedException {
        Thread t1=new Thread(()->bookingService.bookSeatPessimistically(seatId));
        Thread t2=new Thread(()->bookingService.bookSeatPessimistically(seatId));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

    }
}
