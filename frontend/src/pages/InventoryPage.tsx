import { Alert, Button, Snackbar, Stack } from "@mui/material";
import { GridColDef, GridPaginationModel, GridSortModel } from "@mui/x-data-grid";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import SwapHorizOutlinedIcon from "@mui/icons-material/SwapHorizOutlined";
import TuneOutlinedIcon from "@mui/icons-material/TuneOutlined";
import { api, apiMessage, unwrap } from "../api/client";
import { listPage } from "../api/endpoints";
import { DataTableShell } from "../components/DataTableShell";
import { FormDialog, FormField, FormValues } from "../components/FormDialog";
import { PageHeader } from "../components/PageHeader";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import type { InventoryItem } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

const columns = (onAdjust: (row: InventoryItem) => void, onTransfer: (row: InventoryItem) => void, labels: { adjust: string; transfer: string }): GridColDef<InventoryItem>[] => [
  { field: "sku", headerName: "SKU", width: 160 },
  { field: "productName", headerName: "Product", flex: 1, minWidth: 240 },
  { field: "binCode", headerName: "Bin", width: 160 },
  { field: "quantity", headerName: "Quantity", type: "number", width: 130 },
  {
    field: "actions",
    headerName: "",
    width: 190,
    sortable: false,
    renderCell: ({ row }) => (
      <Stack direction="row" spacing={1}>
        <Button size="small" startIcon={<TuneOutlinedIcon />} onClick={() => onAdjust(row)}>{labels.adjust}</Button>
        <Button size="small" startIcon={<SwapHorizOutlinedIcon />} onClick={() => onTransfer(row)}>{labels.transfer}</Button>
      </Stack>
    )
  }
];

export function InventoryPage() {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [sortModel, setSortModel] = useState<GridSortModel>([]);
  const [adjusting, setAdjusting] = useState<InventoryItem | null>(null);
  const [transferring, setTransferring] = useState<InventoryItem | null>(null);
  const [snackbar, setSnackbar] = useState<string | null>(null);
  const debouncedSearch = useDebouncedValue(search);
  const sort = sortModel[0] ? `${sortModel[0].field},${sortModel[0].sort ?? "asc"}` : undefined;

  const inventory = useQuery({
    queryKey: ["inventory", paginationModel.page, paginationModel.pageSize, debouncedSearch, sort],
    queryFn: () => listPage<InventoryItem>("/inventory", { page: paginationModel.page, size: paginationModel.pageSize, q: debouncedSearch, sort })
  });
  const inventoryOptions = useQuery({ queryKey: ["inventory-options"], queryFn: () => listPage<InventoryItem>("/inventory", { size: 500 }) });

  const adjust = useMutation({
    mutationFn: (values: FormValues) =>
      unwrap<InventoryItem>(api.patch("/inventory/adjust", {
        productId: Number(values.productId),
        binId: Number(values.binId),
        newQuantity: Number(values.newQuantity),
        reason: values.reason,
        notes: values.notes
      })),
    onSuccess: () => {
      setAdjusting(null);
      setSnackbar(t("entities.inventoryAdjusted"));
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
    }
  });

  const transfer = useMutation({
    mutationFn: (values: FormValues) =>
      unwrap<void>(api.post("/inventory/transfer", {
        productId: Number(values.productId),
        fromBinId: Number(values.fromBinId),
        toBinId: Number(values.toBinId),
        quantity: Number(values.quantity),
        reason: values.reason,
        notes: values.notes
      })),
    onSuccess: () => {
      setTransferring(null);
      setSnackbar(t("entities.inventoryTransferred"));
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
      queryClient.invalidateQueries({ queryKey: ["movements"] });
    }
  });

  const binOptions = (inventoryOptions.data?.content ?? []).map((item) => ({ value: item.binId, label: `${item.binCode} - ${item.productName}` }));
  const error = inventory.error ? apiMessage(inventory.error) : undefined;
  const mutationError = adjust.error ? apiMessage(adjust.error) : transfer.error ? apiMessage(transfer.error) : undefined;

  return (
    <>
      <PageHeader title={t("entities.inventory")} subtitle={t("entities.inventorySubtitle")} onRefresh={() => inventory.refetch()} />
      {mutationError && <Alert severity="error" sx={{ mb: 2 }}>{mutationError}</Alert>}
      <DataTableShell
        rows={inventory.data?.content ?? []}
        columns={columns(setAdjusting, setTransferring, { adjust: t("entities.adjust"), transfer: t("entities.transfer") })}
        loading={inventory.isLoading || inventory.isFetching}
        error={error}
        total={inventory.data?.totalElements ?? 0}
        paginationModel={paginationModel}
        search={search}
        emptyTitle={t("entities.noInventory")}
        emptyMessage={t("entities.noInventoryMessage")}
        onSearch={(value) => {
          setSearch(value);
          setPaginationModel((current) => ({ ...current, page: 0 }));
        }}
        onPaginationModelChange={setPaginationModel}
        onSortModelChange={setSortModel}
      />

      <FormDialog
        open={Boolean(adjusting)}
        title={t("entities.adjustInventory")}
        fields={[
          { name: "newQuantity", label: "New Quantity", type: "number", required: true },
          { name: "reason", label: "Reason", required: true },
          { name: "notes", label: "Notes", multiline: true }
        ]}
        initialValues={{ productId: adjusting?.productId, binId: adjusting?.binId, newQuantity: adjusting?.quantity ?? 0, reason: "Cycle count adjustment", notes: "" }}
        loading={adjust.isPending}
        onClose={() => setAdjusting(null)}
        onSubmit={(values) => adjust.mutate(values)}
      />

      <FormDialog
        open={Boolean(transferring)}
        title={t("entities.transferInventory")}
        fields={[
          { name: "toBinId", label: "Destination Bin", type: "select", required: true, options: binOptions },
          { name: "quantity", label: "Quantity", type: "number", required: true },
          { name: "reason", label: "Reason", required: true },
          { name: "notes", label: "Notes", multiline: true }
        ]}
        initialValues={{ productId: transferring?.productId, fromBinId: transferring?.binId, toBinId: "", quantity: 1, reason: "Warehouse transfer", notes: "" }}
        loading={transfer.isPending}
        onClose={() => setTransferring(null)}
        onSubmit={(values) => transfer.mutate(values)}
      />

      <Snackbar open={Boolean(snackbar)} autoHideDuration={2800} onClose={() => setSnackbar(null)} message={snackbar} />
    </>
  );
}
