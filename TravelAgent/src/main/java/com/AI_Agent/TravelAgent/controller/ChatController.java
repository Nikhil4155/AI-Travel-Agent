package com.AI_Agent.TravelAgent.controller;

import com.AI_Agent.TravelAgent.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500"
})
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<String> chatGet(@RequestHeader("Conversation-Id") String conversationId,
                                          @RequestBody String message) {
        return ResponseEntity.ok(chatService.chat(message, conversationId));
    }

}
