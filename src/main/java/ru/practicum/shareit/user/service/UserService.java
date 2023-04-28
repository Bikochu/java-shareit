package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundUserException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private static Long id = 0L;
    public final List<User> users = new ArrayList<>();

    public User addUser(User user) {
        if (users.contains(user)) {
            throw new IllegalArgumentException(String.format("User with email %s already exists", user.getEmail()));
        }
        id++;
        user.setId(id);
        User userNew = new User(user.getId(), user.getName(), user.getEmail());
        users.add(userNew);
        return userNew;
    }

    public List<User> getUsers() {
        return users;
    }

    public User updateUser(Long userId, UserDto userDto) {
        User updatedUser = findUserById(userId);
        String name = userDto.getName();
        String email = userDto.getEmail();
        updatedUser.setName(name != null && !name.isBlank() ? name : updatedUser.getName());
        if (email != null && !email.isBlank()) {
            boolean emailExist = users.stream()
                    .filter(user -> !user.getId().equals(userId))
                    .anyMatch(user -> email.equals(user.getEmail()));
            if (emailExist) {
                throw new IllegalArgumentException(String.format("User with email %s already exists", email));
            }
            updatedUser.setEmail(email);
        }
        return updatedUser;
    }

    public User findUserById(Long userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundUserException("User not found."));
    }

    public void deleteUser(Long userId) {
        users.remove(users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundUserException("User not found.")));
    }

    /*public Long makeId() {
        Long endId = users.stream().map(User::getId).max(Comparator.naturalOrder()).orElse(0L);
        return endId + 1;
    }*/
}
