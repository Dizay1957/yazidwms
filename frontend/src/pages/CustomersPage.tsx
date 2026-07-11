import { GridColDef } from "@mui/x-data-grid";
import { EntityPage } from "../features/crud/EntityPage";
import { FormField, FormValues } from "../components/FormDialog";
import { StatusChip } from "../components/StatusChip";
import { useAuth } from "../auth/AuthProvider";
import { canManage } from "../utils/permissions";
import type { Customer } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

const fields: FormField[] = [
  { name: "customerType", label: "Customer Type", type: "select", required: true, options: ["COMPANY", "INDIVIDUAL"].map((value) => ({ value, label: value })) },
  { name: "companyName", label: "Company Name" },
  { name: "fullName", label: "Full Name", required: true },
  { name: "email", label: "Email", type: "email", required: true },
  { name: "phone", label: "Phone" },
  { name: "country", label: "Country" },
  { name: "city", label: "City" },
  { name: "address", label: "Address", multiline: true },
  { name: "status", label: "Status", type: "select", required: true, options: ["ACTIVE", "INACTIVE", "BLOCKED"].map((value) => ({ value, label: value })) }
];

const columns: GridColDef<Customer>[] = [
  { field: "fullName", headerName: "Customer", flex: 1, minWidth: 220 },
  { field: "companyName", headerName: "Company", width: 180 },
  { field: "email", headerName: "Email", width: 220 },
  { field: "customerType", headerName: "Type", width: 130, renderCell: ({ row }) => <StatusChip value={row.customerType} /> },
  { field: "status", headerName: "Business Status", width: 150, renderCell: ({ row }) => <StatusChip value={row.status} /> },
  { field: "active", headerName: "Record", width: 120, renderCell: ({ row }) => <StatusChip value={row.active} /> }
];

export function CustomersPage() {
  const auth = useAuth();
  const { t } = useI18n();
  return (
    <EntityPage<Customer>
      title={t("entities.customers")}
      subtitle={t("entities.customersSubtitle")}
      endpoint="/customers"
      queryKey="customers"
      columns={columns}
      fields={fields}
      createLabel={t("entities.newCustomer")}
      canWrite={canManage(auth.roles)}
      defaultValues={{ customerType: "COMPANY", companyName: "", fullName: "", email: "", phone: "", country: "", city: "", address: "", status: "ACTIVE" }}
      toFormValues={(row) => ({ ...row })}
      toPayload={(values: FormValues) => ({ ...values })}
    />
  );
}
