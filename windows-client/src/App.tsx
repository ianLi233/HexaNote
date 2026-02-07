import { HashRouter, Routes, Route } from 'react-router-dom'
import { MainLayout } from './layout/MainLayout'
import { NotesPage } from './pages/NotesPage'
import { ChatPage } from './pages/ChatPage'
import { useRef, useState } from 'react'
import { ChatSession } from './components/ChatInterface'

function App() {
    // chat histories
    const [historyItems, setHistoryItems] = useState<ChatSession[]>([])
    const nextSessionIdRef = useRef<number>(1)
    const [activeChatId, setActiveChatId] = useState<number>(0)
    return (
        <HashRouter>
            <Routes>
                <Route element={<MainLayout />}>
                    <Route path="/" element={<NotesPage />} />
                    <Route path="/note/:noteId" element={<NotesPage />} />
                    <Route path="/chat" element={
                        <ChatPage 
                            historyItems={historyItems}
                            setHistoryItems={setHistoryItems}
                            nextSessionIdRef={nextSessionIdRef}
                            activeChatId={activeChatId}
                            setActiveChatId={setActiveChatId} />} 
                    />
                </Route>
            </Routes>
        </HashRouter>
    )
}

export default App
