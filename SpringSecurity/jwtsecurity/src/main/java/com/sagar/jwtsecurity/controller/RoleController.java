package com.sagar.jwtsecurity.controller;

import com.sagar.jwtsecurity.repository.RoleRepository;
import com.sagar.jwtsecurity.service.RoleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController {

    private RoleService roleService;

    public RoleController(RoleService roleService){
        this.roleService=roleService;
    }


    @PostMapping
    public String addRole(@RequestParam("roleName") String roleName){
        roleService.addRole(roleName);
        return "Role added sucessfully";

    }
}
