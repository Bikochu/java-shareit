package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

public interface UserService {
    List<UserDto> getUsers();

    UserDto addUser(@Valid UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    UserDto findUserById(Long userId);

    void deleteUser(Long userId);
}
