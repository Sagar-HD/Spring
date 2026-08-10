package com.sagar.storage;

import com.sagar.dto.UserRequestDto;
import com.sagar.dto.UserResponseDto;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UserStore {

    private Map<Long,UserRequestDto> users=new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Long generateId() {
        return idGenerator.getAndIncrement();
    }



    public List<UserRequestDto> getUsers() {
        return new ArrayList<>(users.values());
    }
    public UserResponseDto getUserById(Long id) {

        UserRequestDto res=users.get(id);
        if(res==null) return null;
        UserResponseDto resp=new UserResponseDto(res);
        resp.setId(id);
        return resp;

    }


    public UserResponseDto save(UserRequestDto userRequestDto) {
        Long id = generateId();

        users.put(id, userRequestDto);
        UserResponseDto res=new UserResponseDto(userRequestDto);
        res.setId(id);


        return res;

    }

    public void deleteUserById(Long id) {
        users.remove(id);
    }

    public void update(UserResponseDto userToUpdate) {
        //this is very base design should use user entity  to store
        UserRequestDto userToUpdateinStore =users.get(userToUpdate.getId());
        userToUpdateinStore.setName(userToUpdate.getName());
        userToUpdateinStore.setEmail(userToUpdate.getEmail());

    }
}
