import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: './', // Relative base path for subfolder / GitHub Pages deployment
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
  },
});
