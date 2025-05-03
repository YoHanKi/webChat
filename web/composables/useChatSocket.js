// composables/useChatSocket.js
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';

export function useChatSocket(roomId, username) {
    const messages = ref([]);
    let socket;
    onMounted(() => {
        socket = new WebSocket(`ws://localhost:8080/ws-chat?roomId=${roomId}`);
        socket.onmessage = event => {
            const msg = JSON.parse(event.data);
            if (msg.roomId === roomId) {
                messages.value.push(msg);
            }
        };
    });
    const send = content => {
        if (!content.trim()) return;
        socket.send(JSON.stringify({ type: 'CHAT', sender: username, content, roomId }));
    };
    onBeforeUnmount(() => socket?.close());
    return { messages, send };
}