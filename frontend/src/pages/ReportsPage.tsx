import { Alert, Card, CardContent, Grid, Stack, Button, Typography } from "@mui/material";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import { useState } from "react";
import { api, apiMessage } from "../api/client";
import { PageHeader } from "../components/PageHeader";

const reports = [
  { label: "Inventory Valuation", path: "/reports/inventory" },
  { label: "Low Stock", path: "/reports/low-stock" },
  { label: "Purchase Orders", path: "/reports/purchase" },
  { label: "Sales Orders", path: "/reports/sales" },
  { label: "Stock Movements", path: "/reports/stock-movements" }
];

const formats = ["CSV", "EXCEL", "PDF"];

export function ReportsPage() {
  const [error, setError] = useState<string | null>(null);

  const download = async (path: string, format: string) => {
    setError(null);
    try {
      const response = await api.get(path, { params: { format }, responseType: "blob" });
      const url = URL.createObjectURL(response.data);
      const link = document.createElement("a");
      link.href = url;
      link.download = `${path.split("/").pop()}-${format.toLowerCase()}`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(apiMessage(err));
    }
  };

  return (
    <>
      <PageHeader title="Reports" subtitle="Export operational reports from the Spring Boot reporting service." />
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Grid container spacing={2}>
        {reports.map((report) => (
          <Grid item xs={12} md={6} lg={4} key={report.path}>
            <Card>
              <CardContent>
                <Typography variant="h6">{report.label}</Typography>
                <Typography color="text.secondary" sx={{ mb: 2 }}>Download as CSV, Excel, or PDF.</Typography>
                <Stack direction="row" spacing={1} flexWrap="wrap">
                  {formats.map((format) => (
                    <Button key={format} variant="outlined" startIcon={<DownloadOutlinedIcon />} onClick={() => download(report.path, format)}>
                      {format}
                    </Button>
                  ))}
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </>
  );
}
