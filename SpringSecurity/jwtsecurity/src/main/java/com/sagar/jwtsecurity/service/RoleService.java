package com.sagar.jwtsecurity.service;

import com.sagar.jwtsecurity.entity.Role;
import com.sagar.jwtsecurity.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository){
        this.roleRepository=roleRepository;
    }


    public void addRole(String roleName){
        roleRepository.save(new Role(roleName));
    }
}
