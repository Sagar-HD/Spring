package com.sagar.rediscaching.controller;

import com.sagar.rediscaching.entity.Joke;
import com.sagar.rediscaching.service.JokesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jokes")
public class JokesController {

    private final JokesService jokesService;

    public JokesController(JokesService jokesService) {
        this.jokesService = jokesService;
    }

    @GetMapping
    public ResponseEntity<List<Joke>> getAllJokes(){
        return ResponseEntity.ok(jokesService.getAllJokes());

    }
    @GetMapping("/{id}")
    public ResponseEntity<Joke> getJoke(@PathVariable("id") Long id){
        return ResponseEntity.ok(jokesService.getJoke(id));

    }
    @PatchMapping("/{id}")
    public ResponseEntity<Joke> updateJoke(@PathVariable("id") Long id,@RequestBody String message){
        return ResponseEntity.ok(jokesService.updateJoke(id,message));
    }


    @GetMapping("/v2")
    public ResponseEntity<List<Joke>> getAllJokesv2(){
        return ResponseEntity.ok(jokesService.getAllJokesv2());

    }
    @GetMapping("v2/{id}")
    public ResponseEntity<Joke> getJokev2(@PathVariable("id") Long id){
        return ResponseEntity.ok(jokesService.getJokev2(id));

    }
    @PatchMapping("v2/{id}")
    public ResponseEntity<Joke> updateJokev2(@PathVariable("id") Long id,@RequestBody String message){
        return ResponseEntity.ok(jokesService.updateJokev2(id,message));
    }


    @DeleteMapping("v2/{id}")
    public ResponseEntity<String> seleteJokev2(@PathVariable("id") Long id){
        jokesService.deleteJokev2(id);
        return ResponseEntity.ok("deleted");
    }

}
