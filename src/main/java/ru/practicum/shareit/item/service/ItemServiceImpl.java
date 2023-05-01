package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundItemException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserServiceImpl userServiceImpl;
    private Long id = 0L;
    private final Map<Long, ItemDto> items = new HashMap<>();


    @Override
    public List<ItemDto> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        itemDto.setOwnerId(userServiceImpl.findUserById(userId).getId());
        id++;
        itemDto.setId(id);
        items.put(itemDto.getId(), itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> getItemsByUser(Long userId) {
        userServiceImpl.findUserById(userId);
        return items.values().stream()
                .filter(user -> user.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userServiceImpl.findUserById(userId);
        String name = itemDto.getName();
        String description = itemDto.getDescription();
        Boolean available = itemDto.getAvailable();
        ItemDto updatedItem = findItemById(userId, itemId);
        if (!updatedItem.getOwnerId().equals(userId)) {
            throw new NotFoundItemException("Item not found.");
        }
        updatedItem.setName(name != null && !name.isBlank() ? name : updatedItem.getName());
        updatedItem.setDescription(description != null && !description.isBlank() ? description : updatedItem.getDescription());
        updatedItem.setAvailable(available != null ? available : updatedItem.getAvailable());
        return updatedItem;
    }

    @Override
    public ItemDto findItemById(Long userId, Long itemId) {
        userServiceImpl.findUserById(userId);
        if (!items.containsKey(itemId)) {
            throw new NotFoundItemException("Item not found.");
        }
        return items.get(itemId);
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text) {
        userServiceImpl.findUserById(userId);
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.isAvailable() &&
                        (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .collect(Collectors.toList());
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        findItemById(userId, itemId);
        items.remove(itemId);
    }
}
