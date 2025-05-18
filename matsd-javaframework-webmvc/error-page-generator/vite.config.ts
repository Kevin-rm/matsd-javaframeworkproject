import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import * as path from "node:path";
import { viteSingleFile } from "vite-plugin-singlefile";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), viteSingleFile()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    }
  },
  build: {
    outDir: "../src/main/resources/error-pages",
    emptyOutDir: true,
    assetsInlineLimit: 100000000,
    cssCodeSplit: false,
    minify: process.env.NODE_ENV === "production" ? "terser" : "esbuild",
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
        pure_funcs: ["console.info", "console.debug", "console.warn"]
      },
      format: { comments: false }
    },
    rollupOptions: {
      output: { inlineDynamicImports: true }
    }
  }
})
