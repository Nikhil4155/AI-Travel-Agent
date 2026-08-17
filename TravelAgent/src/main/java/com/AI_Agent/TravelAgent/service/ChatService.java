package com.AI_Agent.TravelAgent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    public String chat(String query, String conversationId) {

        try {
            System.out.println("USER QUERY: " + query);
            System.out.println("CONVERSATION ID: " + conversationId);

            String response = chatClient
                    .prompt()
                    .user(query)
                    .advisors(a -> a.param(
                            ChatMemory.CONVERSATION_ID,
                            conversationId))
                    .call()
                    .content();

            System.out.println("AI RESPONSE: " + response);

            return response;

        } catch (Exception e) {
            System.out.println("========== AI ERROR ==========");
            e.printStackTrace();
            System.out.println("==============================");

            throw e;
        }
    }
}
