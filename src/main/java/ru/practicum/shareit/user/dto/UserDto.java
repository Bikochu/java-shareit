package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
public class UserDto {
    private Long id;

    @NotNull(message = "User name can't be Null")
    @NotBlank(message = "User name can't be Blank")
    private String name;

    @NotNull(message = "User email can't be Null")
    @NotBlank(message = "user email can't be Blank")
    @Email(message = "Invalid email format")
    private String email;
}
