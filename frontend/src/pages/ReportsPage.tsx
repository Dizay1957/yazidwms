import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  LinearProgress,
  Snackbar,
  Stack,
  Typography
} from "@mui/material";
import AssessmentOutlinedIcon from "@mui/icons-material/AssessmentOutlined";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import GridOnOutlinedIcon from "@mui/icons-material/GridOnOutlined";
import InsertDriveFileOutlinedIcon from "@mui/icons-material/InsertDriveFileOutlined";
import PictureAsPdfOutlinedIcon from "@mui/icons-material/PictureAsPdfOutlined";
import TableChartOutlinedIcon from "@mui/icons-material/TableChartOutlined";
import { ReactNode, useState } from "react";
import { api, apiMessage } from "../api/client";
import { PageHeader } from "../components/PageHeader";
import { StatCard } from "../components/StatCard";
import { useI18n } from "../i18n/I18nProvider";

type ReportFormat = "CSV" | "EXCEL" | "PDF";
type ReportKey = "inventory" | "lowStock" | "purchase" | "sales" | "movements";

const formats: Array<{ value: ReportFormat; icon: ReactNode; tone: string }> = [
  { value: "CSV", icon: <InsertDriveFileOutlinedIcon />, tone: "#16a34a" },
  { value: "EXCEL", icon: <GridOnOutlinedIcon />, tone: "#0f766e" },
  { value: "PDF", icon: <PictureAsPdfOutlinedIcon />, tone: "#dc2626" }
];

const reports: Array<{ key: ReportKey; titleKey: Parameters<ReturnType<typeof useI18n>["t"]>[0]; descriptionKey: Parameters<ReturnType<typeof useI18n>["t"]>[0]; path: string }> = [
  { key: "inventory", titleKey: "reports.inventory", descriptionKey: "reports.inventoryDesc", path: "/reports/inventory" },
  { key: "lowStock", titleKey: "reports.lowStock", descriptionKey: "reports.lowStockDesc", path: "/reports/low-stock" },
  { key: "purchase", titleKey: "reports.purchase", descriptionKey: "reports.purchaseDesc", path: "/reports/purchase" },
  { key: "sales", titleKey: "reports.sales", descriptionKey: "reports.salesDesc", path: "/reports/sales" },
  { key: "movements", titleKey: "reports.movements", descriptionKey: "reports.movementsDesc", path: "/reports/stock-movements" }
];

export function ReportsPage() {
  const { t } = useI18n();
  const [error, setError] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState<string | null>(null);
  const [loading, setLoading] = useState<string | null>(null);

  const download = async (report: (typeof reports)[number], format: ReportFormat) => {
    const loadingKey = `${report.key}-${format}`;
    setError(null);
    setLoading(loadingKey);
    try {
      const response = await api.get(report.path, { params: { format }, responseType: "blob" });
      const url = URL.createObjectURL(response.data);
      const link = document.createElement("a");
      link.href = url;
      link.download = filenameFromHeaders(response.headers["content-disposition"]) ?? `${report.key}-${format.toLowerCase()}`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      setSnackbar(t("reports.downloaded", { report: t(report.titleKey), format }));
    } catch (err) {
      setError(`${t("reports.downloadFailed")}: ${apiMessage(err)}`);
    } finally {
      setLoading(null);
    }
  };

  return (
    <>
      <PageHeader title={t("reports.title")} subtitle={t("reports.subtitle")} />
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={12} sm={4}><StatCard label={t("reports.kpiOperational")} value={reports.length} icon={<AssessmentOutlinedIcon />} /></Grid>
        <Grid item xs={12} sm={4}><StatCard label={t("reports.kpiFormats")} value={formats.length} icon={<TableChartOutlinedIcon />} tone="secondary" /></Grid>
        <Grid item xs={12} sm={4}><StatCard label={t("reports.kpiRealtime")} value={t("reports.kpiRealtimeValue")} icon={<DownloadOutlinedIcon />} tone="success" /></Grid>
      </Grid>

      <Grid container spacing={2}>
        {reports.map((report) => (
          <Grid item xs={12} md={6} xl={4} key={report.path}>
            <Card sx={{ height: "100%", overflow: "hidden" }}>
              <Box sx={{ height: 4, bgcolor: "primary.main" }} />
              <CardContent sx={{ height: "100%", display: "flex", flexDirection: "column", gap: 2 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start" spacing={2}>
                  <Box>
                    <Typography variant="h6">{t(report.titleKey)}</Typography>
                    <Typography color="text.secondary" sx={{ mt: 0.5 }}>{t(report.descriptionKey)}</Typography>
                  </Box>
                  <Chip size="small" label={t("reports.generatedByApi")} sx={{ maxWidth: 180 }} />
                </Stack>

                <Stack spacing={0.75}>
                  <Typography variant="overline" color="text.secondary">{t("reports.bestFor")}</Typography>
                  <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                    <Chip size="small" label={t("reports.csvUse")} />
                    <Chip size="small" label={t("reports.excelUse")} />
                    <Chip size="small" label={t("reports.pdfUse")} />
                  </Stack>
                </Stack>

                <Box sx={{ mt: "auto" }}>
                  <Typography variant="overline" color="text.secondary">{t("reports.availableFormats")}</Typography>
                  <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                    {formats.map((format) => {
                      const active = loading === `${report.key}-${format.value}`;
                      return (
                        <Button
                          key={format.value}
                          variant="outlined"
                          startIcon={active ? undefined : format.icon}
                          disabled={Boolean(loading)}
                          onClick={() => download(report, format.value)}
                          sx={{ borderColor: format.tone, color: format.tone, minWidth: 104 }}
                        >
                          {active ? t("reports.downloading", { format: format.value }) : format.value}
                        </Button>
                      );
                    })}
                  </Stack>
                </Box>

                {loading?.startsWith(report.key) && <LinearProgress />}
                <Typography variant="caption" color="text.secondary">{t("reports.emptySafe")}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      <Snackbar open={Boolean(snackbar)} autoHideDuration={3000} onClose={() => setSnackbar(null)} message={snackbar} />
    </>
  );
}

function filenameFromHeaders(disposition?: string) {
  if (!disposition) {
    return null;
  }
  const match = /filename="?([^"]+)"?/i.exec(disposition);
  return match?.[1] ?? null;
}
