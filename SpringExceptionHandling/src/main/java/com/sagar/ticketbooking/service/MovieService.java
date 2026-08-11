package com.sagar.ticketbooking.service;

import com.sagar.ticketbooking.dto.Movie;
import com.sagar.ticketbooking.exception.MovieAlreadyStartedException;
import com.sagar.ticketbooking.exception.MovieNotFoundException;
import com.sagar.ticketbooking.exception.SeatsNotAvailableException;
import com.sagar.ticketbooking.repository.MoviesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovieService {

    private MoviesRepository moviesRepository;

    public MovieService(MoviesRepository moviesRepository) {
        this.moviesRepository = moviesRepository;
    }

    public List<Movie> getAllMovies(){
        return moviesRepository.getAllMovies();
    }
    public Movie getMovieByName(String name){
        return moviesRepository.getMovieByName(name).orElseThrow(()->new MovieNotFoundException("movie not found"));
    }
    public void bookTicket(String movieName,int noOfSeats){
        Movie movie = getMovieByName(movieName);
        if(movie.getShowTime().isBefore(LocalDateTime.now())){
            throw new MovieAlreadyStartedException("movie already started");
        }
        if(!moviesRepository.isSeatsAvailable(movieName,noOfSeats)){
            throw new SeatsNotAvailableException("not enough seats available");
        }

        moviesRepository.bookTicket(movieName,noOfSeats);
    }
}
