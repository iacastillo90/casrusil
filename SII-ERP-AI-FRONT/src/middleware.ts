import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

export function middleware(request: NextRequest) {
    const token = request.cookies.get('JSESSIONID')?.value || request.cookies.get('token')?.value || request.cookies.get('access_token')?.value
    const isAuthPage = request.nextUrl.pathname.startsWith('/login') ||
        request.nextUrl.pathname.startsWith('/register')

    // 1. Si intenta entrar a cualquier ruta protegida SIN token → Login
    if (!token && !isAuthPage && request.nextUrl.pathname !== '/') {
        const loginUrl = new URL('/login', request.url)
        // Guardar la URL original para redirigir después del login
        loginUrl.searchParams.set('redirect', request.nextUrl.pathname)
        return NextResponse.redirect(loginUrl)
    }

    // 2. Si ya tiene token e intenta entrar a login/register → Dashboard
    if (token && isAuthPage) {
        return NextResponse.redirect(new URL('/dashboard', request.url))
    }

    return NextResponse.next()
}

// SEGURIDAD: Proteger TODAS las rutas excepto:
// - _next/static: archivos estáticos de Next.js
// - _next/image: optimización de imágenes
// - favicon y archivos de imagen raíz
// - /api/: si tuviéramos endpoints internos de Next.js
// El patrón negativo '(?!...)' excluye las rutas públicas.
export const config = {
    matcher: [
        // /api se excluye para que las llamadas al backend lleguen al rewrite
        // de next.config.mjs: si el middleware las intercepta, redirige a
        // /login y el cliente recibe HTML en lugar del JSON de la API.
        '/((?!api/|login|register|_next/static|_next/image|favicon\\.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
    ],
}
