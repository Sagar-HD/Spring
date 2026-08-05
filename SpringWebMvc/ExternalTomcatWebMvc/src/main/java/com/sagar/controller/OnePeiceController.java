package com.sagar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@ResponseBody
@RequestMapping("/")
public class OnePeiceController {
    public  OnePeiceController(){
        System.out.println("one peice controller created");
    }
    @GetMapping("/")
    public ResponseEntity<List<String>> getChars(){
        List<String> names = List.of(
                "Luffy",
                "Zoro",
                "Sanji"
        );
        return  ResponseEntity.ok(names);


    }
}
