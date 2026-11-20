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
  // host: true 监听 0.0.0.0，手机等局域网设备才能通过电脑 IP 访问
  server: { host: true, port: 5173, proxy: apiProxy },
  // npm run preview 同样代理，避免预览时接口 404
  preview: { host: true, port: 4173, proxy: apiProxy }
})
