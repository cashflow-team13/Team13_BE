package com.cashflow.backend.user;

import com.cashflow.backend.exception.ResourceNotFoundException;
import com.cashflow.backend.user.dto.UserRequestDto;
import com.cashflow.backend.user.dto.UserResponseDto;
import com.cashflow.backend.user.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트 (DB 없이 Mockito로 UserRepository를 대체)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequestDto request(String name, String email) {
        UserRequestDto dto = new UserRequestDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    // User에는 id setter가 없으므로 리플렉션으로 id를 넣는다
    private User userWithId(Long id, String name, String email) {
        User user = new User(name, email);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // ---------- 등록 ----------

    @Test
    void createUser_success() {
        when(userRepository.existsByEmail("chen@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 1L);
            return u;
        });

        UserResponseDto result = userService.createUser(request("Chen", "chen@test.com"));

        assertEquals(1L, result.getId());
        assertEquals("Chen", result.getName());
        assertEquals("chen@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmailReturns409() {
        when(userRepository.existsByEmail("chen@test.com")).thenReturn(true);

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(request("Chen", "chen@test.com")));

        assertEquals(409, e.getStatusCode().value());
        verify(userRepository, never()).save(any(User.class));
    }

    // ---------- 조회 ----------

    @Test
    void getUser_found() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(userWithId(1L, "Chen", "chen@test.com")));

        UserResponseDto result = userService.getUser(1L);

        assertEquals(1L, result.getId());
        assertEquals("Chen", result.getName());
        assertEquals("chen@test.com", result.getEmail());
    }

    @Test
    void getUser_notFoundThrowsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(99L));
    }

    // ---------- 수정 ----------

    @Test
    void updateUser_changesNameAndEmail() {
        User user = userWithId(1L, "Old", "old@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

        UserResponseDto result = userService.updateUser(1L, request("New", "new@test.com"));

        assertEquals("New", result.getName());
        assertEquals("new@test.com", result.getEmail());
    }

    @Test
    void updateUser_keepingOwnEmailIsAllowed() {
        User user = userWithId(1L, "Chen", "chen@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("chen@test.com")).thenReturn(Optional.of(user));

        UserResponseDto result = userService.updateUser(1L, request("Chen2", "chen@test.com"));

        assertEquals("Chen2", result.getName());
    }

    @Test
    void updateUser_emailUsedByAnotherUserReturns409() {
        User me = userWithId(1L, "Me", "me@test.com");
        User other = userWithId(2L, "Other", "other@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(other));

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, request("Me", "other@test.com")));

        assertEquals(409, e.getStatusCode().value());
        assertEquals("me@test.com", me.getEmail()); // 변경되지 않아야 한다
    }

    @Test
    void updateUser_notFoundThrowsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(99L, request("A", "a@test.com")));
        verify(userRepository, never()).findByEmail(any());
    }

    // ---------- 삭제 ----------

    @Test
    void deleteUser_success() {
        User user = userWithId(1L, "Chen", "chen@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_notFoundThrowsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).delete(any(User.class));
    }
}
