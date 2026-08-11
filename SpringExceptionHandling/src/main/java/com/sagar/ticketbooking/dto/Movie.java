package com.sagar.ticketbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Movie {

    private String name;
    private String theater;
    private LocalDateTime showTime;
    private double ticketPrice;
    private int seatsAvailable;


}
