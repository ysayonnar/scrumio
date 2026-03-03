package com.example.scrumio.service;

import com.example.scrumio.entity.user.User;
import com.example.scrumio.mapper.UserMapper;
import com.example.scrumio.repository.UserRepository;
import com.example.scrumio.web.dto.UserPatchRequest;
import com.example.scrumio.web.dto.UserRequest;
import com.example.scrumio.web.dto.UserResponse;
import com.example.scrumio.web.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository, UserMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAllActive().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return mapper.toResponse(findActive(id));
    }

    public UserResponse create(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(request.password());
        user.setRole(request.role());
        return mapper.toResponse(userRepository.save(user));
    }

    public UserResponse update(UUID id, UserRequest request) {
        User user = findActive(id);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(request.password());
        user.setRole(request.role());
        return mapper.toResponse(userRepository.save(user));
    }

    public UserResponse patch(UUID id, UserPatchRequest request) {
        User user = findActive(id);
        if (request.name() != null) user.setName(request.name());
        if (request.email() != null) user.setEmail(request.email());
        if (request.password() != null) user.setPasswordHash(request.password());
        return mapper.toResponse(userRepository.save(user));
    }

    public UserResponse delete(UUID id) {
        User user = findActive(id);
        user.setDeletedAt(OffsetDateTime.now());
        return mapper.toResponse(userRepository.save(user));
    }

    private User findActive(UUID id) {
        return userRepository.findActiveById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
