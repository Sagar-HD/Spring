package com.sagar.rediscaching.service;

import com.sagar.rediscaching.entity.Joke;
import com.sagar.rediscaching.repository.JokeRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JokesService {
    private final RedisService redisService;
    private final JokeRepository jokeRepository;

    public JokesService(RedisService redisService, JokeRepository jokeRepository) {
        this.redisService = redisService;
        this.jokeRepository = jokeRepository;
    }

    public List<Joke> getAllJokes(){
        List<Joke> cacheJokes=redisService.getList("jokes:all",Joke.class);
        if(cacheJokes!=null){
            System.out.println("cache hit");
            System.out.println("giving data from redis ");
            return cacheJokes;
        }
        else{
            System.out.println("cache miss ");
            List<Joke> jokes=jokeRepository.findAll();
            redisService.set("jokes:all",jokes,15L);
            System.out.println("db and data saved to cache");
            return jokes;


        }

    }

    public Joke getJoke(Long id) {
        Joke cachejoke=redisService.get("joke:"+id,Joke.class);
        if(cachejoke!=null){
            System.out.println("cache hit");
            System.out.println("giving data from redis ");
            return cachejoke;
        }
        else{
            System.out.println("cache miss ");
            Joke joke =jokeRepository.findById(id).orElseThrow();
            redisService.set("joke:"+id,joke,120L);
            System.out.println("db and data saved to cache");
            return joke;


        }

    }

    @Transactional
    public  Joke updateJoke(Long id, String answer) {
        Joke joke=jokeRepository.findById(id).orElseThrow();
        joke.setAnswer(answer);

        redisService.remove("joke:"+id);
        redisService.remove("jokes:all");
        System.out.println("cache invalidate from id "+id);
        return joke;

    }

    @Cacheable(value = "jokes",key = "'all'")
    public  List<Joke> getAllJokesv2() {
        return jokeRepository.findAll();

    }

    @Cacheable(value = "joke" ,key="#id")
    public Joke getJokev2(Long id) {
        return jokeRepository.findById(id).orElseThrow();
    }

    @Caching(
            put = @CachePut(value = "joke", key = "#id"),
            evict = @CacheEvict(value = "jokes", key = "'all'")
    )
    @Transactional
    public Joke updateJokev2(Long id, String message) {
        Joke joke=jokeRepository.findById(id).orElseThrow();
        joke.setAnswer(message);
        return joke;
    }


    @Caching(evict = {
            @CacheEvict(value = "joke", key = "#id"),
            @CacheEvict(value = "jokes", key = "'all'")
    })
    public  void deleteJokev2(Long id) {
        jokeRepository.deleteById(id);

    }
}
