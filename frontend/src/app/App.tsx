import { CssBaseline, ThemeProvider } from "@mui/material";
import { QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { useMemo, useState } from "react";
import { AuthProvider } from "../auth/AuthProvider";
import { queryClient } from "./queryClient";
import { buildTheme } from "./theme";
import { AppRoutes } from "../routes/AppRoutes";

export function App() {
  const [mode, setMode] = useState<"light" | "dark">(() => (localStorage.getItem("yazidwms.theme") as "light" | "dark") || "light");
  const theme = useMemo(() => buildTheme(mode), [mode]);

  const toggleMode = () => {
    setMode((current) => {
      const next = current === "light" ? "dark" : "light";
      localStorage.setItem("yazidwms.theme", next);
      return next;
    });
  };

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <AuthProvider>
            <AppRoutes mode={mode} onToggleMode={toggleMode} />
          </AuthProvider>
        </BrowserRouter>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
