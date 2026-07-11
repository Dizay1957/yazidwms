import { Chip, ChipProps } from "@mui/material";
import { labelize } from "../utils/format";

const colors: Record<string, ChipProps["color"]> = {
  ACTIVE: "success",
  RECEIVED: "success",
  SHIPPED: "success",
  CONFIRMED: "info",
  DRAFT: "default",
  PENDING_ACTIVATION: "warning",
  DISABLED: "error",
  CANCELLED: "error",
  BLOCKED: "error",
  INACTIVE: "default",
  LOW: "warning"
};

export function StatusChip({ value }: { value?: string | boolean }) {
  const normalized = typeof value === "boolean" ? (value ? "ACTIVE" : "INACTIVE") : value;
  return <Chip size="small" label={labelize(normalized)} color={colors[normalized ?? ""] ?? "default"} variant="outlined" />;
}
