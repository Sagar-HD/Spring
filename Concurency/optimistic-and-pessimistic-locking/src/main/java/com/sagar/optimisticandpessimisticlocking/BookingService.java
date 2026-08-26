package com.sagar.optimisticandpessimisticlocking;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final SeatRepository seatRepository;

    public BookingService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }


    @Transactional
    public void bookSeatOptimistically(Long seatId){
        Seat seat=seatRepository.findById(seatId).orElseThrow(()->new RuntimeException("seat id not valid"));
        System.out.println(Thread.currentThread().getName()+" trying to book seat id "+seat+" with version "+seat.getVersion());
        if(seat.isBooked()){
            System.out.println("seat is already booked");
            return;
        }
        seat.setBooked(true);

    }

    @Transactional
    public void bookSeatPessimistically(Long seatId){
        System.out.println(Thread.currentThread().getName()+" is trying to acquire lock for seatId "+seatId);
        Seat seat=seatRepository.findAndLockSeat(seatId);
        System.out.println(Thread.currentThread().getName()+" is acquired lock for seatId "+seatId);
        if(seat.isBooked()){
            System.out.println("seat is already booked booking failed for "+Thread.currentThread().getName());
            return;
        }
        seat.setBooked(true);
        System.out.println("seat succfully nooked for "+Thread.currentThread().getName());

    }
}
