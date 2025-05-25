# Project 2 - Distributed Systems Assignment

This project implements a multi-room, token-authenticated client-server chat system using Java and TCP sockets. It supports regular and AI-powered chat rooms, persistent sessions using tokens, and fault tolerance on reconnect.

## Run the Server
If using IntelliJ, to start the Server you need to go to ChatApp/src folder and open Server.java file.
<img src="screenshots/screenshot1.png">
<img src="screenshots/screenshot2.png">

## Run a Client
Use TestLeticia.java, TestVictor.java, or TestBia.java to simulate one or multiple clients.
<img src="screenshots/screenshot3.png">

## Install Ollama

Make sure you have Ollama installed in your local machine

### Windows

[Download link](https://ollama.com/download/windows)

### MacOS

[Download link](https://ollama.com/download/mac)

### Linux

Run the following command on your terminal:

`curl -fsSL https://ollama.com/install.sh | sh`

### Setting up Ollama

After installation run the following command and ensure that Ollama is running on **port 11434**:

`ollama serve`

In case ollama isn't running on port 11434, change the port in file `OllamaClient.java`

`this.endpoint = "http://localhost:11434/api/generate";` - line 15

Before running the app make sure you pull the used model by running the following command:

`ollama pull llama3`

## Using the App
On first run: \
**Choose 2** - Register \
Provide a username and password.

On future runs: \
**Choose 1** - Login \
You'll receive a session token. If the connection drops, this token will resume your session when the client reconnects.

**Chatting**
- Enter the name of a room to join or create it
- Type messages to chat
- Type **/leave** to return to the lobby
- Type **/quit** to exit the application