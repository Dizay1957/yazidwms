import { Alert, IconButton, Snackbar, Stack, Tooltip } from "@mui/material";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import { GridColDef, GridPaginationModel, GridSortModel } from "@mui/x-data-grid";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { createOne, deleteOne, listPage, updateOne } from "../../api/endpoints";
import { apiMessage } from "../../api/client";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import { DataTableShell } from "../../components/DataTableShell";
import { FormDialog, FormField, FormValues } from "../../components/FormDialog";
import { PageHeader } from "../../components/PageHeader";
import { useDebouncedValue } from "../../hooks/useDebouncedValue";
import { useI18n } from "../../i18n/I18nProvider";

export interface EntityPageConfig<T extends { id: number }> {
  title: string;
  subtitle: string;
  endpoint: string;
  queryKey: string;
  columns: GridColDef<T>[];
  fields: FormField[];
  defaultValues: FormValues;
  toFormValues: (row: T) => FormValues;
  toPayload: (values: FormValues) => Record<string, unknown>;
  createLabel?: string;
  canWrite?: boolean;
}

export function EntityPage<T extends { id: number }>(config: EntityPageConfig<T>) {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [sortModel, setSortModel] = useState<GridSortModel>([]);
  const [editing, setEditing] = useState<T | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<T | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [snackbar, setSnackbar] = useState<string | null>(null);
  const debouncedSearch = useDebouncedValue(search);

  const sort = sortModel[0] ? `${sortModel[0].field},${sortModel[0].sort ?? "asc"}` : undefined;
  const queryKey = [config.queryKey, paginationModel.page, paginationModel.pageSize, debouncedSearch, sort];
  const pageQuery = useQuery({
    queryKey,
    queryFn: () =>
      listPage<T>(config.endpoint, {
        page: paginationModel.page,
        size: paginationModel.pageSize,
        q: debouncedSearch,
        sort
      })
  });

  const saveMutation = useMutation({
    mutationFn: (values: FormValues) => {
      const payload = config.toPayload(values);
      return editing ? updateOne<T, typeof payload>(config.endpoint, editing.id, payload) : createOne<T, typeof payload>(config.endpoint, payload);
    },
    onSuccess: () => {
      setDialogOpen(false);
      setEditing(null);
      setSnackbar(editing ? t("common.recordUpdated") : t("common.recordCreated"));
      queryClient.invalidateQueries({ queryKey: [config.queryKey] });
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (row: T) => deleteOne(config.endpoint, row.id),
    onSuccess: () => {
      setDeleteTarget(null);
      setSnackbar(t("common.recordDeleted"));
      queryClient.invalidateQueries({ queryKey: [config.queryKey] });
    }
  });

  const columns = useMemo<GridColDef<T>[]>(() => {
    if (!config.canWrite) {
      return config.columns;
    }
    return [
      ...config.columns,
      {
        field: "actions",
        headerName: "",
        width: 100,
        sortable: false,
        filterable: false,
        renderCell: ({ row }) => (
          <Stack direction="row">
            <Tooltip title={t("common.edit")}>
              <IconButton
                size="small"
                onClick={() => {
                  setEditing(row);
                  setDialogOpen(true);
                }}
              >
                <EditOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title={t("common.delete")}>
              <IconButton size="small" color="error" onClick={() => setDeleteTarget(row)}>
                <DeleteOutlineIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Stack>
        )
      }
    ];
  }, [config, t]);

  const currentValues = editing ? config.toFormValues(editing) : config.defaultValues;
  const error = pageQuery.error ? apiMessage(pageQuery.error) : undefined;
  const mutationError = saveMutation.error ? apiMessage(saveMutation.error) : deleteMutation.error ? apiMessage(deleteMutation.error) : undefined;

  return (
    <>
      <PageHeader
        title={config.title}
        subtitle={config.subtitle}
        actionLabel={config.canWrite ? config.createLabel ?? t("common.create") : undefined}
        onAction={() => {
          setEditing(null);
          setDialogOpen(true);
        }}
        onRefresh={() => pageQuery.refetch()}
      />
      {mutationError && <Alert severity="error" sx={{ mb: 2 }}>{mutationError}</Alert>}
      <DataTableShell
        rows={pageQuery.data?.content ?? []}
        columns={columns}
        loading={pageQuery.isLoading || pageQuery.isFetching}
        error={error}
        total={pageQuery.data?.totalElements ?? 0}
        paginationModel={paginationModel}
        search={search}
        emptyTitle={t("common.noRecords")}
        emptyMessage={t("common.emptyAction")}
        onSearch={(value) => {
          setSearch(value);
          setPaginationModel((current) => ({ ...current, page: 0 }));
        }}
        onPaginationModelChange={setPaginationModel}
        onSortModelChange={setSortModel}
      />

      <FormDialog
        open={dialogOpen}
        title={editing ? `${t("common.edit")} ${config.title}` : config.createLabel ?? `${t("common.create")} ${config.title}`}
        fields={config.fields}
        initialValues={currentValues}
        loading={saveMutation.isPending}
        onClose={() => {
          setDialogOpen(false);
          setEditing(null);
        }}
        onSubmit={(values) => saveMutation.mutate(values)}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title={t("common.deleteRecord")}
        message={t("common.deleteMessage")}
        loading={deleteMutation.isPending}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget)}
      />

      <Snackbar open={Boolean(snackbar)} autoHideDuration={2800} onClose={() => setSnackbar(null)} message={snackbar} />
    </>
  );
}
