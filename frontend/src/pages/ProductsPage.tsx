import { GridColDef } from "@mui/x-data-grid";
import { useQuery } from "@tanstack/react-query";
import { listPage } from "../api/endpoints";
import { EntityPage } from "../features/crud/EntityPage";
import { FormField, FormValues } from "../components/FormDialog";
import { StatusChip } from "../components/StatusChip";
import { currency } from "../utils/format";
import { canManage } from "../utils/permissions";
import { useAuth } from "../auth/AuthProvider";
import type { Category, Product, Supplier } from "../types/api";
import { useI18n } from "../i18n/I18nProvider";

const columns: GridColDef<Product>[] = [
  { field: "sku", headerName: "SKU", width: 150 },
  { field: "name", headerName: "Product", flex: 1, minWidth: 220 },
  { field: "categoryName", headerName: "Category", width: 160 },
  { field: "supplierName", headerName: "Supplier", width: 180 },
  { field: "quantity", headerName: "Qty", type: "number", width: 90 },
  { field: "minimumQuantity", headerName: "Min", type: "number", width: 90 },
  { field: "sellingPrice", headerName: "Sell Price", width: 130, valueFormatter: (value) => currency(value as number) },
  { field: "lowStock", headerName: "Stock", width: 120, renderCell: ({ row }) => (row.lowStock ? <StatusChip value="LOW" /> : <StatusChip value="ACTIVE" />) },
  { field: "active", headerName: "Status", width: 120, renderCell: ({ row }) => <StatusChip value={row.active} /> }
];

export function ProductsPage() {
  const auth = useAuth();
  const { t } = useI18n();
  const categories = useQuery({ queryKey: ["category-options"], queryFn: () => listPage<Category>("/categories", { size: 200 }) });
  const suppliers = useQuery({ queryKey: ["supplier-options"], queryFn: () => listPage<Supplier>("/suppliers", { size: 200 }) });

  const fields: FormField[] = [
    { name: "sku", label: "SKU", required: true },
    { name: "barcode", label: "Barcode", required: true },
    { name: "name", label: "Name", required: true },
    { name: "description", label: "Description", multiline: true },
    { name: "categoryId", label: "Category", type: "select", required: true, options: (categories.data?.content ?? []).map((item) => ({ value: item.id, label: item.name })) },
    { name: "supplierId", label: "Supplier", type: "select", required: true, options: (suppliers.data?.content ?? []).map((item) => ({ value: item.id, label: item.companyName })) },
    { name: "purchasePrice", label: "Purchase Price", type: "number", required: true },
    { name: "sellingPrice", label: "Selling Price", type: "number", required: true },
    { name: "unit", label: "Unit", required: true },
    { name: "weight", label: "Weight", type: "number", required: true },
    { name: "minimumQuantity", label: "Minimum Quantity", type: "number", required: true },
    { name: "maximumQuantity", label: "Maximum Quantity", type: "number", required: true }
  ];

  return (
    <EntityPage<Product>
      title={t("entities.products")}
      subtitle={t("entities.productsSubtitle")}
      endpoint="/products"
      queryKey="products"
      columns={columns}
      fields={fields}
      createLabel={t("entities.newProduct")}
      canWrite={canManage(auth.roles)}
      defaultValues={{ sku: "", barcode: "", name: "", description: "", categoryId: "", supplierId: "", purchasePrice: 0, sellingPrice: 0, unit: "PCS", weight: 0, minimumQuantity: 0, maximumQuantity: 0 }}
      toFormValues={(row) => ({ ...row })}
      toPayload={(values: FormValues) => ({
        sku: values.sku,
        barcode: values.barcode,
        name: values.name,
        description: values.description,
        categoryId: Number(values.categoryId),
        supplierId: Number(values.supplierId),
        purchasePrice: Number(values.purchasePrice),
        sellingPrice: Number(values.sellingPrice),
        unit: values.unit,
        weight: Number(values.weight),
        minimumQuantity: Number(values.minimumQuantity),
        maximumQuantity: Number(values.maximumQuantity),
        active: true
      })}
    />
  );
}
