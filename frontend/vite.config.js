import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define:{
    global:'window',
  },
  
  resolve: {
    alias: {
      global: 'globalThis', // Megoldja a "global is not defined" hibát
    },
  },
})
