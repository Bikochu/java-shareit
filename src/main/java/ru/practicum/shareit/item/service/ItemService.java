package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final UserService userService;
    private static Long id = 0L;
    private final List<Item> items = new ArrayList<>();

    @Autowired
    public ItemService(UserService userService) {
        this.userService = userService;
    }

    public Item addItem(Long userId, Item item) {
        item.setOwner(userService.findUserById(userId));
        id++;
        item.setId(id);
        items.add(item);
        return item;
    }

    public List<Item> getItemsByUser(Long userId) {
        userService.findUserById(userId);
        return items.stream()
                .filter(user -> user.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    public Item updateItem(Long userId, Long itemId, Item item) {
        userService.findUserById(userId);
        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();
        Item updatedItem = findItemById(userId, itemId);
        if (!updatedItem.getOwner().getId().equals(userId)) {
            throw new NotFoundItemException("Item not found.");
        }
        updatedItem.setName(name != null && !name.isBlank() ? name : updatedItem.getName());
        updatedItem.setDescription(description != null && !description.isBlank() ? description : updatedItem.getDescription());
        updatedItem.setAvailable(available != null ? available : updatedItem.getAvailable());
        return updatedItem;
    }

    public Item findItemById(Long userId, Long itemId) {
        userService.findUserById(userId);
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundItemException("Item not found."));
    }

    public List<Item> searchItems(Long userId, String text) {
        userService.findUserById(userId);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return items.stream()
                .filter(item -> item.isAvailable() &&
                        (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
    }
}
