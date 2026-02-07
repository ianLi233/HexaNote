import { ChatInterface, ChatInterfaceProps } from '../components/ChatInterface'

export function ChatPage({
    historyItems,
    setHistoryItems,
    nextSessionIdRef,
    activeChatId,
    setActiveChatId,
}: ChatInterfaceProps) {
    return (
        <div className="h-full w-full flex flex-col overflow-hidden">
            {/* <header className="flex-shrink-0 p-4 border-b border-slate-800 bg-slate-900/50 backdrop-blur">
                <h1 className="text-xl font-bold text-white">Chat with RAG(slow) or Search by Embeddings(fast)</h1>
                <p className="text-xs text-slate-400">Powered by Ollama and Weaviate.</p>
            </header> */}
            <div className="flex-1 min-h-0 flex flex-col overflow-hidden">
                <ChatInterface
                    historyItems={historyItems}
                    setHistoryItems={setHistoryItems}
                    nextSessionIdRef={nextSessionIdRef}
                    activeChatId={activeChatId}
                    setActiveChatId={setActiveChatId} />
            </div>
        </div>
    )
}
