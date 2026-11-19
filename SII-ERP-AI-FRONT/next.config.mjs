/** @type {import('next').NextConfig} */

// URL interna del backend a la que el proxy same-origin reenvía /api/v1.
// El proxy permite que la cookie httpOnly del backend se almacene y reenvíe
// bajo el mismo origen del frontend (mismo dominio => sin problemas de CORS/cookies).
const BACKEND_URL = process.env.BACKEND_INTERNAL_URL || 'http://localhost:8092';

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
  // Fase 5 completada: los errores de tipos/lint fueron corregidos, por lo que
  // el build vuelve a validar tipos y lint de forma estricta.
  eslint: {
    ignoreDuringBuilds: false,
  },
  typescript: {
    ignoreBuildErrors: false,
  },
};

export default nextConfig;
