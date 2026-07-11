import { GridColDef, GridPaginationModel, GridSortModel } from "@mui/x-data-grid";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { listPage } from "../api/endpoints";
import { apiMessage } from "../api/client";
import { DataTableShell } from "../components/DataTableShell";
import { PageHeader } from "../components/PageHeader";
import { StatusChip } from "../components/StatusChip";
import { dateTime } from "../utils/format";
import type { StockMovement } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

const columns: GridColDef<StockMovement>[] = [
  { field: "timestamp", headerName: "Timestamp", width: 190, valueFormatter: (value) => dateTime(value as string) },
  { field: "type", headerName: "Type", width: 140, renderCell: ({ row }) => <StatusChip value={row.type} /> },
  { field: "sku", headerName: "SKU", width: 160 },
  { field: "quantity", headerName: "Qty", type: "number", width: 100 },
  { field: "fromBinId", headerName: "From Bin", width: 110 },
  { field: "toBinId", headerName: "To Bin", width: 110 },
  { field: "reference", headerName: "Reference", width: 180 },
  { field: "reason", headerName: "Reason", flex: 1, minWidth: 220 },
  { field: "notes", headerName: "Notes", flex: 1, minWidth: 220 }
];

export function MovementsPage() {
  const { t } = useI18n();
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [sortModel, setSortModel] = useState<GridSortModel>([]);
  const sort = sortModel[0] ? `${sortModel[0].field},${sortModel[0].sort ?? "asc"}` : undefined;
  const movements = useQuery({
    queryKey: ["movements", paginationModel.page, paginationModel.pageSize, sort],
    queryFn: () => listPage<StockMovement>("/inventory/movements", { page: paginationModel.page, size: paginationModel.pageSize, sort })
  });

  return (
    <>
      <PageHeader title={t("entities.movements")} subtitle={t("entities.movementsSubtitle")} onRefresh={() => movements.refetch()} />
      <DataTableShell
        rows={movements.data?.content ?? []}
        columns={columns}
        loading={movements.isLoading || movements.isFetching}
        error={movements.error ? apiMessage(movements.error) : undefined}
        total={movements.data?.totalElements ?? 0}
        paginationModel={paginationModel}
        search={search}
        emptyTitle={t("entities.noMovements")}
        emptyMessage={t("entities.noMovementsMessage")}
        onSearch={setSearch}
        onPaginationModelChange={setPaginationModel}
        onSortModelChange={setSortModel}
      />
    </>
  );
}
