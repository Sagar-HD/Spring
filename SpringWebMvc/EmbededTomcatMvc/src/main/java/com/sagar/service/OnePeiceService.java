package com.sagar.service;

import com.sagar.entity.AnimeCharacter;
import com.sagar.repository.OnePeiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnePeiceService {
    private OnePeiceRepository repo;

    public OnePeiceService(OnePeiceRepository repo){
        this.repo=repo;
    }


    public void save(AnimeCharacter character){
        repo.saveCharacter(character);

    }

    public Optional<AnimeCharacter> getByName(String name){
        return repo.getCharacterByName(name);
    }

    public List<AnimeCharacter> getAll(){
        return repo.getAllChar();
    }


}
