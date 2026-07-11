import { Card, CardContent, Stack, Typography } from "@mui/material";
import { ReactNode } from "react";

export function StatCard({ label, value, icon, tone = "primary" }: { label: string; value: string | number; icon: ReactNode; tone?: "primary" | "secondary" | "warning" | "success" }) {
  return (
    <Card>
      <CardContent>
        <Stack direction="row" spacing={2} alignItems="center">
          <Stack alignItems="center" justifyContent="center" sx={{ width: 44, height: 44, borderRadius: 2, bgcolor: `${tone}.main`, color: `${tone}.contrastText` }}>
            {icon}
          </Stack>
          <Stack>
            <Typography color="text.secondary" variant="body2">{label}</Typography>
            <Typography variant="h5">{value}</Typography>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
}
