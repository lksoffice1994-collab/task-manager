package com.learning.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.entity.RoleEntity;
import com.learning.model.RoleDto;
import com.learning.repository.RoleRepository;
import com.learning.service.RoleService;

public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleDto getRoleByName(String roleName) {

        RoleEntity roleEntity = roleRepository.findByRoleName(roleName);

        if (roleEntity == null) {
            throw new RuntimeException("runtime exception");
        }

        return objectMapper.convertValue(roleEntity, RoleDto.class);
    }
}
