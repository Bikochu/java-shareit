package ru.practicum.shareit.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class RequestServiceImplTest {

    @Mock
    UserService userService;

    @Mock
    RequestRepository requestRepository;

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    RequestServiceImpl requestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestService = new RequestServiceImpl(userService, requestRepository, itemRepository);
    }

    @Test
    void addRequest() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        Long userId = 1L;
        Long itemRequestId = 1L;
        UserDto requester = new UserDto();
        requester.setId(userId);
        ItemRequestDto itemRequestDto = new ItemRequestDto(itemRequestId, "Need item for eat.", requester, now, new ArrayList<>());

        when(userService.findUserById(userId)).thenReturn(requester);
        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(request -> request.getArgument(0));

        //Тестируем.
        ItemRequestDto result = requestService.addRequest(userId, itemRequestDto);

        //Проверяем.
        assertEquals(itemRequestDto, result);
        verify(userService, times(1)).findUserById(anyLong());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void addRequest_WithNoDescription() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        Long userId = 1L;
        Long itemRequestId = 1L;
        UserDto requester = new UserDto();
        requester.setId(userId);
        ItemRequestDto itemRequestDto = new ItemRequestDto(itemRequestId, null, requester, now, new ArrayList<>());

        //Тестируем и проверяем.
        assertThrows(ResponseStatusException.class, () -> requestService.addRequest(userId, itemRequestDto));
    }

    @Test
    void getAllOwnRequests() {
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        Long userId = 1L;
        User requester = new User();

        List<ItemRequest> itemRequests = new ArrayList<>();
        ItemRequest itemRequest = new ItemRequest(1L, "Need item for eat.", requester, now);
        itemRequests.add(itemRequest);
        List<ItemRequestDto> itemRequestDtoList = itemRequests.stream().map(RequestMapper::toItemRequestDto).collect(Collectors.toList());

        when(requestRepository.findAllByRequesterIdOrderByCreatedAsc(userId))
                .thenReturn(itemRequests);

        //Тестируем.
        List<ItemRequestDto> result = requestService.getAllOwnRequests(userId);

        //Проверяем.
        assertEquals(itemRequestDtoList, result);
        verify(requestRepository, times(1)).findAllByRequesterIdOrderByCreatedAsc(anyLong());
    }

    @Test
    void getAllOthersRequests_WithPages() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        Long userId = 1L;
        User requester = new User();
        ItemRequest itemRequest = new ItemRequest(1L, "Need item for eat.", requester, now);
        List<ItemRequest> itemRequests = new ArrayList<>();
        itemRequests.add(itemRequest);
        Page<ItemRequest> page = new PageImpl<>(itemRequests); // Пустая страница с пустым списком

        List<ItemRequestDto> itemRequestDtoList = page.stream().map(RequestMapper::toItemRequestDto).collect(Collectors.toList());

        when(requestRepository.findByRequesterIdNot(userId, PageRequest.of(0, 10)))
                .thenReturn(page);
        //Тестируем.
        List<ItemRequestDto> result = requestService.getAllOthersRequests(userId, 0, 10);

        //Проверяем.
        assertEquals(itemRequestDtoList, result);
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
    }

    @Test
    void findItemRequestById() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        Long userId = 1L;
        Long requestId = 1L;
        User requester = new User();
        requester.setId(userId);
        UserDto userDto = new UserDto();
        userDto.setId(userId);

        ItemRequest itemRequest = new ItemRequest(1L, "Need item for eat.", requester, now);
        List<Item> items = new ArrayList<>();
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(itemRepository.findAllByRequestId(requestId)).thenReturn(items);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));

        //Тестируем.
        ItemRequestDto result = requestService.findItemRequestById(userId, requestId);

        //Проверяем.
        assertEquals(itemRequestDto, result);

    }
}