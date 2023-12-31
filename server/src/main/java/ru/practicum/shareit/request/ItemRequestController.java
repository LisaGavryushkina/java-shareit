package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestBody ItemRequestDto requestDto,
                                     @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemRequestService.addRequest(requestDto, userId, LocalDateTime.now());
    }

    @GetMapping
    public List<ItemRequestDto> findUserRequests(@RequestHeader("X-Sharer-User-Id") int userId) {
        return itemRequestService.findUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllRequests(@RequestParam int from, @RequestParam int size,
                                                @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemRequestService.findAllRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findRequestById(@PathVariable int requestId, @RequestHeader("X-Sharer-User-Id") int userId) {
        return itemRequestService.findRequestById(requestId, userId);
    }
}
