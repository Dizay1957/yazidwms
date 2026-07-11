import { Alert, Button, Snackbar, Stack } from "@mui/material";
import { GridColDef, GridPaginationModel, GridSortModel } from "@mui/x-data-grid";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import CancelOutlinedIcon from "@mui/icons-material/CancelOutlined";
import InventoryOutlinedIcon from "@mui/icons-material/InventoryOutlined";
import { api, apiMessage, unwrap } from "../../api/client";
import { listPage } from "../../api/endpoints";
import { DataTableShell } from "../../components/DataTableShell";
import { FormDialog, FormField, FormValues } from "../../components/FormDialog";
import { PageHeader } from "../../components/PageHeader";
import { StatusChip } from "../../components/StatusChip";
import { currency, dateTime } from "../../utils/format";
import type { Customer, InventoryItem, Product, PurchaseOrder, SalesOrder, Supplier } from "../../types/api";
import { useI18n } from "../../i18n/I18nProvider";

type OrderKind = "purchase" | "sales";
type Order = PurchaseOrder | SalesOrder;

export function OrderPage({ kind }: { kind: OrderKind }) {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [sortModel, setSortModel] = useState<GridSortModel>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [snackbar, setSnackbar] = useState<string | null>(null);
  const sort = sortModel[0] ? `${sortModel[0].field},${sortModel[0].sort ?? "asc"}` : undefined;
  const endpoint = kind === "purchase" ? "/purchase-orders" : "/sales-orders";
  const queryKey = kind === "purchase" ? "purchase-orders" : "sales-orders";

  const orders = useQuery({
    queryKey: [queryKey, paginationModel.page, paginationModel.pageSize, sort],
    queryFn: () => listPage<Order>(endpoint, { page: paginationModel.page, size: paginationModel.pageSize, sort })
  });
  const suppliers = useQuery({ queryKey: ["supplier-options"], queryFn: () => listPage<Supplier>("/suppliers", { size: 200 }), enabled: kind === "purchase" });
  const customers = useQuery({ queryKey: ["customer-options"], queryFn: () => listPage<Customer>("/customers", { size: 200 }), enabled: kind === "sales" });
  const products = useQuery({ queryKey: ["product-options"], queryFn: () => listPage<Product>("/products", { size: 300 }) });
  const inventory = useQuery({ queryKey: ["inventory-options"], queryFn: () => listPage<InventoryItem>("/inventory", { size: 500 }) });

  const create = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = {
        orderNumber: values.orderNumber,
        ...(kind === "purchase" ? { supplierId: Number(values.partnerId) } : { customerId: Number(values.partnerId) }),
        items: [
          {
            productId: Number(values.productId),
            binId: Number(values.binId),
            quantity: Number(values.quantity),
            unitPrice: Number(values.unitPrice)
          }
        ]
      };
      return unwrap<Order>(api.post(endpoint, payload));
    },
    onSuccess: () => {
      setDialogOpen(false);
      setSnackbar(t("entities.orderCreated"));
      queryClient.invalidateQueries({ queryKey: [queryKey] });
    }
  });

  const action = useMutation({
    mutationFn: ({ id, actionName }: { id: number; actionName: string }) => unwrap<Order>(api.patch(`${endpoint}/${id}/${actionName}`)),
    onSuccess: () => {
      setSnackbar(t("entities.orderUpdated"));
      queryClient.invalidateQueries({ queryKey: [queryKey] });
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    }
  });

  const partnerOptions =
    kind === "purchase"
      ? (suppliers.data?.content ?? []).map((item) => ({ value: item.id, label: item.companyName }))
      : (customers.data?.content ?? []).map((item) => ({ value: item.id, label: item.companyName || item.fullName }));
  const productOptions = (products.data?.content ?? []).map((item) => ({ value: item.id, label: `${item.sku} - ${item.name}` }));
  const binOptions = (inventory.data?.content ?? []).map((item) => ({ value: item.binId, label: `${item.binCode} (${item.productName})` }));

  const fields: FormField[] = [
    { name: "orderNumber", label: "Order Number", required: true },
    { name: "partnerId", label: kind === "purchase" ? "Supplier" : "Customer", type: "select", required: true, options: partnerOptions },
    { name: "productId", label: "Product", type: "select", required: true, options: productOptions },
    { name: "binId", label: "Bin", type: "select", required: true, options: binOptions },
    { name: "quantity", label: "Quantity", type: "number", required: true },
    { name: "unitPrice", label: "Unit Price", type: "number", required: true }
  ];

  const columns = useMemo<GridColDef<Order>[]>(
    () => [
      { field: "orderNumber", headerName: "Order", width: 170 },
      { field: kind === "purchase" ? "supplierName" : "customerName", headerName: kind === "purchase" ? "Supplier" : "Customer", flex: 1, minWidth: 220 },
      { field: "status", headerName: "Status", width: 140, renderCell: ({ row }) => <StatusChip value={row.status} /> },
      { field: "totalAmount", headerName: "Total", width: 140, valueFormatter: (value) => currency(value as number) },
      { field: "confirmedAt", headerName: "Confirmed", width: 180, valueFormatter: (value) => dateTime(value as string | undefined) },
      { field: kind === "purchase" ? "receivedAt" : "shippedAt", headerName: kind === "purchase" ? "Received" : "Shipped", width: 180, valueFormatter: (value) => dateTime(value as string | undefined) },
      {
        field: "actions",
        headerName: "",
        width: 300,
        sortable: false,
        renderCell: ({ row }) => (
          <Stack direction="row" spacing={1}>
            {row.status === "DRAFT" && (
              <Button size="small" startIcon={<CheckCircleOutlineIcon />} onClick={() => action.mutate({ id: row.id, actionName: "confirm" })}>Confirm</Button>
            )}
            {kind === "purchase" && row.status === "CONFIRMED" && (
              <Button size="small" startIcon={<InventoryOutlinedIcon />} onClick={() => action.mutate({ id: row.id, actionName: "receive" })}>Receive</Button>
            )}
            {kind === "sales" && row.status === "CONFIRMED" && (
              <Button size="small" startIcon={<InventoryOutlinedIcon />} onClick={() => action.mutate({ id: row.id, actionName: "ship" })}>Ship</Button>
            )}
            {row.status !== "CANCELLED" && row.status !== "RECEIVED" && row.status !== "SHIPPED" && (
              <Button size="small" color="error" startIcon={<CancelOutlinedIcon />} onClick={() => action.mutate({ id: row.id, actionName: "cancel" })}>Cancel</Button>
            )}
          </Stack>
        )
      }
    ],
    [action, kind]
  );

  const title = kind === "purchase" ? t("entities.purchaseOrders") : t("entities.salesOrders");

  return (
    <>
      <PageHeader
        title={title}
        subtitle={kind === "purchase" ? t("entities.purchaseOrdersSubtitle") : t("entities.salesOrdersSubtitle")}
        actionLabel={kind === "purchase" ? t("entities.newPurchaseOrder") : t("entities.newSalesOrder")}
        onAction={() => setDialogOpen(true)}
        onRefresh={() => orders.refetch()}
      />
      {(create.error || action.error) && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(create.error ?? action.error)}</Alert>}
      <DataTableShell
        rows={orders.data?.content ?? []}
        columns={columns}
        loading={orders.isLoading || orders.isFetching}
        error={orders.error ? apiMessage(orders.error) : undefined}
        total={orders.data?.totalElements ?? 0}
        paginationModel={paginationModel}
        search={search}
        emptyTitle={t("entities.noOrders")}
        emptyMessage={t("entities.noOrdersMessage")}
        onSearch={setSearch}
        onPaginationModelChange={setPaginationModel}
        onSortModelChange={setSortModel}
      />
      <FormDialog
        open={dialogOpen}
        title={`Create ${title.slice(0, -1)}`}
        fields={fields}
        initialValues={{ orderNumber: "", partnerId: "", productId: "", binId: "", quantity: 1, unitPrice: 0 }}
        loading={create.isPending}
        onClose={() => setDialogOpen(false)}
        onSubmit={(values) => create.mutate(values)}
      />
      <Snackbar open={Boolean(snackbar)} autoHideDuration={2800} onClose={() => setSnackbar(null)} message={snackbar} />
    </>
  );
}
