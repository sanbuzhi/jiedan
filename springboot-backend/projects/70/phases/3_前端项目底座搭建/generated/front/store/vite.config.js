import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig(({ mode }) => {
  const isDev = mode === 'development'
  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [VantResolver()],
        imports: ['vue', 'vue-router', 'pinia'],
        dts: 'src/auto-imports.d.ts',
        eslintrc: {
          enabled: true
        }
      }),
      Components({
        resolvers: [VantResolver()],
        dts: 'src/components.d.ts'
      })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000,
      open: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: !isDev,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: !isDev,
          drop_debugger: !isDev
        }
      }
    },
    css: {
      postcss: {
        plugins: [
          require('postcss-px-to-viewport')({
            viewportWidth: 375,
            viewportHeight: 667,
            unitPrecision: 5,
            viewportUnit: 'vw',
            selectorBlackList: ['.ignore', '.hairlines'],
            minPixelValue: 1,
            mediaQuery: false
          })
        ]
      }
    }
  }
})