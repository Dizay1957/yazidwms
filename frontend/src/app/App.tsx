import { CssBaseline, ThemeProvider } from "@mui/material";
import { QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { useMemo, useState } from "react";
import { AuthProvider } from "../auth/AuthProvider";
import { I18nProvider, useI18n } from "../i18n/I18nProvider";
import { queryClient } from "./queryClient";
import { buildTheme } from "./theme";
import { AppRoutes } from "../routes/AppRoutes";

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <I18nProvider>
        <ThemedApp />
      </I18nProvider>
    </QueryClientProvider>
  );
}

function ThemedApp() {
  const [mode, setMode] = useState<"light" | "dark">(() => (localStorage.getItem("yazidwms.theme") as "light" | "dark") || "light");
  const { direction } = useI18n();
  const theme = useMemo(() => buildTheme(mode, direction), [direction, mode]);

  const toggleMode = () => {
    setMode((current) => {
      const next = current === "light" ? "dark" : "light";
      localStorage.setItem("yazidwms.theme", next);
      return next;
    });
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes mode={mode} onToggleMode={toggleMode} />
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}
