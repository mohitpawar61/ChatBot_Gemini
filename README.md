# 🤖 ChatBot Gemini

A real-time AI chatbot backend built with **Spring Boot 4.1** and **Java 21**, powered by **Google Gemini 2.5 Flash Lite**. Communication happens over a persistent **WebSocket** connection — no polling, no REST calls per message — giving users an instant, live chat experience.

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [How It Works — End to End](#-how-it-works--end-to-end)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [WebSocket Endpoint](#-websocket-endpoint)
- [Gemini API Integration](#-gemini-api-integration)
- [Frontend Integration](#-frontend-integration)
- [Error Handling](#-error-handling)
- [Author](#-author)

---

## 📖 Overview

**ChatBot Gemini** is a WebSocket-based backend that connects your frontend chat UI to **Google's Gemini AI** (`gemini-2.5-flash-lite`). The client opens a single persistent WebSocket connection to `/chat`, sends messages as plain text, and receives Gemini's AI responses back in real time — all without HTTP overhead per message.

The backend calls Google's `generativelanguage.googleapis.com` REST API, parses the structured JSON response, and sends the extracted text back through the WebSocket to the client.

---

## ✨ Features

- ⚡ Real-time bidirectional chat over **WebSocket** (`ws://`)
- 🧠 Powered by **Google Gemini 2.5 Flash Lite** — fast, capable, cost-efficient
- 🌐 CORS open to all origins — ready for any frontend on any port
- 🔧 Spring `RestClient` for clean, fluent HTTP calls to Gemini API
- 📦 Minimal dependencies — just WebSocket + WebMVC + Jackson
- 🛡 Graceful error handling — errors returned as messages, not broken connections
- 🔑 API key injected via environment variable — no hardcoded secrets

---

## 🛠 Tech Stack

| Layer              | Technology                                      |
|--------------------|-------------------------------------------------|
| Language           | Java 21                                         |
| Framework          | Spring Boot 4.1                                 |
| Communication      | Spring WebSocket (`spring-boot-starter-websocket`) |
| HTTP Client        | Spring `RestClient` (built-in, Spring 6+)       |
| AI Model           | Google Gemini 2.5 Flash Lite                    |
| Gemini API         | `generativelanguage.googleapis.com/v1beta`      |
| JSON Parsing       | Jackson `JsonNode`                              |
| Web                | Spring MVC (`spring-boot-starter-webmvc`)       |
| Build Tool         | Maven                                           |

---

## 🏗 Architecture

```
Frontend (Browser / Mobile)
         │
         │  ws://localhost:8080/chat
         │  (WebSocket — persistent connection)
         │
         ▼
┌──────────────────────────────┐
│     WebSocketConfig           │  @EnableWebSocket
│     Registers /chat handler   │  AllowedOrigins: *
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│   GeminiWebSocketHandler      │  extends TextWebSocketHandler
│                               │
│   handleTextMessage()         │
│   ├─ Validate message         │
│   ├─ Call GeminiService       │
│   └─ Send response back       │
│      via session.sendMessage()│
└──────────────┬───────────────┘
               │
┌──────────────▼───────────────┐
│       GeminiService           │
│                               │
│   generateAnswer(prompt)      │
│   ├─ Build Gemini API URL     │
│   │  with model + api key     │
│   ├─ POST request body:       │
│   │  { contents: [{           │
│   │    role: "user",          │
│   │    parts: [{text: ...}]   │
│   │  }] }                     │
│   ├─ RestClient.post()        │
│   └─ extractText(JsonNode)    │
│      → candidates[0]          │
│        .content.parts[].text  │
└──────────────┬───────────────┘
               │  HTTPS POST
               ▼
┌──────────────────────────────┐
│  Google Gemini API            │
│  generativelanguage           │
│  .googleapis.com/v1beta/      │
│  models/gemini-2.5-flash-lite │
│  :generateContent?key=...     │
└──────────────────────────────┘
```

---

## 🔄 How It Works — End to End

```
1. App starts → WebSocketConfig registers GeminiWebSocketHandler at ws://.../chat

2. Client connects:
   new WebSocket("ws://localhost:8080/chat")

3. Client sends a message:
   socket.send("What is a neural network?")

4. GeminiWebSocketHandler.handleTextMessage() fires
   │
   ├─ Validates the message is not blank
   │
   └─ Calls GeminiService.generateAnswer("What is a neural network?")

5. GeminiService builds the Gemini API URL:
   https://generativelanguage.googleapis.com/v1beta/models/
   gemini-2.5-flash-lite:generateContent?key=YOUR_API_KEY

6. GeminiService POSTs the request body:
   {
     "contents": [{
       "role": "user",
       "parts": [{ "text": "What is a neural network?" }]
     }]
   }

7. Gemini API responds:
   {
     "candidates": [{
       "content": {
         "parts": [{ "text": "A neural network is..." }],
         "role": "model"
       }
     }]
   }

8. extractText() traverses:
   candidates[0] → content → parts[] → text
   Concatenates all part texts into one String

9. GeminiWebSocketHandler sends the answer back:
   session.sendMessage(new TextMessage("A neural network is..."))

10. Client receives the message:
    socket.onmessage = (e) => console.log(e.data)
```

---

## 📁 Project Structure

```
ChatBot_Gemini/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/cfs/chatbot/
    │   │   ├── ChatBotApplication.java              # @SpringBootApplication entry point
    │   │   ├── config/
    │   │   │   ├── RestClientConfig.java            # Registers Spring RestClient bean
    │   │   │   └── WebSocketConfig.java             # @EnableWebSocket — maps /chat endpoint
    │   │   ├── controller/
    │   │   │   └── GeminiWebSocketHandler.java      # TextWebSocketHandler — receives messages,
    │   │   │                                        # calls service, sends back AI response
    │   │   └── service/
    │   │       └── GeminiService.java               # Calls Gemini REST API, parses JSON response
    │   └── resources/
    │       └── application.properties               # API key + model configuration
    └── test/
        └── java/com/cfs/chatbot/
            └── ChatBotApplicationTests.java         # Spring context load test
```

---

## 📋 Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/)
- **Google Gemini API Key** — [Get yours at Google AI Studio](https://aistudio.google.com/app/apikey)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/mohitpawar61/ChatBot_Gemini.git
cd ChatBot_Gemini
```

### 2. Set Your Gemini API Key

The API key is read from the environment variable `GEMINI_API_KEY`.

**Linux / macOS:**
```bash
export GEMINI_API_KEY=your_gemini_api_key_here
```

**Windows (Command Prompt):**
```cmd
set GEMINI_API_KEY=your_gemini_api_key_here
```

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your_gemini_api_key_here"
```

> ⚠️ **Never hardcode your API key in `application.properties` or commit it to Git.**

### 3. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

Or pass the key directly at runtime:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DGEMINI_API_KEY=your_key_here"
```

Or with the packaged JAR:
```bash
mvn clean package
java -DGEMINI_API_KEY=your_key_here -jar target/ChatBot-0.0.1-SNAPSHOT.jar
```

The application starts at: **`ws://localhost:8080/chat`**

---

## 🔧 Configuration

### `application.properties`

```properties
spring.application.name=ChatBot

# Gemini API key — loaded from GEMINI_API_KEY environment variable
gemini.api.key=${GEMINI_API_KEY}

# Gemini model to use
gemini.model=gemini-2.5-flash-lite
```

### Changing the Model

To switch to a more powerful Gemini model, update `application.properties`:

```properties
# Options:
gemini.model=gemini-2.5-flash-lite     # Fast, efficient (default)
gemini.model=gemini-2.5-flash          # Balanced speed + quality
gemini.model=gemini-2.5-pro            # Highest quality, complex reasoning
gemini.model=gemini-1.5-flash          # Previous generation, very fast
gemini.model=gemini-1.5-pro            # Previous generation, high quality
```

### Key Beans

| Bean / Class              | Purpose                                                          |
|---------------------------|------------------------------------------------------------------|
| `RestClient`              | Spring 6+ fluent HTTP client used to call the Gemini REST API   |
| `WebSocketConfig`         | Registers `GeminiWebSocketHandler` at path `/chat`, CORS `*`    |
| `GeminiWebSocketHandler`  | Handles incoming WebSocket messages, delegates to service        |
| `GeminiService`           | Builds Gemini API request, calls it, parses and returns text     |

---

## 🔌 WebSocket Endpoint

| Property        | Value                          |
|-----------------|--------------------------------|
| **Protocol**    | `ws://` (plain) / `wss://` (SSL)|
| **Path**        | `/chat`                        |
| **Full URL**    | `ws://localhost:8080/chat`     |
| **Allowed Origins** | `*` (all)                  |
| **Message format**  | Plain text (UTF-8 string)  |
| **Handler type**    | `TextWebSocketHandler`     |

### WebSocket Message Contract

**Client → Server:**
```
Plain text string — the user's question or message.
Example: "What is quantum entanglement?"
```

**Server → Client (success):**
```
Plain text string — Gemini's AI-generated answer.
Example: "Quantum entanglement is a phenomenon where two particles..."
```

**Server → Client (empty message):**
```
"Please send message."
```

**Server → Client (error):**
```
"Gemini request failed: <error detail>"
```

---

## 🧠 Gemini API Integration

### Endpoint Called

```
POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}
```

### Request Body

```json
{
  "contents": [
    {
      "role": "user",
      "parts": [
        { "text": "What is a neural network?" }
      ]
    }
  ]
}
```

### Gemini Response Structure

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          { "text": "A neural network is a computational model..." }
        ],
        "role": "model"
      },
      "finishReason": "STOP",
      "index": 0
    }
  ],
  "usageMetadata": {
    "promptTokenCount": 8,
    "candidatesTokenCount": 120,
    "totalTokenCount": 128
  }
}
```

### Response Parsing (`extractText`)

```
response
  └─ candidates[0]
       └─ content
            └─ parts[]
                 └─ text   ← concatenated into final answer string
```

The `extractText()` method iterates over all `parts` entries (Gemini can split long responses into multiple parts) and concatenates their `text` fields into one complete answer string.

---

## 🖥 Frontend Integration

### Vanilla JavaScript

```html
<!DOCTYPE html>
<html>
<head><title>ChatBot Gemini</title></head>
<body>
  <div id="chat"></div>
  <input id="input" type="text" placeholder="Ask anything..." />
  <button onclick="send()">Send</button>

  <script>
    const socket = new WebSocket("ws://localhost:8080/chat");

    // Receive AI response
    socket.onmessage = (event) => {
      const chat = document.getElementById("chat");
      chat.innerHTML += `<p><b>Gemini:</b> ${event.data}</p>`;
    };

    socket.onerror = (err) => console.error("WebSocket error:", err);
    socket.onclose = () => console.log("Connection closed");

    function send() {
      const input = document.getElementById("input");
      const msg = input.value.trim();
      if (!msg) return;

      // Show user message
      document.getElementById("chat").innerHTML +=
        `<p><b>You:</b> ${msg}</p>`;

      socket.send(msg);
      input.value = "";
    }

    // Send on Enter key
    document.getElementById("input")
      .addEventListener("keydown", (e) => {
        if (e.key === "Enter") send();
      });
  </script>
</body>
</html>
```

### React Hook Example

```jsx
import { useState, useEffect, useRef } from "react";

function useChatBot() {
  const [messages, setMessages] = useState([]);
  const socketRef = useRef(null);

  useEffect(() => {
    socketRef.current = new WebSocket("ws://localhost:8080/chat");

    socketRef.current.onmessage = (event) => {
      setMessages((prev) => [
        ...prev,
        { role: "gemini", text: event.data }
      ]);
    };

    return () => socketRef.current.close();
  }, []);

  const sendMessage = (text) => {
    setMessages((prev) => [...prev, { role: "user", text }]);
    socketRef.current.send(text);
  };

  return { messages, sendMessage };
}

// Usage in component:
// const { messages, sendMessage } = useChatBot();
```

### Testing with wscat (CLI)

```bash
# Install wscat
npm install -g wscat

# Connect and chat
wscat -c ws://localhost:8080/chat

# Then type any message and press Enter:
> What is artificial intelligence?
< Artificial intelligence (AI) refers to the simulation of human intelligence...
```

---

## 🛡 Error Handling

| Scenario                       | Behaviour                                                      |
|--------------------------------|----------------------------------------------------------------|
| Empty or blank message sent    | Sends back `"Please send message."` — connection stays open   |
| Gemini API call fails          | Sends back `"Gemini request failed: <cause>"` — no crash       |
| Invalid API key                | Gemini returns 400/403 → caught, error message sent to client  |
| Network timeout to Gemini      | Caught by `try/catch`, error message sent to client            |
| WebSocket connection drops     | Spring handles cleanup; client can reconnect to `/chat`        |

---

## 🔐 Security Notes

- **API Key** is read from the environment variable `GEMINI_API_KEY` — never stored in code or properties file in plain text.
- **CORS** is currently open to all origins (`*`). For production, restrict this in `WebSocketConfig`:

```java
registry.addHandler(geminiWebSocketHandler, "/chat")
        .setAllowedOrigins("https://your-frontend-domain.com");
```

- For production with HTTPS, the WebSocket URL becomes `wss://your-domain.com/chat`.

---

## 📦 Dependencies Summary

| Dependency                              | Version  | Purpose                                  |
|-----------------------------------------|----------|------------------------------------------|
| `spring-boot-starter-webmvc`            | 4.1.0    | Spring MVC web layer                     |
| `spring-boot-starter-websocket`         | 4.1.0    | WebSocket server support                 |
| `jackson-databind` (transitive)         | —        | JSON parsing via `JsonNode`              |
| `spring-web` RestClient (transitive)    | —        | HTTP client for Gemini API calls         |

---

## 👨‍💻 Author

**Mohit Pawar**
- GitHub: [@mohitpawar61](https://github.com/mohitpawar61)

---

## 📄 License

This project is for educational and development purposes.
