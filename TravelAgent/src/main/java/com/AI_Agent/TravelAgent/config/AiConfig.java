//package com.AI_Agent.TravelAgent.config;
//
//import com.AI_Agent.TravelAgent.tools.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class AiConfig {
//
//    private final OrderTools orderTools;
//    private final InventoryTools inventoryTools;
//    private final FlightTools flightTools;
//    private final HotelTools hotelTools;
//    private final WeatherTools weatherTools;
//
//    @Bean
//    public ChatMemory chatMemory() {
//        return MessageWindowChatMemory
//                .builder()
//                .maxMessages(10)
//                .build();
//    }
//
//    @Bean
//    public ChatClient chatClient(
//            ChatClient.Builder builder) {
//        return builder
//                .defaultSystem(
//                        """
//You are a helpful travel planning and customer support agent.
//
//Use the available tools whenever they are required.
//
//For customer orders:
//- Use getOrderCount when the user asks how many orders they have.
//- Use getOrderStatus when the user provides an order ID and asks about its status.
//- Use cancelOrder when the user asks to cancel an order.
//
//For travel:
//- Use the appropriate flight, hotel, and weather tools.
//
//     """)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory()).build())
//                .defaultTools(orderTools, inventoryTools, flightTools, hotelTools, weatherTools)
//                .build();
//    }
//
//}

package com.AI_Agent.TravelAgent.config;

import com.AI_Agent.TravelAgent.tools.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final OrderTools orderTools;
    private final InventoryTools inventoryTools;
    private final FlightTools flightTools;
    private final HotelTools hotelTools;
    private final WeatherTools weatherTools;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory
                .builder()
                .maxMessages(10)
                .build();
    }

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder) {

        return builder
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .extraBody(
                                        Map.of(
                                                "include_reasoning", false
                                        )
                                )
                )
                .defaultSystem(
                        """
                        You are a helpful travel planning and customer support agent.

                        Use the available tools whenever they are required.

                        For customer orders:
                        - Use getOrderCount when the user asks how many orders they have.
                        - Use getOrderStatus when the user provides an order ID and asks about its status.
                        - Use cancelOrder when the user asks to cancel an order.

                        For travel:
                        - Use the appropriate flight, hotel, and weather tools.
                        """
                )
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultTools(
                        orderTools,
                        inventoryTools,
                        flightTools,
                        hotelTools,
                        weatherTools
                )
                .build();
    }
}