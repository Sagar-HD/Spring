package com.sagar.service;

import com.sagar.dto.UserRequestDto;
import com.sagar.dto.UserResponseDto;
import com.sagar.storage.UserStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private UserStore userStore;
    public UserService(UserStore userStore) {
        this.userStore = userStore;
    }
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        UserResponseDto resp= userStore.save(userRequestDto);
        resp.setMessage("User created successfully");
        return resp;

    }


    public List<UserResponseDto> getAllUsers() {
        return userStore.getUsers().stream().map(UserResponseDto::new).collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
       UserResponseDto res=userStore.getUserById(id);
       if(res==null) return null;
       res.setMessage("User found successfully");
       return res;

    }

    public Boolean deleteUserById(Long id) {
        UserResponseDto userToDlt=userStore.getUserById(id);
        if(userToDlt==null){
            return false;
        }
        else{
            userStore.deleteUserById(id);
            return true;

        }


    }

    public UserResponseDto updateById(@Valid @Positive Long id, @Valid UserRequestDto userRequestDto) {
        UserResponseDto userToUpdate=userStore.getUserById(id);
        if(userToUpdate==null){
            return null;
        }
        else{



            //this is very base design should use user entity  to store
            userToUpdate.setName(userRequestDto.getName());
            userToUpdate.setEmail(userRequestDto.getEmail());
            userStore.update(userToUpdate);
            UserResponseDto resp= userToUpdate;
            resp.setMessage("User updated successfully");
            return resp;

        }
    }
}
