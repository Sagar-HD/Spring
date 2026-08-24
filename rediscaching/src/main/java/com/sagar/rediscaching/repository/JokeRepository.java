package com.sagar.rediscaching.repository;

import com.sagar.rediscaching.entity.Joke;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JokeRepository extends JpaRepository<Joke,Long> {

}
