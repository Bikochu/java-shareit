package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundUserException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private static Long id = 0L;
    public final Map<Long, UserDto> users = new HashMap<>();

    @Override
    public List<UserDto> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public UserDto addUser(@Valid UserDto userDto) {
        if (users.containsValue(userDto)) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", userDto.getEmail()));
        }
        id++;
        userDto.setId(id);
        UserDto userNew = new UserDto(userDto.getId(), userDto.getName(), userDto.getEmail());
        users.put(id, userNew);
        return userNew;
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        UserDto updatedUser = findUserById(userId);
        String name = userDto.getName();
        String email = userDto.getEmail();
        updatedUser.setName(name != null && !name.isBlank() ? name : updatedUser.getName());
        if (email != null && !email.isBlank()) {
            boolean emailExist = users.values().stream()
                    .filter(user -> !user.getId().equals(userId))
                    .anyMatch(user -> email.equals(user.getEmail()));
            if (emailExist) {
                throw new IllegalArgumentException(String.format("User with email %s already exists", email));
            }
            updatedUser.setEmail(email);
        }
        return updatedUser;
    }

    @Override
    public UserDto findUserById(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundUserException("User not found.");
        }
        return users.get(userId);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundUserException("User not found.");
        }
        users.remove(userId);
    }
}
