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
import { ref, onMounted } from 'vue'

const username      = ref('')
const messageContent= ref('')
const messages      = ref([])
const connected     = ref(false)
const roomId        = ref('public')
let stompClient     = null

onMounted(async () => {
  // now only in the browser
  const SockJSModule = await import('sockjs-client')
  const SockJS       = SockJSModule.default
  const StompModule  = await import('@stomp/stompjs')
  const Client       = StompModule.Client

  stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
    reconnectDelay: 5000,
    debug: msg => console.log(msg)
  })

  stompClient.onConnect = () => {
    connected.value = true
    stompClient.subscribe(`http://localhost:8080/topic/${roomId.value}`, frame => {
      messages.value.push(JSON.parse(frame.body))
    })
    stompClient.publish({
      destination: '/app/chat.addUser',
      body: JSON.stringify({
        sender: username.value,
        type: 'JOIN',
        roomId: roomId.value
      })
    })
  }
})

function connect() {
  if (!username.value.trim()) {
    return alert('Enter your name')
  }
  stompClient.activate()
}

function sendMessage() {
  if (!messageContent.value) return
  stompClient.publish({
    destination: '/app/chat.sendMessage',
    body: JSON.stringify({
      sender: username.value,
      content: messageContent.value,
      type: 'CHAT',
      roomId: roomId.value
    })
  })

  messages.value.push({
    sender: username.value,
    content: messageContent.value,
    type: 'CHAT',
    roomId: roomId.value
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