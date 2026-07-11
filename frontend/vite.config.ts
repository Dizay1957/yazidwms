import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const apiTarget = process.env.VITE_API_PROXY_TARGET ?? "http://127.0.0.1:8081";

export default defineConfig({
  plugins: [react()],
  build: {
    chunkSizeWarningLimit: 700,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("node_modules")) {
            if (id.includes("@mui/x-data-grid")) return "mui-grid";
            if (id.includes("@mui/icons-material")) return "mui-icons";
            if (id.includes("@mui")) return "mui-core";
            if (id.includes("recharts")) return "charts";
            if (id.includes("react") || id.includes("@tanstack") || id.includes("axios")) return "react";
          }
          return undefined;
        }
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: apiTarget,
        changeOrigin: true
      }
    }
  }
});
