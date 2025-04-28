<template>
  <div class="chat-container">
    <div v-if="!connected" class="login">
      <h2>Enter your name</h2>
      <input v-model="username" placeholder="Your name" />
      <button @click="connect">Connect</button>
    </div>
    <div v-else class="chat">
      <h2>Chat Room: {{ roomId }}</h2>
      <div class="messages">
        <div v-for="(msg, idx) in messages" :key="idx" class="message">
          <strong>{{ msg.sender }}:</strong> {{ msg.content }}
        </div>
      </div>
      <input
          v-model="messageContent"
          @keyup.enter="sendMessage"
          placeholder="Type a message"
      />
      <button @click="sendMessage">Send</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const username = ref('')
const messageContent = ref('')
const messages = ref([])
const connected = ref(false)
const roomId = ref('public')
let stompClient = null

function connect() {
  if (!username.value) {
    alert('Please enter your name.')
    return
  }
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws-chat'),
    reconnectDelay: 5000,
    debug: (str) => console.log(str),
  })
  stompClient.onConnect = () => {
    connected.value = true
    stompClient.subscribe(`/topic/${roomId.value}`, (payload) => {
      const chat = JSON.parse(payload.body)
      messages.value.push(chat)
    })
    stompClient.publish({
      destination: '/app/chat.addUser',
      body: JSON.stringify({
        sender: username.value,
        type: 'JOIN',
        roomId: roomId.value,
      }),
    })
  }
  stompClient.activate()
}

function sendMessage() {
  if (!messageContent.value) return
  const chatMessage = {
    sender: username.value,
    content: messageContent.value,
    type: 'CHAT',
    roomId: roomId.value,
  }
  stompClient.publish({
    destination: '/app/chat.sendMessage',
    body: JSON.stringify(chatMessage),
  })
  messageContent.value = ''
}
</script>

<style scoped>
.chat-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
}
.login input {
  margin-right: 1rem;
}
.messages {
  border: 1px solid #ccc;
  height: 400px;
  overflow-y: auto;
  padding: 1rem;
  margin-bottom: 1rem;
}
.message {
  margin-bottom: 0.5rem;
}
</style>