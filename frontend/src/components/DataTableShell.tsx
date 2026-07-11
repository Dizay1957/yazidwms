import { Alert, Box, Card, CardContent, LinearProgress, Stack, TextField } from "@mui/material";
import { DataGrid, GridColDef, GridPaginationModel, GridSortModel } from "@mui/x-data-grid";
import { EmptyState } from "./EmptyState";

export function DataTableShell<T extends { id: number }>({
  rows,
  columns,
  loading,
  error,
  total,
  paginationModel,
  search,
  emptyTitle,
  emptyMessage,
  onSearch,
  onPaginationModelChange,
  onSortModelChange
}: {
  rows: T[];
  columns: GridColDef<T>[];
  loading?: boolean;
  error?: string;
  total: number;
  paginationModel: GridPaginationModel;
  search: string;
  emptyTitle: string;
  emptyMessage: string;
  onSearch: (value: string) => void;
  onPaginationModelChange: (model: GridPaginationModel) => void;
  onSortModelChange: (model: GridSortModel) => void;
}) {
  return (
    <Card>
      {loading && <LinearProgress />}
      <CardContent>
        <Stack spacing={2}>
          <TextField
            value={search}
            onChange={(event) => onSearch(event.target.value)}
            label="Search"
            placeholder="Filter records"
            size="small"
            sx={{ maxWidth: 360 }}
          />
          {error && <Alert severity="error">{error}</Alert>}
          {!loading && !error && rows.length === 0 ? (
            <EmptyState title={emptyTitle} message={emptyMessage} />
          ) : (
            <Box sx={{ width: "100%" }}>
              <DataGrid
                rows={rows}
                columns={columns}
                autoHeight
                disableRowSelectionOnClick
                paginationMode="server"
                sortingMode="server"
                rowCount={total}
                paginationModel={paginationModel}
                onPaginationModelChange={onPaginationModelChange}
                onSortModelChange={onSortModelChange}
                pageSizeOptions={[10, 25, 50]}
                loading={loading}
                sx={{ minHeight: 420 }}
              />
            </Box>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
}
