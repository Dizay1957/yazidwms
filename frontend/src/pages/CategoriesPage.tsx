import { GridColDef } from "@mui/x-data-grid";
import { useQuery } from "@tanstack/react-query";
import { listPage } from "../api/endpoints";
import { EntityPage } from "../features/crud/EntityPage";
import { FormField, FormValues } from "../components/FormDialog";
import { StatusChip } from "../components/StatusChip";
import { useAuth } from "../auth/AuthProvider";
import { canManage } from "../utils/permissions";
import type { Category } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

const columns: GridColDef<Category>[] = [
  { field: "name", headerName: "Category", flex: 1, minWidth: 220 },
  { field: "parentName", headerName: "Parent", width: 180 },
  { field: "description", headerName: "Description", flex: 1, minWidth: 260 },
  { field: "active", headerName: "Status", width: 120, renderCell: ({ row }) => <StatusChip value={row.active} /> }
];

export function CategoriesPage() {
  const auth = useAuth();
  const { t } = useI18n();
  const categoryOptions = useQuery({ queryKey: ["category-options-all"], queryFn: () => listPage<Category>("/categories", { size: 200 }) });
  const fields: FormField[] = [
    { name: "name", label: "Name", required: true },
    { name: "description", label: "Description", multiline: true },
    { name: "parentId", label: "Parent Category", type: "select", options: [{ value: "", label: "None" }, ...(categoryOptions.data?.content ?? []).map((item) => ({ value: item.id, label: item.name }))] }
  ];

  return (
    <EntityPage<Category>
      title={t("entities.categories")}
      subtitle={t("entities.categoriesSubtitle")}
      endpoint="/categories"
      queryKey="categories"
      columns={columns}
      fields={fields}
      createLabel={t("entities.newCategory")}
      canWrite={canManage(auth.roles)}
      defaultValues={{ name: "", description: "", parentId: "" }}
      toFormValues={(row) => ({ name: row.name, description: row.description ?? "", parentId: row.parentId ?? "" })}
      toPayload={(values: FormValues) => ({ name: values.name, description: values.description, parentId: values.parentId ? Number(values.parentId) : null })}
    />
  );
}
