package com.sagar.ticketbooking.controller;

import com.sagar.ticketbooking.dto.BookingRequest;
import com.sagar.ticketbooking.dto.Movie;
import com.sagar.ticketbooking.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/movies")
@Validated
public class MovieController {
    private MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<Map<String,List<Movie>>> getMovies(){
        HashMap<String,List<Movie>> res=new HashMap<>();
        res.put("data",movieService.getAllMovies());

        return ResponseEntity.ok().body(res);


    }

    @GetMapping("/{movieName}")
    public ResponseEntity<Movie> getMovieById(@PathVariable @NotBlank(message = "movie name is required")String movieName){

        return ResponseEntity.ok(movieService.getMovieByName(movieName));

    }

    //custom action
    @PostMapping("/book")
    public ResponseEntity<String> bookTicket(@RequestBody @Valid BookingRequest request){
        movieService.bookTicket(request.getMovieName(),request.getNoOfTickets());
        return ResponseEntity.ok("Ticket booked successfully");
    }



}
