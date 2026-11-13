package com.cashflow.backend.user;

import com.cashflow.backend.exception.GlobalExceptionHandler;
import com.cashflow.backend.exception.ResourceNotFoundException;
import com.cashflow.backend.user.controller.UserController;
import com.cashflow.backend.user.dto.UserResponseDto;
import com.cashflow.backend.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController API 테스트 (스프링 전체/DB 없이 MockMvc standalone 방식)
 * - UserService는 Mockito mock으로 대체
 * - 입력값 검증(@Valid)과 GlobalExceptionHandler의 응답 형식을 확인한다
 */
class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- POST /api/users ----------

    @Test
    void createUser_returns201() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(new UserResponseDto(1L, "Chen", "chen@test.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Chen\",\"email\":\"chen@test.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chen"))
                .andExpect(jsonPath("$.email").value("chen@test.com"));
    }

    @Test
    void createUser_blankNameReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"chen@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createUser_invalidEmailReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Chen\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createUser_missingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_duplicateEmailReturns409() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Chen\",\"email\":\"chen@test.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    // ---------- GET /api/users/{id} ----------

    @Test
    void getUser_returns200() throws Exception {
        when(userService.getUser(1L)).thenReturn(new UserResponseDto(1L, "Chen", "chen@test.com"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("chen@test.com"));
    }

    @Test
    void getUser_notFoundReturns404() throws Exception {
        when(userService.getUser(99L))
                .thenThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
    }

    // ---------- PUT /api/users/{id} ----------

    @Test
    void updateUser_returns200() throws Exception {
        when(userService.updateUser(eq(1L), any()))
                .thenReturn(new UserResponseDto(1L, "New", "new@test.com"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"email\":\"new@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void updateUser_invalidInputReturns400() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_notFoundReturns404() throws Exception {
        when(userService.updateUser(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"a@test.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_emailConflictReturns409() throws Exception {
        when(userService.updateUser(eq(1L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"other@test.com\"}"))
                .andExpect(status().isConflict());
    }

    // ---------- DELETE /api/users/{id} ----------

    @Test
    void deleteUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_notFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
