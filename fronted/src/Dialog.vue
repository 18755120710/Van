<template>
    <div class="main-page">
        <header class="app-header">
            <h1 class="logo-text">Agent<span class="highlight">Scope</span></h1>
        </header>

        <div class="chat-container">
            <div class="messages" ref="messagesContainer">
                <transition-group name="msg" tag="div" class="messages-list">
                    <div v-for="(message, index) in messages" :key="index" :class="['message-wrapper', message.type]">
                        <div class="avatar">{{ message.type === 'user' ? 'U' : 'AI' }}</div>
                        <div class="message-content">
                            <span class="message-text">{{ message.text }}</span>
                            <a v-if="message.imageUrl" :href="message.imageUrl" target="_blank" class="media-link">
                                <img :src="message.imageUrl" class="message-image" alt="Generated image" />
                            </a>
                            <a v-if="message.fileUrl" :href="message.fileUrl" target="_blank" class="download-link">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                                Download File
                            </a>
                        </div>
                    </div>
                </transition-group>
            </div>
            
            <div class="input-container">
                <div class="message-input">
                    <input type="text" v-model="newMessage" placeholder="Type your message to AgentScope..." @keyup.enter="sendMessage" :disabled="disableInput" />
                    <button @click="sendMessage" :disabled="disableInput || !newMessage.trim()" class="send-btn">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
                    </button>
                </div>
                <div class="status-hint" v-if="disableInput">
                    <span class="loading-dot"></span> Agent is thinking...
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { Client } from '@stomp/stompjs';

export default {
    data() {
        return {
            disableInput: false,
            isVisible: true,
            messages: [
                { type: 'user', text: 'Hello, server!' },
                { type: 'server', text: 'Hello, user!' },
            ],
            newMessage: '',
            stompClient: null
        };
    },
    methods: {
        sendMessage() {
            if (this.newMessage.trim() && !this.disableInput) {
                var msg = { type: 'user', text: this.newMessage };
                this.messages.push(msg);
                this.newMessage = '';
                if(this.stompClient && this.stompClient.connected) {
                    this.stompClient.publish({ destination: '/app/enhanced-dialog', body: JSON.stringify(msg) });
                }
                this.scrollToBottom();
            }
        },
        closeDialog() {
            this.isVisible = false;
        },
        handleMessage(playload) {
            const message = JSON.parse(playload.body);
            if (message.text) {
                this.messages.push(message);
                this.scrollToBottom();
            }
            if (message.meta) {
                if (message.meta.serverStatusHint == 0) {
                    this.disableInput = false;
                } else if (message.meta.serverStatusHint == 1) {
                    this.disableInput = true;
                }
            }
        },
        scrollToBottom() {
            this.$nextTick(() => {
                const messagesContainer = this.$refs.messagesContainer;
                if(messagesContainer) {
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }
            });
        }
    },
    mounted() {
        this.scrollToBottom();
    },
    created() {
        console.log("Starting connection to WebSocket Server")
        this.stompClient = new Client({
            brokerURL: 'ws://localhost:18081/bs-dialog-websocket'
        });
        this.stompClient.onConnect = (frame) => {
            console.log('Connected: ' + frame);
            this.stompClient.subscribe('/user/queue/dialog', this.handleMessage);
        };
        this.stompClient.onWebSocketError = (error) => {
            console.error('Error with websocket', error);
        };

        this.stompClient.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };
        this.stompClient.activate();
    }
};
</script>

<style scoped>
/* App Layout */
.main-page {
    display: flex;
    flex-direction: column;
    height: 100vh;
    width: 100%;
    background: var(--color-background);
    color: var(--color-text);
    overflow: hidden;
    font-family: var(--font-body);
}

.app-header {
    padding: 1.5rem 2rem;
    border-bottom: 1px solid var(--color-border);
    display: flex;
    align-items: center;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    z-index: 10;
}

.logo-text {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0;
    letter-spacing: -0.5px;
}

.highlight {
    color: var(--color-cta);
}

.chat-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    max-width: 1000px;
    margin: 0 auto;
    width: 100%;
    position: relative;
    padding: 0 1rem;
}

/* Messages Area */
.messages {
    flex: 1;
    overflow-y: auto;
    padding: 2rem 1rem;
    scroll-behavior: smooth;
    /* Custom Scrollbar */
    scrollbar-width: thin;
    scrollbar-color: var(--color-secondary) transparent;
}
.messages::-webkit-scrollbar {
    width: 6px;
}
.messages::-webkit-scrollbar-thumb {
    background-color: var(--color-secondary);
    border-radius: 10px;
}

.messages-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

/* Message Bubbles */
.message-wrapper {
    display: flex;
    gap: 1rem;
    max-width: 85%;
    animation: fadeIn 0.3s ease-out forwards;
}

.message-wrapper.user {
    align-self: flex-end;
    flex-direction: row-reverse;
}

.message-wrapper.server {
    align-self: flex-start;
}

.avatar {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: var(--font-heading);
    font-weight: 600;
    font-size: 0.85rem;
    flex-shrink: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user .avatar {
    background: var(--color-cta);
    color: var(--color-background);
}

.server .avatar {
    background: var(--color-secondary);
    color: var(--color-text);
    border: 1px solid var(--color-border);
}

.message-content {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.message-text {
    padding: 1rem 1.25rem;
    border-radius: 12px;
    font-size: 0.95rem;
    line-height: 1.6;
    word-wrap: break-word;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user .message-text {
    background: var(--color-primary);
    border: 1px solid var(--color-border);
    border-bottom-right-radius: 4px;
}

.server .message-text {
    background: var(--color-secondary);
    border: 1px solid var(--color-border);
    border-bottom-left-radius: 4px;
}

/* Media and Links */
.message-image {
    max-width: 100%;
    max-height: 300px;
    border-radius: 8px;
    border: 1px solid var(--color-border);
    transition: transform 0.2s ease;
}
.message-image:hover {
    transform: scale(1.02);
}

.download-link {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1rem;
    background: var(--color-secondary);
    color: var(--color-text);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    text-decoration: none;
    font-size: 0.9rem;
    font-weight: 500;
    transition: all 0.2s ease;
}
.download-link:hover {
    background: var(--color-border);
}

/* Input Area */
.input-container {
    padding: 1.5rem 0 2rem;
    position: sticky;
    bottom: 0;
    background: linear-gradient(to top, var(--color-background) 80%, transparent);
}

.message-input {
    display: flex;
    gap: 0.75rem;
    padding: 0.75rem;
    background: var(--color-primary);
    border: 1px solid var(--color-border);
    border-radius: 16px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.message-input:focus-within {
    border-color: var(--color-border-hover);
    box-shadow: 0 0 0 2px rgba(17, 24, 39, 0.1);
}

.message-input input {
    flex: 1;
    background: transparent;
    border: none;
    color: var(--color-text);
    padding: 0.5rem 1rem;
    font-family: var(--font-body);
    font-size: 1rem;
    outline: none;
}
.message-input input::placeholder {
    color: var(--color-text-muted);
}
.message-input input:disabled {
    cursor: not-allowed;
    opacity: 0.6;
}

.send-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-cta);
    color: #FFFFFF;
    border: none;
    border-radius: 12px;
    width: 44px;
    height: 44px;
    cursor: pointer;
    transition: all 0.2s ease;
}
.send-btn:hover:not(:disabled) {
    background: var(--color-cta-hover);
    transform: translateY(-1px);
}
.send-btn:disabled {
    background: var(--color-secondary);
    color: var(--color-text-muted);
    cursor: not-allowed;
}

/* Status Hint */
.status-hint {
    font-size: 0.85rem;
    color: var(--color-text-muted);
    margin-top: 0.75rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
}

.loading-dot {
    width: 8px;
    height: 8px;
    background-color: var(--color-cta);
    border-radius: 50%;
    animation: pulse 1.5s infinite ease-in-out;
}

/* Animations */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
    0% { transform: scale(0.8); opacity: 0.5; }
    50% { transform: scale(1.2); opacity: 1; }
    100% { transform: scale(0.8); opacity: 0.5; }
}

.msg-enter-active, .msg-leave-active {
    transition: all 0.3s ease;
}
.msg-enter-from {
    opacity: 0;
    transform: translateY(15px);
}
.msg-leave-to {
    opacity: 0;
    transform: translateY(-15px);
}
</style>
