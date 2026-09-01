import { create } from 'zustand';

export interface ChatMessage {
    id: string;
    role: 'user' | 'assistant';
    content: string;
    createdAt: string;
}

interface ChatState {
    isOpen: boolean;
    messages: ChatMessage[];
    conversationId: string | null;
    openChat: () => void;
    closeChat: () => void;
    toggleOpen: () => void;
    setConversationId: (id: string | null) => void;
    addMessage: (message: ChatMessage) => void;
    clearMessages: () => void;
}

export const useChatStore = create<ChatState>((set) => ({
    isOpen: false,
    messages: [],
    conversationId: null,
    openChat: () => set({ isOpen: true }),
    closeChat: () => set({ isOpen: false }),
    toggleOpen: () => set((state) => ({ isOpen: !state.isOpen })),
    setConversationId: (id) => set({ conversationId: id }),
    addMessage: (message) => set((state) => ({ messages: [...state.messages, message] })),
    clearMessages: () => set({ messages: [] }),
}));
