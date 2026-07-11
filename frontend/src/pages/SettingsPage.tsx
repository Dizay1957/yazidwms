import { Alert, Button, Card, CardContent, Grid, Snackbar, Stack, TextField, Typography } from "@mui/material";
import { FormEvent, useEffect, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api, apiMessage, unwrap } from "../api/client";
import { useAuth } from "../auth/AuthProvider";
import { PageHeader } from "../components/PageHeader";
import type { User } from "../types/api";

export function SettingsPage() {
  const auth = useAuth();
  const queryClient = useQueryClient();
  const [profile, setProfile] = useState({ fullName: "", phone: "" });
  const [password, setPassword] = useState({ currentPassword: "", newPassword: "" });
  const [snackbar, setSnackbar] = useState<string | null>(null);

  useEffect(() => {
    setProfile({ fullName: auth.profile?.fullName ?? "", phone: auth.profile?.phone ?? "" });
  }, [auth.profile]);

  const updateProfile = useMutation({
    mutationFn: () => unwrap<User>(api.patch("/profile", profile)),
    onSuccess: () => {
      setSnackbar("Profile updated");
      queryClient.invalidateQueries({ queryKey: ["profile"] });
    }
  });

  const changePassword = useMutation({
    mutationFn: () => unwrap<void>(api.post("/auth/change-password", password)),
    onSuccess: () => {
      setSnackbar("Password changed");
      setPassword({ currentPassword: "", newPassword: "" });
    }
  });

  const submitProfile = (event: FormEvent) => {
    event.preventDefault();
    updateProfile.mutate();
  };

  const submitPassword = (event: FormEvent) => {
    event.preventDefault();
    changePassword.mutate();
  };

  return (
    <>
      <PageHeader title="Settings" subtitle="Manage your profile, password, and workspace display preferences." />
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Profile</Typography>
              {updateProfile.error && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(updateProfile.error)}</Alert>}
              <Stack component="form" spacing={2} onSubmit={submitProfile}>
                <TextField label="Full name" required value={profile.fullName} onChange={(event) => setProfile((current) => ({ ...current, fullName: event.target.value }))} />
                <TextField label="Phone" value={profile.phone} onChange={(event) => setProfile((current) => ({ ...current, phone: event.target.value }))} />
                <TextField label="Email" value={auth.profile?.email ?? auth.user?.email ?? ""} disabled />
                <Button type="submit" variant="contained" disabled={updateProfile.isPending}>Save profile</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Security</Typography>
              {changePassword.error && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(changePassword.error)}</Alert>}
              <Stack component="form" spacing={2} onSubmit={submitPassword}>
                <TextField label="Current password" type="password" required value={password.currentPassword} onChange={(event) => setPassword((current) => ({ ...current, currentPassword: event.target.value }))} />
                <TextField label="New password" type="password" required inputProps={{ minLength: 10 }} value={password.newPassword} onChange={(event) => setPassword((current) => ({ ...current, newPassword: event.target.value }))} helperText="Minimum 10 characters." />
                <Button type="submit" variant="contained" disabled={changePassword.isPending}>Change password</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <Snackbar open={Boolean(snackbar)} autoHideDuration={2800} onClose={() => setSnackbar(null)} message={snackbar} />
    </>
  );
}
