import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
const apiProxy = {
  // 开发/预览服务器代理后端 API，避免跨域
  '/api': {
    target: process.env.VITE_API_TARGET || 'http://localhost:8080',
    changeOrigin: true
  }
}

export default defineConfig({
  plugins: [vue()],
  server: { port: 5173, proxy: apiProxy },
  // npm run preview 同样代理，避免预览时接口 404
  preview: { port: 4173, proxy: apiProxy }
})
