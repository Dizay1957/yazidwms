import { GridColDef } from "@mui/x-data-grid";
import { EntityPage } from "../features/crud/EntityPage";
import { FormField, FormValues } from "../components/FormDialog";
import { StatusChip } from "../components/StatusChip";
import { useAuth } from "../auth/AuthProvider";
import { canManage } from "../utils/permissions";
import type { Supplier } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

const statusOptions = ["ACTIVE", "INACTIVE", "BLOCKED"].map((value) => ({ value, label: value }));
const fields: FormField[] = [
  { name: "companyName", label: "Company Name", required: true },
  { name: "contactName", label: "Contact Name" },
  { name: "email", label: "Email", type: "email", required: true },
  { name: "phone", label: "Phone" },
  { name: "taxNumber", label: "Tax Number" },
  { name: "country", label: "Country" },
  { name: "city", label: "City" },
  { name: "address", label: "Address", multiline: true },
  { name: "status", label: "Status", type: "select", required: true, options: statusOptions }
];

const columns: GridColDef<Supplier>[] = [
  { field: "companyName", headerName: "Company", flex: 1, minWidth: 220 },
  { field: "contactName", headerName: "Contact", width: 160 },
  { field: "email", headerName: "Email", width: 220 },
  { field: "city", headerName: "City", width: 140 },
  { field: "status", headerName: "Business Status", width: 150, renderCell: ({ row }) => <StatusChip value={row.status} /> },
  { field: "active", headerName: "Record", width: 120, renderCell: ({ row }) => <StatusChip value={row.active} /> }
];

export function SuppliersPage() {
  const auth = useAuth();
  const { t } = useI18n();
  return (
    <EntityPage<Supplier>
      title={t("entities.suppliers")}
      subtitle={t("entities.suppliersSubtitle")}
      endpoint="/suppliers"
      queryKey="suppliers"
      columns={columns}
      fields={fields}
      createLabel={t("entities.newSupplier")}
      canWrite={canManage(auth.roles)}
      defaultValues={{ companyName: "", contactName: "", email: "", phone: "", taxNumber: "", country: "", city: "", address: "", status: "ACTIVE" }}
      toFormValues={(row) => ({ ...row })}
      toPayload={(values: FormValues) => ({ ...values })}
    />
  );
}
