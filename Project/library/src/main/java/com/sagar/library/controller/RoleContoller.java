package com.sagar.library.controller;

import com.sagar.library.entity.Role;
import com.sagar.library.repository.RoleRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class RoleContoller {
    private RoleRepository roleRepository;

    public RoleContoller(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostMapping("/role")
    public String addRole(@RequestParam("roleName") @NotBlank String roleName){
        Role role=new Role();
        role.setName(roleName);
        roleRepository.save(role);
        return "Role added";


    }
}
