package com.kariimhosny.filesyncserver.space.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.auth.enrtities.User;
import com.kariimhosny.filesyncserver.auth.repositories.contracts.IUserRepository;
import com.kariimhosny.filesyncserver.space.dto.CreateSpaceRequest;
import com.kariimhosny.filesyncserver.space.dto.JoinSpaceRequest;
import com.kariimhosny.filesyncserver.space.dto.SpaceResponse;
import com.kariimhosny.filesyncserver.space.entities.Space;
import com.kariimhosny.filesyncserver.space.repositories.SpaceRepository;
import com.kariimhosny.filesyncserver.space.services.contracts.ISpaceService;

import jakarta.transaction.Transactional;

// Service
@Service
@Transactional
public class SpaceService implements  ISpaceService{
    
    private IUserRepository userRepository;
    private SpaceRepository spaceRepository;
    
    public SpaceService(SpaceRepository spaceRepository, IUserRepository userRepository){
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
    }
    
    public SpaceResponse createSpace(CreateSpaceRequest request) {
        // Get authenticated user
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Assuming AuthUser has getId()
        Long ownerId = authUser.getId();
        
        // Check if user already owns a space
        Optional<Space> existingSpace = spaceRepository.findByOwner(ownerId);
        if (existingSpace.isPresent()) {
            throw new RuntimeException("User already owns a space");
        }
        
        // Check if space name already exists
        if (spaceRepository.existsByName(request.getName())) {
            throw new RuntimeException("Space name already exists");
        }
        
        // Create space
        Space space = new Space();
        space.setName(request.getName());
        space.setOwner(ownerId);
        
        Space savedSpace = spaceRepository.save(space);
        
        // Add owner to the space
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new RuntimeException("Owner not found"));
        owner.setSpaceId(savedSpace.getId());
        
        return mapToResponse(savedSpace);
    }
    
    public SpaceResponse joinSpace(JoinSpaceRequest request) {
        // Check if space exists
        Space space = spaceRepository.findById(request.getSpaceId())
            .orElseThrow(() -> new RuntimeException("Space not found"));

        // Get authenticated user
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Assuming AuthUser has getId()

        // Check if user is already in a space
        if (userRepository.findUserInSpace(authUser.getId(), space.getId()) != null) {
            throw new RuntimeException("User is already in a space");
        }

        // Add user to space
        userRepository.updateUserSpaceIdById(authUser.getId(), space.getId());

        return mapToResponse(space);
    }
    
    public SpaceResponse getSpace(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
            .orElseThrow(() -> new RuntimeException("Space not found"));
        return mapToResponse(space);
    }
    
    public List<User> getSpaceMembers(Long spaceId) {
        // Verify space exists
        spaceRepository.findById(spaceId)
            .orElseThrow(() -> new RuntimeException("Space not found"));
        
        return userRepository.findBySpaceId(spaceId);
    }
    
    private SpaceResponse mapToResponse(Space space) {
        SpaceResponse response = new SpaceResponse();
        response.setId(space.getId());
        response.setName(space.getName());
        response.setOwner(space.getOwner());
        
        // Get owner name
        userRepository.findById(space.getOwner())
            .ifPresent(owner -> response.setOwnerName(owner.getName()));
        
        // Get member count
        Integer memberCount = spaceRepository.countMembersBySpaceId(space.getId());
        response.setMemberCount(memberCount);
        
        return response;
    }
}

