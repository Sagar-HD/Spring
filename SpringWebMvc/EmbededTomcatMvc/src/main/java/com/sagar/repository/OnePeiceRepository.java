package com.sagar.repository;

import com.sagar.entity.AnimeCharacter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class OnePeiceRepository {
    private HashMap<String,AnimeCharacter> map=new HashMap<>();

    public void saveCharacter(AnimeCharacter character){
        map.put(character.getName().toLowerCase(),character);
    }
    public List<AnimeCharacter> getAllChar(){
        return new ArrayList<>(map.values());
    }
    public Optional<AnimeCharacter> getCharacterByName(String name){
        if(map.containsKey(name.toLowerCase())){
            return Optional.of(map.get(name.toLowerCase()));
        }
        else{
            return Optional.empty();
        }
    }


}
