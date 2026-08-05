package com.sagar.controller;

import com.sagar.entity.AnimeCharacter;
import com.sagar.service.OnePeiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@ResponseBody
@RequestMapping("/onepeice")
public class OnePeice {
    private OnePeiceService service;

    public OnePeice(OnePeiceService service){
        this.service=service;
    }

    @PostMapping("add")
    public ResponseEntity<String> addCharacter(@RequestBody AnimeCharacter character){

        service.save(character);
        return ResponseEntity.ok("character "+character.getName()+" saved");

    }
    @GetMapping("getall")
    public  ResponseEntity<List<AnimeCharacter>> getAllCharacters(){
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("get/{name}")
    public  ResponseEntity<AnimeCharacter> getCharacterByName(@PathVariable("name") String name){
        Optional<AnimeCharacter> resAnime=service.getByName(name);
        if(resAnime.isPresent()){
            return  ResponseEntity.ok(resAnime.get());
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

}
