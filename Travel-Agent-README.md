# AI Travel Agent

An AI-powered conversational Travel Agent built with **Spring Boot 4.1.0**, **Spring AI 2.0.0**, **Groq**, and **GPT-OSS-120B**.

The application uses an LLM-driven tool-calling workflow to understand natural-language travel requests and invoke backend capabilities such as flight search, hotel search, weather lookup, inventory operations, and order operations.

The frontend is a separate **HTML/CSS/JavaScript** application that communicates with the Spring Boot backend through a REST API.

---

## 🌟 Features

- **Conversational AI Travel Agent**
  - Accepts natural-language travel requests.
  - Supports contextual follow-up questions.

- **LLM Tool Calling**
  - Backend capabilities are exposed as tools using Spring AI.
  - The LLM decides which tool(s) are relevant to the user's request.
  - Spring AI executes the selected registered Java tools and returns their results to the LLM.

- **Multi-Tool Workflows**
  - Flight search
  - Hotel search
  - Weather information
  - Inventory operations
  - Order operations

- **Conversation Memory**
  - Uses `MessageWindowChatMemory`.
  - Conversations are associated with a `Conversation-Id`.
  - Follow-up questions can therefore use previous conversation context.

- **Separate Frontend and Backend**
  - Frontend runs independently, typically through VS Code Live Server on port `5500`.
  - Spring Boot backend runs on port `8081`.
  - Frontend communicates with `POST /chat`.

- **Secure API-Key Handling**
  - Groq credentials remain on the backend.
  - API keys are supplied through configuration/environment variables rather than frontend JavaScript.

---

## 🏗️ Architecture

The project has three main layers:

```text
┌──────────────────────────────────────┐
│            FRONTEND                  │
│        HTML / CSS / JavaScript       │
│             :5500                    │
└──────────────────┬───────────────────┘
                   │
                   │ HTTP POST /chat
                   ▼
┌──────────────────────────────────────┐
│           SPRING BOOT                │
│              :8081                   │
│                                      │
│        ChatController                │
│              ↓                       │
│          ChatService                 │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│             SPRING AI                │
│                                      │
│            ChatClient                │
│                                      │
│   ┌──────────────────────────────┐   │
│   │ MessageWindowChatMemory      │   │
│   │ Conversation-Id              │   │
│   └──────────────────────────────┘   │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│              GROQ                    │
│                                      │
│          GPT-OSS-120B                │
└──────────────────┬───────────────────┘
                   │
                   │ Tool-call request
                   ▼
┌──────────────────────────────────────┐
│          SPRING AI TOOLS             │
│                                      │
│ Flight │ Hotel │ Weather │ Order │   │
│ Inventory                            │
└──────────────────┬───────────────────┘
                   │
                   ▼
             Tool execution
                   │
                   ▼
             Tool results
                   │
                   ▼
             GPT-OSS-120B
                   │
                   ▼
             Final response
                   │
                   ▼
               Frontend
```

### Spring MVC request lifecycle

When the browser calls `/chat`, the HTTP request enters the embedded Tomcat server and is processed by Spring MVC:

```text
Browser
  │
  │ POST /chat
  ▼
Embedded Tomcat
  │
  ▼
DispatcherServlet
  │
  ▼
HandlerMapping
  │
  │ Finds the matching controller method
  ▼
HandlerAdapter
  │
  ▼
ChatController
  │
  ▼
ChatService
  │
  ▼
Spring AI ChatClient
```

`HandlerAdapter` is shown here for completeness; it is normally not necessary to mention it in a high-level project explanation.

---

## 🔄 End-to-End Agent Flow

For a request such as:

> **"Plan a trip from Delhi to Goa on 2026-08-21. My total budget for flight + one night hotel is ₹7000. What should I book, and what's the weather going to be like?"**

the conceptual flow is:

```text
1. User enters query
          ↓
2. Frontend sends POST /chat
          ↓
3. Tomcat receives HTTP request
          ↓
4. DispatcherServlet processes request
          ↓
5. HandlerMapping finds ChatController
          ↓
6. Controller delegates to ChatService
          ↓
7. ChatService invokes Spring AI ChatClient
          ↓
8. ChatClient sends user request + available tool definitions
   to GPT-OSS-120B through Groq
          ↓
9. LLM understands the user's intent
          ↓
10. LLM generates tool-call request(s)
          ↓
11. Spring AI executes the registered Java tool(s)
          ↓
12. Tools retrieve/produce the required data
          ↓
13. Tool results are returned to the LLM
          ↓
14. LLM combines the results with the user's requirements
    (destination, date, budget, etc.)
          ↓
15. LLM generates final natural-language response
          ↓
16. Response returns through ChatService
          ↓
17. Controller returns HTTP response
          ↓
18. Frontend displays the response
```

### Important distinction

The LLM **does not execute the Java tool itself**.

The LLM generates a structured tool-call request such as:

```text
Tool: searchFlight
Arguments:
    source = Delhi
    destination = Goa
    date = 2026-08-21
```

Spring AI receives that request and executes the corresponding registered Java tool.

The result is then sent back to the LLM so it can produce the final response.

---

## 🧠 Role of Spring AI

Spring AI is the **AI integration and orchestration layer** inside the Spring Boot application.

It provides Spring-friendly abstractions for:

- `ChatClient`
- LLM integration
- Tool calling
- Conversation memory
- Advisors and other AI application capabilities

Without a framework abstraction, the application would need to manually handle much of the model interaction and tool-calling workflow.

The conceptual responsibility is:

```text
Spring Boot Application
          │
          ▼
      Spring AI
      /            ▼         ▼
   LLM        Tools
     \         /
      \       /
       ▼     ▼
     AI Workflow
          │
          ▼
   Final Response
```

Spring AI therefore is not the LLM and it is not the Groq inference service.

```text
Spring AI
    ↓
Groq API
    ↓
GPT-OSS-120B
```

---

## 🤖 Tool Calling

The available Java methods are exposed to the LLM as tools.

Conceptually, a tool might look like:

```java
@Tool(description = "Search flights between two cities for a given date")
public Flight searchFlight(
        String source,
        String destination,
        String date) {
    // tool implementation
}
```

The LLM receives the tool's name, description, and input schema.

For example:

```text
Available tool:
searchFlight

Description:
Search flights between two cities for a given date.

Parameters:
source
destination
date
```

The model can then decide whether that capability is required for the user's request.

---

## 🧩 Registered Tools

| Tool | Purpose |
|---|---|
| Flight Tool | Search flight options |
| Hotel Tool | Search hotel options |
| Weather Tool | Retrieve weather information |
| Order Tool | Perform order-related operations |
| Inventory Tool | Check inventory/product availability |

The exact implementation and data source for each tool are defined in the backend Java code.

---

## 💬 Conversation Memory

The application uses:

```text
MessageWindowChatMemory
```

and associates messages with a conversation identifier.

Example:

```text
User:
I want to travel to Goa.

AI:
Sure.

User:
What about hotels?

AI:
Here are hotel options in Goa...
```

The second message can be interpreted using the context of the same conversation.

The conversation identifier acts as the key that allows the application to associate multiple turns with the same conversation.

---

## 🔐 Configuration and Security

The Groq API key should be stored outside the source code, for example as an environment variable.

Do not expose the API key in:

- Frontend JavaScript
- GitHub repositories
- Hardcoded Java strings
- Public configuration files

The intended communication path is:

```text
Frontend :5500
      │
      ▼
Spring Boot :8081
      │
      ▼
Groq API
```

This keeps the provider credential on the backend.

---

## 🌐 CORS

Because the frontend and backend run on different ports:

```text
Frontend → localhost:5500
Backend  → localhost:8081
```

they have different origins from the browser's perspective.

The backend therefore allows the frontend origin through CORS.

For the local development setup, the controller can allow the frontend origin, for example:

```java
@CrossOrigin(origins = "http://localhost:5500")
```

---

## 🛠️ Tech Stack

### Backend

- Java
- Spring Boot 4.1.0
- Spring Web
- Spring AI 2.0.0
- Maven

### AI

- Groq API
- `openai/gpt-oss-120b`
- Spring AI `ChatClient`
- Spring AI Tool Calling
- `MessageWindowChatMemory`

### Frontend

- HTML
- CSS
- JavaScript
- VS Code Live Server

---

## 📂 Project Structure

The logical structure of the project is:

```text
Travel-Agent/
│
├── README.md
├── travel-agent-frontend.html
│
└── backend/
    │
    ├── pom.xml
    │
    └── src/
        └── main/
            ├── java/
            │   └── ...
            │       ├── controller/
            │       │   └── ChatController.java
            │       │
            │       ├── service/
            │       │   └── ChatService.java
            │       │
            │       ├── tools/
            │       │   ├── FlightTools.java
            │       │   ├── HotelTools.java
            │       │   ├── WeatherTools.java
            │       │   ├── OrderTools.java
            │       │   └── InventoryTools.java
            │       │
            │       └── config/
            │           └── ...
            │
            └── resources/
                └── application.properties
```

Adjust the package/file names above to match the final repository structure if they differ.

---

## ⚙️ Prerequisites

- Java 21+
- Maven
- Groq API key
- VS Code
- Live Server extension for the frontend

---

## 🚀 Running the Backend

From the Spring Boot backend directory:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8081
```

---

## 🌐 Running the Frontend

Open:

```text
travel-agent-frontend.html
```

in VS Code and run it using the Live Server extension.

The frontend will typically be available at:

```text
http://localhost:5500
```

It communicates with:

```text
http://localhost:8081/chat
```

---

## 💡 Example Prompts

### Travel planning

```text
Plan a trip from Delhi to Goa on 2026-08-21.
My total budget for flight + one night hotel is ₹7000.
What should I book, and what's the weather going to be like?
```

### Flight search

```text
Find flights from Delhi to Goa on 2026-08-21.
```

### Hotel search

```text
Find me a hotel in Goa for one night within my budget.
```

### Weather

```text
What's the weather going to be like in Goa?
```

### Follow-up

```text
What about a cheaper hotel?
```

The last request can use the existing conversation context instead of requiring the user to repeat the destination.

---

## 🎯 Project Objective

The project demonstrates how a conventional Spring Boot application can be extended into an **LLM-powered agentic application**.

Instead of hardcoding routing logic such as:

```java
if (message.contains("flight")) {
    // call flight functionality
}
```

the application exposes backend capabilities as tools and allows the LLM to determine which capability is relevant to a natural-language request.

This enables conversational, multi-tool workflows while keeping actual tool execution under application control.

---

## 🔮 Future Improvements

- Authentication and authorization
- Persistent conversation storage
- Redis-backed shared chat memory
- Real flight/hotel booking integrations
- Payment integration
- Streaming responses
- Rate limiting
- Centralized secret management
- Monitoring and observability
- Production-grade validation and exception handling
- Docker/cloud deployment

---

## 👨‍💻 Author

**Nikhil Sah**

A learning project focused on **Spring AI, LLM-powered agents, tool calling, conversational memory, and full-stack AI application development**.
