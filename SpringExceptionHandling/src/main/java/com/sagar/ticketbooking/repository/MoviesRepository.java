package com.sagar.ticketbooking.repository;

import com.sagar.ticketbooking.dto.Movie;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class MoviesRepository {
    HashMap<String, Movie> movies;
    public MoviesRepository() {
        this.movies = new HashMap<>();
        this.movies.put("spiderman", new Movie("spiderman", "1", LocalDateTime.now().plusMonths(1), 1.0, 1));
        this.movies.put("ramayana", new Movie("ramayana", "2", LocalDateTime.now().minusDays(1), 2.0, 2));
        this.movies.put("toxic", new Movie("toxic", "3", LocalDateTime.now().plusWeeks(2), 3.0, 3));
        this.movies.put("inception", new Movie("inception", "4", LocalDateTime.now().plusMonths(1), 4.0, 4));
        this.movies.put("three idiots", new Movie("three idiots", "5", LocalDateTime.now().minusYears(10), 5.0, 5));
    }
    public List<Movie> getAllMovies(){
        return new ArrayList<>(movies.values());
    }
    public Optional<Movie> getMovieByName(String name){
        return Optional.ofNullable(movies.get(name));
    }

    public boolean isSeatsAvailable(String movieName,int noOfSeats){
        return movies.get(movieName).getSeatsAvailable() >= noOfSeats;
    }

    public void bookTicket(String movieName,int noOfSeats){
        movies.get(movieName).setSeatsAvailable(movies.get(movieName).getSeatsAvailable() - noOfSeats);
    }


}
