import { Alert, Avatar, Box, Button, Card, CardContent, Stack, TextField, Typography } from "@mui/material";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { FormEvent, useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";
import { apiMessage } from "../api/client";

export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [values, setValues] = useState({ email: "", password: "" });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  if (auth.isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const from = (location.state as { from?: Location })?.from?.pathname ?? "/";

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await auth.login(values.email, values.password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(apiMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: "100vh", display: "grid", placeItems: "center", p: 2, bgcolor: "background.default" }}>
      <Card sx={{ width: "100%", maxWidth: 440 }}>
        <CardContent sx={{ p: 4 }}>
          <Stack spacing={2} alignItems="center" sx={{ mb: 3 }}>
            <Avatar sx={{ bgcolor: "primary.main" }}><LockOutlinedIcon /></Avatar>
            <Box textAlign="center">
              <Typography variant="h5">Sign in to YazidWMS</Typography>
              <Typography color="text.secondary">Enterprise warehouse operations console</Typography>
            </Box>
          </Stack>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Stack component="form" spacing={2} onSubmit={submit}>
            <TextField label="Email" type="email" required value={values.email} onChange={(event) => setValues((current) => ({ ...current, email: event.target.value }))} />
            <TextField label="Password" type="password" required value={values.password} onChange={(event) => setValues((current) => ({ ...current, password: event.target.value }))} />
            <Button type="submit" variant="contained" size="large" disabled={loading}>Login</Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
}
