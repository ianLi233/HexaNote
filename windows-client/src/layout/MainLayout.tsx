import { Outlet, useLocation } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Hexagon } from 'lucide-react'

function getPageTitle(pathname: string) {
    if (pathname.startsWith('/chat')) return 'Chat with RAG(slow) or Search by Embeddings(fast)'
    if (pathname.startsWith('/')) return 'Notes'
    return 'HexaNote'
}

function getPageSubtitle(pathname: string) {
    if (pathname.startsWith('/chat')) return 'Powered by Ollama and Weaviate.'
    if (pathname.startsWith('/notes')) return 'Your markdown notes.'
    return ''
}

export function MainLayout() {
    const { pathname } = useLocation()
    const title = getPageTitle(pathname)
    const subtitle = getPageSubtitle(pathname)

    return (
        <div className="flex flex-col h-screen bg-slate-950 text-slate-100 overflow-hidden">
            {/* Shared App Header (aligns logo + page title) */}
            <header className="h-16 flex-shrink-0 border-b border-slate-800 bg-slate-950/80 backdrop-blur">
                <div className="h-full flex">
                    {/* Left header cell matches sidebar width */}
                    <div className="w-56 flex-shrink-0 flex items-center gap-3 px-5">
                        {/* Simple logo mark */}
                        <Hexagon className="w-8 h-8 text-cyan-500" />
                        <div className="text-2xl font-bold tracking-tight">HexaNote</div>
                    </div>

                    {/* Right header cell: page title */}
                    <div className="flex-1 flex items-center justify-between px-6">
                        <div className="leading-tight">
                            <div className="text-3xl font-bold tracking-tight">{title}</div>
                            {subtitle ? (
                                <div className="text-base text-slate-400">{subtitle}</div>
                            ) : null}
                        </div>
                    </div>
                </div>
            </header>

            {/* Body */}
            <div className="flex flex-1 min-h-0 overflow-hidden">
                {/* Left Nav */}
                <div className="w-56 flex-shrink-0 min-h-0">
                    <Sidebar />
                </div>

                {/* Main */}
                <main className="flex-1 relative overflow-hidden">
                    <div className="absolute inset-0 bg-gradient-to-br from-cyan-900/5 via-transparent to-purple-900/5 pointer-events-none" />
                    <div className="relative h-full w-full">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    )
}

// import { Outlet } from 'react-router-dom'
// import { Sidebar } from './Sidebar'

// export function MainLayout() {
//     return (
//         <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden">
//             <div className="w-56 flex-shrink-0">
//                 <Sidebar />
//             </div>
//             <main className="flex-1 relative overflow-hidden">
//                 <div className="absolute inset-0 bg-gradient-to-br from-cyan-900/5 via-transparent to-purple-900/5 pointer-events-none" />
//                 <div className="relative h-full w-full">
//                     <Outlet />
//                 </div>
//             </main>
//         </div>
//     )
// }
