
package com.cashflow.backend.user.service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.cashflow.backend.user.User;
import com.cashflow.backend.user.UserRepository;
import com.cashflow.backend.user.dto.UserRequestDto;
import com.cashflow.backend.user.dto.UserResponseDto;
import com.cashflow.backend.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 1. 사용자 등록
    public UserResponseDto createUser(UserRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 이메일입니다."
            );
        }

        User user = new User(
                request.getName(),
                request.getEmail()
        );

        User savedUser = userRepository.save(user);

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }

    // 2. 사용자 조회
    public UserResponseDto getUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("사용자를 찾을 수 없습니다.")
                );

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    // 3. 사용자 수정
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("사용자를 찾을 수 없습니다.")
                );

        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "이미 사용 중인 이메일입니다."
                    );
                });

        user.update(request.getName(), request.getEmail());

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    // 4. 사용자 삭제
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("사용자를 찾을 수 없습니다.")
                );

        userRepository.delete(user);
    }
}
