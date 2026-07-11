import { GridColDef } from "@mui/x-data-grid";
import { useQuery } from "@tanstack/react-query";
import { api, unwrap } from "../api/client";
import { EntityPage } from "../features/crud/EntityPage";
import { FormField, FormValues } from "../components/FormDialog";
import { StatusChip } from "../components/StatusChip";
import { dateTime } from "../utils/format";
import type { Role, User } from "../types/api";

const columns: GridColDef<User>[] = [
  { field: "fullName", headerName: "Name", flex: 1, minWidth: 220 },
  { field: "email", headerName: "Email", width: 240 },
  { field: "phone", headerName: "Phone", width: 150 },
  { field: "roles", headerName: "Roles", width: 260, valueGetter: (_value, row) => row.roles.join(", ") },
  { field: "status", headerName: "Status", width: 160, renderCell: ({ row }) => <StatusChip value={row.status} /> },
  { field: "lastLoginAt", headerName: "Last Login", width: 190, valueFormatter: (value) => dateTime(value as string | undefined) }
];

export function UsersPage() {
  const roles = useQuery({ queryKey: ["roles"], queryFn: () => unwrap<Role[]>(api.get("/roles")) });
  const roleOptions = (roles.data ?? []).map((role) => ({ value: role.name, label: role.name.replace(/_/g, " ") }));
  const fields: FormField[] = [
    { name: "fullName", label: "Full Name", required: true },
    { name: "email", label: "Email", type: "email", required: true },
    { name: "password", label: "Password", type: "password" },
    { name: "phone", label: "Phone" },
    { name: "roles", label: "Roles", type: "multiselect", required: true, options: roleOptions },
    { name: "status", label: "Status", type: "select", required: true, options: ["ACTIVE", "PENDING_ACTIVATION", "DISABLED"].map((value) => ({ value, label: value })) }
  ];

  return (
    <EntityPage<User>
      title="User Management"
      subtitle="Manage users, roles, status, and access boundaries."
      endpoint="/users"
      queryKey="users"
      columns={columns}
      fields={fields}
      createLabel="New user"
      canWrite
      defaultValues={{ fullName: "", email: "", password: "AdminPass1", phone: "", roles: ["VIEWER"], status: "ACTIVE" }}
      toFormValues={(row) => ({ fullName: row.fullName, email: row.email, password: "", phone: row.phone ?? "", roles: row.roles, status: row.status })}
      toPayload={(values: FormValues) => ({
        fullName: values.fullName,
        email: values.email,
        password: values.password || "AdminPass1",
        phone: values.phone,
        roles: values.roles,
        status: values.status
      })}
    />
  );
}
