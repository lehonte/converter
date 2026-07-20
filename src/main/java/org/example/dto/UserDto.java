package org.example.dto;

import lombok.Builder;

@Builder
public record UserDto(String username, String role, String token) {
}
