import { Box, Button, Stack, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useI18n } from "../i18n/I18nProvider";

export function PageHeader({
  title,
  subtitle,
  actionLabel,
  onAction,
  onRefresh
}: {
  title: string;
  subtitle?: string;
  actionLabel?: string;
  onAction?: () => void;
  onRefresh?: () => void;
}) {
  const { t } = useI18n();

  return (
    <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" spacing={2} sx={{ mb: 2 }}>
      <Box>
        <Typography variant="h5">{title}</Typography>
        {subtitle && <Typography color="text.secondary">{subtitle}</Typography>}
      </Box>
      <Stack direction="row" spacing={1}>
        {onRefresh && <Button variant="outlined" startIcon={<RefreshIcon />} onClick={onRefresh}>{t("common.refresh")}</Button>}
        {actionLabel && <Button variant="contained" startIcon={<AddIcon />} onClick={onAction}>{actionLabel}</Button>}
      </Stack>
    </Stack>
  );
}
