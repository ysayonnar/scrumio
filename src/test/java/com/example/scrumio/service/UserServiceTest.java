package com.example.scrumio.service;

import com.example.scrumio.entity.user.User;
import com.example.scrumio.entity.user.UserRole;
import com.example.scrumio.mapper.UserMapper;
import com.example.scrumio.repository.UserRepository;
import com.example.scrumio.web.dto.UserPatchRequest;
import com.example.scrumio.web.dto.UserRequest;
import com.example.scrumio.web.dto.UserResponse;
import com.example.scrumio.web.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    private User stubUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(UserRole.MEMBER);
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    private UserResponse stubResponse(UUID id) {
        return new UserResponse(id, "Test User", "test@example.com", UserRole.MEMBER, OffsetDateTime.now());
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnAllUsers() {
            User user = stubUser(userId);
            UserResponse response = stubResponse(userId);
            when(userRepository.findAllActive()).thenReturn(List.of(user));
            when(mapper.toResponse(user)).thenReturn(response);

            List<UserResponse> result = service.getAll();

            assertEquals(1, result.size());
            assertEquals(response, result.get(0));
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnUser() {
            User user = stubUser(userId);
            UserResponse response = stubResponse(userId);
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(user));
            when(mapper.toResponse(user)).thenReturn(response);

            UserResponse result = service.getById(userId);

            assertEquals(response, result);
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> service.getById(userId));
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateUser() {
            UserRequest request = new UserRequest("Jane", "jane@example.com", "pass", UserRole.MEMBER);
            User saved = stubUser(UUID.randomUUID());
            UserResponse response = stubResponse(saved.getId());
            when(userRepository.save(any(User.class))).thenReturn(saved);
            when(mapper.toResponse(saved)).thenReturn(response);

            UserResponse result = service.create(request);

            assertEquals(response, result);
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateUser() {
            User existing = stubUser(userId);
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(existing);
            UserResponse response = stubResponse(userId);
            when(mapper.toResponse(existing)).thenReturn(response);

            UserRequest request = new UserRequest("Updated", "updated@example.com", "newpass", UserRole.ADMIN);
            UserResponse result = service.update(userId, request);

            assertEquals(response, result);
        }

        @Test
        void shouldThrowWhenNotFoundOnUpdate() {
            when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

            UserRequest request = new UserRequest("Updated", "updated@example.com", "newpass", UserRole.MEMBER);
            assertThrows(UserNotFoundException.class, () -> service.update(userId, request));
        }
    }

    @Nested
    class Patch {

        @Test
        void shouldPatchOnlyProvidedFields() {
            User existing = stubUser(userId);
            existing.setName("Original");
            existing.setEmail("original@example.com");
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(existing);
            when(mapper.toResponse(existing)).thenReturn(stubResponse(userId));

            UserPatchRequest request = new UserPatchRequest("Patched", null, null);
            service.patch(userId, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertEquals("Patched", captor.getValue().getName());
            assertEquals("original@example.com", captor.getValue().getEmail());
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldSoftDeleteAndCascade() {
            User existing = stubUser(userId);
            when(userRepository.findActiveById(userId)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenReturn(existing);
            when(mapper.toResponse(existing)).thenReturn(stubResponse(userId));

            UserResponse result = service.delete(userId);

            assertNotNull(result);
            assertNotNull(existing.getDeletedAt());
            verify(projectService).cascadeDeleteAllByOwner(userId);
        }

        @Test
        void shouldThrowWhenNotFoundOnDelete() {
            when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> service.delete(userId));
        }
    }
}
