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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestControllerTest {

    static final String header = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RequestService requestService;

    static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    void getAllOwnRequests() throws Exception {
        Long userId = 1L;
        UserDto requester = new UserDto();
        List<ItemDto> items = new ArrayList<>();
        List<ItemRequestDto> itemRequestDtoList = Arrays.asList(
                new ItemRequestDto(1L, "Need thing for food.", requester, null, items),
                new ItemRequestDto(2L, "Need thing for soup.", requester, null, items));

        when(requestService.getAllOwnRequests(userId)).thenReturn(itemRequestDtoList);

        mockMvc.perform(get("/requests").header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Need thing for food."))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Need thing for soup."));
    }

    @Test
    void getAllOthersRequests() throws Exception {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;
        UserDto requester = new UserDto();
        List<ItemDto> items = new ArrayList<>();
        List<ItemRequestDto> itemRequestDtoList = Arrays.asList(
                new ItemRequestDto(1L, "Need thing for food.", requester, null, items),
                new ItemRequestDto(2L, "Need thing for soup.", requester, null, items));

        when(requestService.getAllOthersRequests(userId, from, size)).thenReturn(itemRequestDtoList);

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Need thing for food."))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Need thing for soup."));
    }

    @Test
    void getRequest() throws Exception {
        Long userId = 1L;
        Long requestId = 1L;
        UserDto requester = new UserDto();
        List<ItemDto> items = new ArrayList<>();
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Need thing for food.", requester, null, items);

        when(requestService.findItemRequestById(userId, requestId)).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need thing for food."));
    }

    @Test
    void addRequest() throws Exception {
        Long userId = 1L;
        UserDto requester = new UserDto();
        List<ItemDto> items = new ArrayList<>();
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Need thing for food.", requester, null, items);

        when(requestService.addRequest(userId, itemRequestDto)).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need thing for food."));
    }
}