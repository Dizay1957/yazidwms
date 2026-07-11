import { Alert, Box, Card, CardContent, Grid, LinearProgress, Stack, Typography } from "@mui/material";
import Inventory2OutlinedIcon from "@mui/icons-material/Inventory2Outlined";
import WarehouseOutlinedIcon from "@mui/icons-material/WarehouseOutlined";
import LocalShippingOutlinedIcon from "@mui/icons-material/LocalShippingOutlined";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import WarningAmberOutlinedIcon from "@mui/icons-material/WarningAmberOutlined";
import PaidOutlinedIcon from "@mui/icons-material/PaidOutlined";
import { Bar, BarChart, CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { useQuery } from "@tanstack/react-query";
import { api, apiMessage, unwrap } from "../api/client";
import { StatCard } from "../components/StatCard";
import { PageHeader } from "../components/PageHeader";
import { currency, number } from "../utils/format";
import type { DashboardData } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

type ChartRow = { name: string; [key: string]: string | number };

const toChartRows = (record: Record<string, number>, key: string): ChartRow[] =>
  Object.entries(record ?? {}).map(([name, value]) => ({ name, [key]: Number(value) }));

const hasChartValues = (rows: ChartRow[], keys: string[]) => rows.some((row) => keys.some((key) => Number(row[key] ?? 0) > 0));

export function DashboardPage() {
  const { t } = useI18n();
  const dashboard = useQuery({
    queryKey: ["dashboard"],
    queryFn: () => unwrap<DashboardData>(api.get("/dashboard"))
  });

  const data = dashboard.data;
  const activity = toChartRows(data?.monthlyInventoryActivity ?? {}, "movements");
  const salesPurchase = Array.from(new Set([...Object.keys(data?.monthlySales ?? {}), ...Object.keys(data?.monthlyPurchases ?? {})])).map((month) => ({
    name: month,
    sales: Number(data?.monthlySales?.[month] ?? 0),
    purchases: Number(data?.monthlyPurchases?.[month] ?? 0)
  }));
  const hasSalesPurchaseData = hasChartValues(salesPurchase, ["sales", "purchases"]);
  const hasActivityData = hasChartValues(activity, ["movements"]);

  return (
    <>
      <PageHeader title={t("dashboard.title")} subtitle={t("dashboard.subtitle")} onRefresh={() => dashboard.refetch()} />
      {dashboard.isLoading && <LinearProgress sx={{ mb: 2 }} />}
      {dashboard.error && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(dashboard.error)}</Alert>}
      <Grid container spacing={2}>
        <Grid item xs={12} sm={6} lg={2}><StatCard label={t("dashboard.products")} value={number(data?.totalProducts)} icon={<Inventory2OutlinedIcon />} /></Grid>
        <Grid item xs={12} sm={6} lg={2}><StatCard label={t("dashboard.warehouses")} value={number(data?.totalWarehouses)} icon={<WarehouseOutlinedIcon />} tone="secondary" /></Grid>
        <Grid item xs={12} sm={6} lg={2}><StatCard label={t("dashboard.suppliers")} value={number(data?.totalSuppliers)} icon={<LocalShippingOutlinedIcon />} tone="success" /></Grid>
        <Grid item xs={12} sm={6} lg={2}><StatCard label={t("dashboard.customers")} value={number(data?.totalCustomers)} icon={<GroupsOutlinedIcon />} tone="secondary" /></Grid>
        <Grid item xs={12} sm={6} lg={2}><StatCard label={t("dashboard.inventoryValue")} value={currency(data?.inventoryValue)} icon={<PaidOutlinedIcon />} tone="success" /></Grid>
        <Grid item xs={12} sm={6} lg={2}><StatCard label={t("dashboard.lowStock")} value={number(data?.lowStockProducts)} icon={<WarningAmberOutlinedIcon />} tone="warning" /></Grid>

        <Grid item xs={12} lg={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>{t("dashboard.monthlySales")}</Typography>
              <Box sx={{ height: 320 }}>
                {hasSalesPurchaseData ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={salesPurchase}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Line type="monotone" dataKey="sales" stroke="#2563eb" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} />
                      <Line type="monotone" dataKey="purchases" stroke="#0f766e" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <ChartEmptyState />
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} lg={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>{t("dashboard.inventoryActivity")}</Typography>
              <Box sx={{ height: 320 }}>
                {hasActivityData ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={activity}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis allowDecimals={false} />
                      <Tooltip />
                      <Bar dataKey="movements" fill="#2563eb" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <ChartEmptyState />
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <StatusPanel title={t("dashboard.purchaseOrders")} rows={data?.purchaseOrdersByStatus ?? {}} />
        </Grid>
        <Grid item xs={12} md={6}>
          <StatusPanel title={t("dashboard.salesOrders")} rows={data?.salesOrdersByStatus ?? {}} />
        </Grid>
      </Grid>
    </>
  );
}

function ChartEmptyState() {
  const { t } = useI18n();
  return (
    <Stack
      alignItems="center"
      justifyContent="center"
      sx={{
        height: "100%",
        border: 1,
        borderColor: "divider",
        borderRadius: 1,
        bgcolor: "action.hover",
        px: 3,
        textAlign: "center"
      }}
    >
      <Typography color="text.secondary">{t("dashboard.noChartData")}</Typography>
    </Stack>
  );
}

function StatusPanel({ title, rows }: { title: string; rows: Record<string, number> }) {
  const { t } = useI18n();
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2 }}>{title}</Typography>
        <Stack spacing={1}>
          {Object.entries(rows).length === 0 && <Typography color="text.secondary">{t("dashboard.noOrderActivity")}</Typography>}
          {Object.entries(rows).map(([status, count]) => (
            <Stack key={status} direction="row" justifyContent="space-between">
              <Typography>{status.replaceAll("_", " ")}</Typography>
              <Typography fontWeight={800}>{count}</Typography>
            </Stack>
          ))}
        </Stack>
      </CardContent>
    </Card>
  );
}
