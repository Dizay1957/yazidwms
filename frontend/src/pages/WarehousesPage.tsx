import { GridColDef } from "@mui/x-data-grid";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Box, Card, CardContent, Grid, List, ListItemButton, ListItemText, Typography } from "@mui/material";
import { useState } from "react";
import { api, apiMessage, unwrap } from "../api/client";
import { listPage } from "../api/endpoints";
import { EntityPage } from "../features/crud/EntityPage";
import { FormDialog, FormField, FormValues } from "../components/FormDialog";
import { StatusChip } from "../components/StatusChip";
import { useAuth } from "../auth/AuthProvider";
import { canManage } from "../utils/permissions";
import { PageHeader } from "../components/PageHeader";
import type { Bin, LocationNode, Warehouse } from "../types/api";

const fields: FormField[] = [
  { name: "code", label: "Warehouse Code", required: true },
  { name: "name", label: "Name", required: true },
  { name: "country", label: "Country" },
  { name: "city", label: "City" },
  { name: "address", label: "Address", multiline: true }
];

const columns: GridColDef<Warehouse>[] = [
  { field: "code", headerName: "Code", width: 160 },
  { field: "name", headerName: "Warehouse", flex: 1, minWidth: 220 },
  { field: "city", headerName: "City", width: 150 },
  { field: "country", headerName: "Country", width: 150 },
  { field: "address", headerName: "Address", flex: 1, minWidth: 260 },
  { field: "active", headerName: "Status", width: 120, renderCell: ({ row }) => <StatusChip value={row.active} /> }
];

export function WarehousesPage() {
  const auth = useAuth();
  return (
    <>
      <EntityPage<Warehouse>
        title="Warehouses"
        subtitle="Manage facilities and their physical topology: zones, aisles, shelves, and bins."
        endpoint="/warehouses"
        queryKey="warehouses"
        columns={columns}
        fields={fields}
        createLabel="New warehouse"
        canWrite={canManage(auth.roles)}
        defaultValues={{ code: "", name: "", country: "", city: "", address: "" }}
        toFormValues={(row) => ({ code: row.code, name: row.name, country: row.country ?? "", city: row.city ?? "", address: row.address ?? "" })}
        toPayload={(values: FormValues) => ({ ...values })}
      />
      <Box sx={{ mt: 3 }}>
        <WarehouseTopology canWrite={canManage(auth.roles)} />
      </Box>
    </>
  );
}

function WarehouseTopology({ canWrite }: { canWrite: boolean }) {
  const queryClient = useQueryClient();
  const [warehouseId, setWarehouseId] = useState<number | null>(null);
  const [zoneId, setZoneId] = useState<number | null>(null);
  const [aisleId, setAisleId] = useState<number | null>(null);
  const [shelfId, setShelfId] = useState<number | null>(null);
  const [dialog, setDialog] = useState<"zone" | "aisle" | "shelf" | "bin" | null>(null);

  const warehouses = useQuery({ queryKey: ["warehouse-topology-options"], queryFn: () => listPage<Warehouse>("/warehouses", { size: 200 }) });
  const zones = useQuery({ queryKey: ["zones", warehouseId], queryFn: () => unwrap<LocationNode[]>(api.get(`/warehouses/${warehouseId}/zones`)), enabled: Boolean(warehouseId) });
  const aisles = useQuery({ queryKey: ["aisles", zoneId], queryFn: () => unwrap<LocationNode[]>(api.get(`/warehouses/zones/${zoneId}/aisles`)), enabled: Boolean(zoneId) });
  const shelves = useQuery({ queryKey: ["shelves", aisleId], queryFn: () => unwrap<LocationNode[]>(api.get(`/warehouses/aisles/${aisleId}/shelves`)), enabled: Boolean(aisleId) });
  const bins = useQuery({ queryKey: ["bins", shelfId], queryFn: () => unwrap<Bin[]>(api.get(`/warehouses/shelves/${shelfId}/bins`)), enabled: Boolean(shelfId) });

  const create = useMutation({
    mutationFn: (values: FormValues) => {
      if (dialog === "zone") return unwrap(api.post("/warehouses/zones", { code: values.code, name: values.name, warehouseId }));
      if (dialog === "aisle") return unwrap(api.post("/warehouses/aisles", { code: values.code, zoneId }));
      if (dialog === "shelf") return unwrap(api.post("/warehouses/shelves", { code: values.code, aisleId }));
      return unwrap(api.post("/warehouses/bins", { code: values.code, capacity: Number(values.capacity), shelfId }));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["zones"] });
      queryClient.invalidateQueries({ queryKey: ["aisles"] });
      queryClient.invalidateQueries({ queryKey: ["shelves"] });
      queryClient.invalidateQueries({ queryKey: ["bins"] });
      setDialog(null);
    }
  });

  const fieldSets: Record<string, FormField[]> = {
    zone: [{ name: "code", label: "Zone Code", required: true }, { name: "name", label: "Zone Name", required: true }],
    aisle: [{ name: "code", label: "Aisle Code", required: true }],
    shelf: [{ name: "code", label: "Shelf Code", required: true }],
    bin: [{ name: "code", label: "Bin Code", required: true }, { name: "capacity", label: "Capacity", type: "number", required: true }]
  };

  return (
    <>
      <PageHeader
        title="Warehouse Topology"
        subtitle="Create and inspect zones, aisles, shelves, and bins with live backend calls."
        actionLabel={canWrite && warehouseId ? "Add location" : undefined}
        onAction={() => setDialog(zoneId ? (aisleId ? (shelfId ? "bin" : "shelf") : "aisle") : "zone")}
      />
      {create.error && <Alert severity="error" sx={{ mb: 2 }}>{apiMessage(create.error)}</Alert>}
      <Grid container spacing={2}>
        <TopologyList title="Warehouses" rows={warehouses.data?.content ?? []} selectedId={warehouseId} onSelect={(id) => { setWarehouseId(id); setZoneId(null); setAisleId(null); setShelfId(null); }} />
        <TopologyList title="Zones" rows={zones.data ?? []} selectedId={zoneId} onSelect={(id) => { setZoneId(id); setAisleId(null); setShelfId(null); }} disabled={!warehouseId} />
        <TopologyList title="Aisles" rows={aisles.data ?? []} selectedId={aisleId} onSelect={(id) => { setAisleId(id); setShelfId(null); }} disabled={!zoneId} />
        <TopologyList title="Shelves" rows={shelves.data ?? []} selectedId={shelfId} onSelect={setShelfId} disabled={!aisleId} />
        <Grid item xs={12} lg={2.4}>
          <Card>
            <CardContent>
              <Typography variant="h6">Bins</Typography>
              <List dense>
                {(bins.data ?? []).map((bin) => (
                  <ListItemButton key={bin.id}>
                    <ListItemText primary={bin.code} secondary={`Capacity ${bin.capacity}`} />
                  </ListItemButton>
                ))}
                {shelfId && (bins.data ?? []).length === 0 && <Typography color="text.secondary">No bins.</Typography>}
                {!shelfId && <Typography color="text.secondary">Select a shelf.</Typography>}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
      <FormDialog
        open={Boolean(dialog)}
        title={`Create ${dialog ?? "location"}`}
        fields={fieldSets[dialog ?? "zone"]}
        initialValues={{ code: "", name: "", capacity: 1 }}
        loading={create.isPending}
        onClose={() => setDialog(null)}
        onSubmit={(values) => create.mutate(values)}
      />
    </>
  );
}

function TopologyList({
  title,
  rows,
  selectedId,
  disabled,
  onSelect
}: {
  title: string;
  rows: Array<{ id: number; code: string; name?: string }>;
  selectedId: number | null;
  disabled?: boolean;
  onSelect: (id: number) => void;
}) {
  return (
    <Grid item xs={12} md={6} lg={2.4}>
      <Card>
        <CardContent>
          <Typography variant="h6">{title}</Typography>
          <List dense>
            {rows.map((row) => (
              <ListItemButton key={row.id} selected={selectedId === row.id} onClick={() => onSelect(row.id)}>
                <ListItemText primary={row.code} secondary={row.name} />
              </ListItemButton>
            ))}
            {rows.length === 0 && <Typography color="text.secondary">{disabled ? `Select parent ${title.toLowerCase()}.` : "No records."}</Typography>}
          </List>
        </CardContent>
      </Card>
    </Grid>
  );
}
