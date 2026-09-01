/** @type {import('next').NextConfig} */

// URL interna del backend a la que el proxy same-origin reenvía /api/v1.
// El proxy permite que la cookie httpOnly del backend se almacene y reenvíe
// bajo el mismo origen del frontend (mismo dominio => sin problemas de CORS/cookies).
const BACKEND_URL = process.env.BACKEND_INTERNAL_URL || 'http://localhost:8080';

const nextConfig = {
  output: 'standalone',
  async rewrites() {
    return [
      {
        source: '/api/v1/:path*',
        destination: `${BACKEND_URL}/api/v1/:path*`,
      },
    ];
  },
  // NOTA (Fase 5): estos flags ocultan errores de tipos/lint en el build.
  // Se pondrán en `false` al corregir los errores existentes.
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
};

export default nextConfig;
