package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithDate;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemControllerTest {

    static final String header = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemService itemService;

    static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    void getItemsByUser() throws Exception {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        UserDto userDto = new UserDto(1L, "Ivan", "ivan@bik.com");

        List<ItemDtoWithDate> items = Arrays.asList(
                new ItemDtoWithDate(1L, "Fork", "Thing for eat", true, userDto, null, null, null, null),
                new ItemDtoWithDate(2L, "Spoon", "Thing for soup", true, userDto, null, null, null, null)
        );

        when(itemService.getItemsByUser(userId, from, size)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(header, userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Fork"))
                .andExpect(jsonPath("$[0].description").value("Thing for eat"))
                .andExpect(jsonPath("$[1].name").value("Spoon"))
                .andExpect(jsonPath("$[1].description").value("Thing for soup"));
    }

    @Test
    void findItemById() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        UserDto userDto = new UserDto(1L, "Ivan", "ivan@bik.com");
        ItemDtoWithDate item = new ItemDtoWithDate(itemId, "Fork", "Thing for eat", true, userDto, null, null, null, null);

        when(itemService.findItemById(userId, itemId)).thenReturn(item);

        mockMvc.perform(get("/items/1")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fork"));
    }

    @Test
    void searchItems() throws Exception {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        UserDto userDto = new UserDto(1L, "Ivan", "ivan@bik.com");
        List<ItemDto> items = Arrays.asList(
                new ItemDto(1L, "Fork", "Thing for eat", true, userDto, null, null),
                new ItemDto(2L, "Spoon", "Thing for soup", true, userDto, null, null)
        );
        when(itemService.searchItems(userId, "Thing", from, size)).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", "Thing")
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Fork"))
                .andExpect(jsonPath("$[0].description").value("Thing for eat"))
                .andExpect(jsonPath("$[1].name").value("Spoon"))
                .andExpect(jsonPath("$[1].description").value("Thing for soup"));
    }

    @Test
    void addItem() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        UserDto userDto = new UserDto(1L, "Ivan", "ivan@bik.com");
        ItemDto item = new ItemDto(itemId, "Fork", "Thing for eat", true, userDto, null, null);

        when(itemService.addItem(userId, item)).thenReturn(item);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fork"));
    }

    @Test
    void updateItem() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        UserDto userDto = new UserDto(1L, "Ivan", "ivan@bik.com");
        ItemDto item1 = new ItemDto(itemId, "Fork", "Thing for eat", true, userDto, null, null);
        ItemDto item2 = new ItemDto(itemId, "Spoon", "Thing for soup", true, userDto, null, null);

        when(itemService.addItem(userId, item1)).thenReturn(item1);
        when(itemService.updateItem(userId, itemId, item2)).thenReturn(item2);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item1)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/items/1")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Spoon"));
    }

    @Test
    void addComment() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        UserDto userDto = new UserDto(1L, "Ivan", "ivan@bik.com");
        ItemDto item = new ItemDto(itemId, "Fork", "Thing for eat", true, userDto, null, null);
        CommentDto comment = new CommentDto(1L, "Nice fork", item, "Svetlana", null);

        when(itemService.addComment(userId, itemId, comment)).thenReturn(comment);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(comment)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Nice fork"));
    }
}