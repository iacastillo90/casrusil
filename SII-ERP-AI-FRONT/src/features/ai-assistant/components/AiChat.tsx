"use client";

import { useState, useRef, useEffect } from "react";
import api from "@/lib/axios";
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";

interface Message {
  role: "user" | "assistant";
  content: string;
}

/**
 * Componente de chat con el Asistente IA Financiero.
 *
 * Flujo correcto:
 * 1. Al primer mensaje, crea una conversación (POST /ai/conversations)
 * 2. Cada mensaje subsiguiente se envía a la conversación activa
 *    (POST /ai/conversations/{id}/messages)
 */
export function AiChat() {
  const [messages, setMessages] = useState<Message[]>([
    { role: "assistant", content: "¡Hola! Soy tu asistente contable IA del SII-ERP. Tengo acceso al Libro Mayor, Inventario y Remuneraciones. ¿En qué te puedo ayudar hoy?" }
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  // Auto-scroll al último mensaje
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage: Message = { role: "user", content: input };
    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setLoading(true);

    try {
      let activeConversationId = conversationId;

      // Paso 1: Si no existe conversación activa, crearla
      if (!activeConversationId) {
        const conversationRes = await api.post('/ai/conversations');
        activeConversationId = conversationRes.data.id;
        setConversationId(activeConversationId);
      }

      // Paso 2: Enviar el mensaje a la conversación activa
      const messageRes = await api.post(
        `/ai/conversations/${activeConversationId}/messages`,
        { content: userMessage.content }
      );

      const reply = messageRes.data?.content || messageRes.data?.reply || "Sin respuesta del asistente.";
      setMessages((prev) => [...prev, { role: "assistant", content: reply }]);

    } catch (error) {
      console.error("Error comunicando con el asistente IA:", error);
      setMessages((prev) => [...prev, {
        role: "assistant",
        content: "⚠️ Error de conexión con la IA. Verifica que el servicio LangChain4j/Gemini esté configurado y el backend esté corriendo."
      }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 h-[80vh] flex flex-col">
      <Card className="flex flex-col h-full">
        <CardHeader>
          <CardTitle>Asistente IA (Gerente Financiero)</CardTitle>
        </CardHeader>
        <CardContent className="flex-1 overflow-hidden">
          <ScrollArea className="h-full pr-4">
            <div className="space-y-4">
              {messages.map((msg, idx) => (
                <div key={idx} className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}>
                  <div className={`max-w-[80%] p-3 rounded-lg ${msg.role === "user" ? "bg-blue-600 text-white rounded-br-none" : "bg-muted rounded-bl-none"}`}>
                    <p className="whitespace-pre-wrap text-sm">{msg.content}</p>
                  </div>
                </div>
              ))}
              {loading && (
                <div className="flex justify-start">
                  <div className="bg-muted p-3 rounded-lg rounded-bl-none">
                    <p className="text-sm animate-pulse">Pensando...</p>
                  </div>
                </div>
              )}
              <div ref={bottomRef} />
            </div>
          </ScrollArea>
        </CardContent>
        <CardFooter className="pt-4 border-t">
          <form className="flex w-full space-x-2" onSubmit={(e) => { e.preventDefault(); sendMessage(); }}>
            <Input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Pregúntale sobre tus balances, impuestos o stock..."
              disabled={loading}
            />
            <Button type="submit" disabled={loading || !input.trim()}>
              Enviar
            </Button>
          </form>
        </CardFooter>
      </Card>
    </div>
  );
}
