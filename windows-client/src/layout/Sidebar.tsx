import { Link, useLocation } from 'react-router-dom'
import { Sticker, MessageSquare, RefreshCw, Loader2 } from 'lucide-react'
import { clsx } from 'clsx'
import { useState } from 'react'

export function Sidebar() {
    const location = useLocation()

    const navItems = [
        { name: 'Notes', icon: Sticker, path: '/' },
        { name: 'Chat', icon: MessageSquare, path: '/chat' },
        // { name: 'Settings', icon: Settings, path: '/settings' },
    ]

    const [isLoading, setIsLoading] = useState(false)
    const triggerFetchNotes = () => {
        setIsLoading(true)
        window.dispatchEvent(new CustomEvent('notes:refresh'))
    }

    // Listen for completion status from NotesPage
    useState(() => {
        const onDone = (evt: Event) => {
            const e = evt as CustomEvent<{ ok: boolean; message?: string }>
            // optionally keep a brief delay before allowing another refresh
            if (!e.detail?.ok) {
                window.setTimeout(() => {}, 500)
            }
            setIsLoading(false)
        }
        window.addEventListener('notes:refresh:done', onDone as EventListener)
        return () => window.removeEventListener('notes:refresh:done', onDone as EventListener)
    })

    return (
        <div className="w-full h-full bg-slate-900 border-r border-slate-800 flex flex-col min-w-0">
            {/* <div className="h-16 flex items-center justify-center border-b border-slate-800">
                <Hexagon className="w-8 h-8 text-cyan-500" />
                <span className="ml-3 font-bold text-xl text-white hidden md:block">HexaNote</span>
            </div> */}

            <nav className="flex-1 py-4 flex flex-col gap-2 p-2">
                {navItems.map((item) => {
                    const isActive = location.pathname === item.path
                    return (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={clsx(
                                'flex items-center justify-between gap-2 p-3 rounded-lg transition-colors group relative min-w-0',
                                isActive
                                    ? 'bg-cyan-500/10 text-cyan-400'
                                    : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                            )}
                        >
                            <div className="flex items-center min-w-0 flex-1">
                                <item.icon className="w-6 h-6" />
                                <span className="ml-3 font-medium hidden md:block truncate">{item.name}</span>
                            </div>
                            {item.name === 'Notes' && isActive && (
                                <button
                                    onClick={(e) => {
                                        e.preventDefault()
                                        e.stopPropagation()
                                        triggerFetchNotes()
                                    }}
                                    className="inline-flex items-center justify-center h-6 w-6 bg-slate-800 text-cyan-400 rounded hover:bg-slate-800/90 transition-colors flex-shrink-0"
                                    title="Refresh notes"
                                >
                                    {isLoading ? (
                                        <Loader2 className="animate-spin text-cyan-500" />
                                    ) : (
                                        <RefreshCw size={16} />
                                    )}
                                </button>
                            )}
                            {isActive && (
                                <div className="absolute left-0 w-1 h-6 bg-cyan-500 rounded-r-full" />
                            )}
                        </Link>
                    )
                })}
            </nav>

            <div className="p-4 border-t border-slate-800 text-xs text-slate-500 text-center hidden md:block">
                v0.1.0 Alpha
            </div>
        </div>
    )
}
