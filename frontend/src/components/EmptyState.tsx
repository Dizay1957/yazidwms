import { Box, Button, Typography } from "@mui/material";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";

export function EmptyState({ title, message, actionLabel, onAction }: { title: string; message: string; actionLabel?: string; onAction?: () => void }) {
  return (
    <Box sx={{ py: 6, textAlign: "center" }}>
      <InboxOutlinedIcon color="disabled" sx={{ fontSize: 48, mb: 1 }} />
      <Typography variant="h6">{title}</Typography>
      <Typography color="text.secondary" sx={{ mb: actionLabel ? 2 : 0 }}>
        {message}
      </Typography>
      {actionLabel && <Button onClick={onAction} variant="contained">{actionLabel}</Button>}
    </Box>
  );
}
