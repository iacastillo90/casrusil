import axios from 'axios';
import Cookies from 'js-cookie';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para inyectar el token JWT
api.interceptors.request.use(
  (config) => {
    const token = Cookies.get('token');
    // Para entornos locales sin login real aún, podemos hardcodear un company id o token si es necesario
    // Pero asumiendo que el login funciona, pasamos el Bearer
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    } else {
      // Mock para pruebas sin auth0 por ahora: pasamos el X-Company-Id que esperaba el back
      config.headers['X-Company-Id'] = 'comp-1';
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para respuestas (manejo de 401, etc)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Cookies.remove('token');
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
