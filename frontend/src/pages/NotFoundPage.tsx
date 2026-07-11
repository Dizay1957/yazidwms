import { Button, Stack, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import { useI18n } from "../i18n/I18nProvider";

export function NotFoundPage() {
  const { t } = useI18n();
  return (
    <Stack spacing={2} alignItems="center" justifyContent="center" sx={{ minHeight: "60vh" }}>
      <Typography variant="h4">{t("notFound.title")}</Typography>
      <Typography color="text.secondary">{t("notFound.message")}</Typography>
      <Button component={RouterLink} to="/" variant="contained">{t("notFound.action")}</Button>
    </Stack>
  );
}
