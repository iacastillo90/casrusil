'use server';

import { createAI, getMutableAIState } from 'ai/rsc';
import { v4 as uuidv4 } from 'uuid';
import { ReactNode } from 'react';

export interface ServerMessage {
    role: 'user' | 'assistant' | 'system';
    content: string;
}

export interface ClientMessage {
    id: string;
    role: 'user' | 'assistant';
    display: ReactNode;
}

export async function submitUserMessage(content: string): Promise<ClientMessage> {
    'use server';

    const aiState = getMutableAIState<typeof AI>();

    aiState.update([
        ...aiState.get(),
        {
            role: 'user',
            content,
        },
    ]);

    // Use next/headers to extract the auth cookie/token
    const { cookies } = await import('next/headers');
    const cookieStore = await cookies();
    const sessionId = cookieStore.get('JSESSIONID')?.value || cookieStore.get('token')?.value || '';

    let backendResponse = "Error de comunicación con el asistente (Backend)";
    
    try {
        // Server Action: necesita URL absoluta interna; el proxy relativo solo sirve en el navegador.
        const internalHost = process.env.BACKEND_INTERNAL_URL || 'http://localhost:8092';
        const baseUrl = `${internalHost}/api/v1`;
        
        // Asumiendo que primero creamos una conversación o usamos una genérica
        // Por simplicidad, enviaremos directo asumiendo un conversationId 'default' o iniciaremos una.
        const convResponse = await fetch(`${baseUrl}/ai/conversations`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Cookie': `token=${sessionId}; JSESSIONID=${sessionId}` // Pass session context
            }
        });
        
        if (convResponse.ok) {
            const conversation = await convResponse.json();
            
            const msgResponse = await fetch(`${baseUrl}/ai/conversations/${conversation.id}/messages`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Cookie': `token=${sessionId}; JSESSIONID=${sessionId}`
                },
                body: JSON.stringify({ content })
            });
            
            if (msgResponse.ok) {
                const msgData = await msgResponse.json();
                backendResponse = msgData.content || msgData.text || backendResponse;
            } else {
                console.error("Error from AI messages endpoint:", msgResponse.status);
            }
        } else {
            console.error("Error creating AI conversation:", convResponse.status);
        }
    } catch (err) {
        console.error("Failed to connect to AI Backend:", err);
    }

    aiState.done([
        ...aiState.get(),
        {
            role: 'assistant',
            content: backendResponse,
        },
    ]);

    return {
        id: uuidv4(),
        role: 'assistant',
        display: <div>{backendResponse}</div>,
    };
}

export const AI = createAI<ServerMessage[], ClientMessage[]>({
    actions: {
        submitUserMessage,
    },
    initialUIState: [],
    initialAIState: [],
});
