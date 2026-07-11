import { Alert, Button, Card, CardContent, FormControl, Grid, InputLabel, MenuItem, Select, Snackbar, Stack, TextField, Typography } from "@mui/material";
import { FormEvent, useEffect, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api, apiMessage, unwrap } from "../api/client";
import { useAuth } from "../auth/AuthProvider";
import { PageHeader } from "../components/PageHeader";
import type { User } from "../types/api";
import { Language, useI18n } from "../i18n/I18nProvider";

export function SettingsPage() {
  const auth = useAuth();
  const { language, languageLabels, setLanguage, t } = useI18n();
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
      setSnackbar(t("settings.profileUpdated"));
      queryClient.invalidateQueries({ queryKey: ["profile"] });
    }
  });

  const changePassword = useMutation({
    mutationFn: () => unwrap<void>(api.post("/auth/change-password", password)),
    onSuccess: () => {
      setSnackbar(t("settings.passwordChanged"));
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
      <PageHeader title={t("settings.title")} subtitle={t("settings.subtitle")} />
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>{t("settings.profile")}</Typography>
              {updateProfile.error && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(updateProfile.error)}</Alert>}
              <Stack component="form" spacing={2} onSubmit={submitProfile}>
                <TextField label={t("settings.fullName")} required value={profile.fullName} onChange={(event) => setProfile((current) => ({ ...current, fullName: event.target.value }))} />
                <TextField label={t("settings.phone")} value={profile.phone} onChange={(event) => setProfile((current) => ({ ...current, phone: event.target.value }))} />
                <TextField label={t("login.email")} value={auth.profile?.email ?? auth.user?.email ?? ""} disabled />
                <Button type="submit" variant="contained" disabled={updateProfile.isPending}>{t("settings.saveProfile")}</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>{t("settings.security")}</Typography>
              {changePassword.error && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(changePassword.error)}</Alert>}
              <Stack component="form" spacing={2} onSubmit={submitPassword}>
                <TextField label={t("settings.currentPassword")} type="password" required value={password.currentPassword} onChange={(event) => setPassword((current) => ({ ...current, currentPassword: event.target.value }))} />
                <TextField label={t("settings.newPassword")} type="password" required inputProps={{ minLength: 10 }} value={password.newPassword} onChange={(event) => setPassword((current) => ({ ...current, newPassword: event.target.value }))} helperText={t("settings.minPassword")} />
                <Button type="submit" variant="contained" disabled={changePassword.isPending}>{t("settings.changePassword")}</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>{t("settings.preferences")}</Typography>
              <Stack spacing={2}>
                <FormControl fullWidth>
                  <InputLabel>{t("common.language")}</InputLabel>
                  <Select
                    label={t("common.language")}
                    value={language}
                    onChange={(event) => setLanguage(event.target.value as Language)}
                  >
                    {(Object.keys(languageLabels) as Language[]).map((option) => (
                      <MenuItem key={option} value={option}>{languageLabels[option]}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Typography color="text.secondary">{t("settings.languageHelper")}</Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <Snackbar open={Boolean(snackbar)} autoHideDuration={2800} onClose={() => setSnackbar(null)} message={snackbar} />
    </>
  );
}
