package com.sagar.ticketbooking.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BookingRequest {

    @NotBlank(message = "movie name is required")
    private String movieName;

    @NotNull(message = "no of tickets is required")
    @Min(value = 1,message = "no of tickets should be greater than 0")
    @Max(value = 6,message = "no of tickets should be less than 6")
    private Integer noOfTickets;

}
