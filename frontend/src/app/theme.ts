import { createTheme } from "@mui/material/styles";
import type {} from "@mui/x-data-grid/themeAugmentation";

export const buildTheme = (mode: "light" | "dark", direction: "ltr" | "rtl" = "ltr") =>
  createTheme({
    direction,
    palette: {
      mode,
      primary: {
        main: "#2563eb"
      },
      secondary: {
        main: "#0f766e"
      },
      background: {
        default: mode === "light" ? "#f6f8fb" : "#0f172a",
        paper: mode === "light" ? "#ffffff" : "#111827"
      }
    },
    shape: {
      borderRadius: 8
    },
    typography: {
      fontFamily: "Aptos, Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif",
      h4: { fontWeight: 700 },
      h5: { fontWeight: 700 },
      h6: { fontWeight: 700 }
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: { textTransform: "none", fontWeight: 700 }
        }
      },
      MuiCard: {
        styleOverrides: {
          root: { boxShadow: "none", border: mode === "light" ? "1px solid #e5e7eb" : "1px solid #1f2937" }
        }
      },
      MuiDataGrid: {
        styleOverrides: {
          root: { border: 0 },
          columnHeaders: { fontWeight: 700 }
        }
      }
    }
  });
