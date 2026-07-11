import {
  AppBar,
  Avatar,
  Badge,
  Box,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Stack,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery
} from "@mui/material";
import DashboardIcon from "@mui/icons-material/Dashboard";
import Inventory2OutlinedIcon from "@mui/icons-material/Inventory2Outlined";
import CategoryOutlinedIcon from "@mui/icons-material/CategoryOutlined";
import LocalShippingOutlinedIcon from "@mui/icons-material/LocalShippingOutlined";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import WarehouseOutlinedIcon from "@mui/icons-material/WarehouseOutlined";
import SwapHorizOutlinedIcon from "@mui/icons-material/SwapHorizOutlined";
import ShoppingCartOutlinedIcon from "@mui/icons-material/ShoppingCartOutlined";
import PointOfSaleOutlinedIcon from "@mui/icons-material/PointOfSaleOutlined";
import AssessmentOutlinedIcon from "@mui/icons-material/AssessmentOutlined";
import PeopleAltOutlinedIcon from "@mui/icons-material/PeopleAltOutlined";
import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import MenuIcon from "@mui/icons-material/Menu";
import LogoutIcon from "@mui/icons-material/Logout";
import NotificationsOutlinedIcon from "@mui/icons-material/NotificationsOutlined";
import DarkModeOutlinedIcon from "@mui/icons-material/DarkModeOutlined";
import LightModeOutlinedIcon from "@mui/icons-material/LightModeOutlined";
import TranslateOutlinedIcon from "@mui/icons-material/TranslateOutlined";
import { PropsWithChildren, useMemo, useState } from "react";
import { Link as RouterLink, useLocation, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { api, unwrap } from "../api/client";
import { useAuth } from "../auth/AuthProvider";
import { hasAnyRole } from "../utils/permissions";
import type { DashboardData, RoleName } from "../types/api";
import { Language, useI18n } from "../i18n/I18nProvider";

const expandedWidth = 260;
const collapsedWidth = 74;

type NavItem = { labelKey: Parameters<ReturnType<typeof useI18n>["t"]>[0]; path: string; icon: React.ReactNode; roles?: RoleName[] };

const navItems: NavItem[] = [
  { labelKey: "nav.dashboard", path: "/", icon: <DashboardIcon /> },
  { labelKey: "nav.products", path: "/products", icon: <Inventory2OutlinedIcon /> },
  { labelKey: "nav.categories", path: "/categories", icon: <CategoryOutlinedIcon /> },
  { labelKey: "nav.suppliers", path: "/suppliers", icon: <LocalShippingOutlinedIcon /> },
  { labelKey: "nav.customers", path: "/customers", icon: <GroupsOutlinedIcon /> },
  { labelKey: "nav.warehouses", path: "/warehouses", icon: <WarehouseOutlinedIcon /> },
  { labelKey: "nav.inventory", path: "/inventory", icon: <Inventory2OutlinedIcon /> },
  { labelKey: "nav.movements", path: "/movements", icon: <SwapHorizOutlinedIcon /> },
  { labelKey: "nav.purchaseOrders", path: "/purchase-orders", icon: <ShoppingCartOutlinedIcon /> },
  { labelKey: "nav.salesOrders", path: "/sales-orders", icon: <PointOfSaleOutlinedIcon /> },
  { labelKey: "nav.reports", path: "/reports", icon: <AssessmentOutlinedIcon />, roles: ["ADMIN", "MANAGER"] },
  { labelKey: "nav.users", path: "/users", icon: <PeopleAltOutlinedIcon />, roles: ["ADMIN"] },
  { labelKey: "nav.settings", path: "/settings", icon: <SettingsOutlinedIcon /> }
];

export function AppLayout({
  children,
  mode,
  onToggleMode
}: PropsWithChildren<{ mode: "light" | "dark"; onToggleMode: () => void }>) {
  const location = useLocation();
  const navigate = useNavigate();
  const auth = useAuth();
  const { language, languageLabels, setLanguage, t } = useI18n();
  const isDesktop = useMediaQuery("(min-width:900px)");
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileAnchor, setProfileAnchor] = useState<null | HTMLElement>(null);
  const [notificationAnchor, setNotificationAnchor] = useState<null | HTMLElement>(null);
  const [languageAnchor, setLanguageAnchor] = useState<null | HTMLElement>(null);

  const visibleItems = useMemo(() => navItems.filter((item) => hasAnyRole(auth.roles, item.roles)), [auth.roles]);
  const drawerWidth = collapsed ? collapsedWidth : expandedWidth;

  const dashboardQuery = useQuery({
    queryKey: ["dashboard-notifications"],
    queryFn: () => unwrap<DashboardData>(api.get("/dashboard")),
    enabled: auth.isAuthenticated,
    staleTime: 60_000
  });

  const lowStockCount = dashboardQuery.data?.lowStockProducts ?? 0;

  const drawer = (
    <Box sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
      <Toolbar sx={{ px: 2, minHeight: 64 }}>
        <Stack direction="row" spacing={1.5} alignItems="center">
          <Avatar sx={{ width: 34, height: 34, bgcolor: "primary.main", fontWeight: 800 }}>Y</Avatar>
          {!collapsed && (
            <Box>
              <Typography variant="subtitle1" fontWeight={800}>{t("app.name")}</Typography>
              <Typography variant="caption" color="text.secondary">{t("app.tagline")}</Typography>
            </Box>
          )}
        </Stack>
      </Toolbar>
      <Divider />
      <List sx={{ px: 1, py: 1, flex: 1 }}>
        {visibleItems.map((item) => {
          const active = location.pathname === item.path;
          const label = t(item.labelKey);
          return (
            <Tooltip key={item.path} title={collapsed ? label : ""} placement="right">
              <ListItemButton
                component={RouterLink}
                to={item.path}
                selected={active}
                sx={{ borderRadius: 1, mb: 0.25, minHeight: 44, justifyContent: collapsed ? "center" : "initial" }}
                onClick={() => setMobileOpen(false)}
              >
                <ListItemIcon sx={{ minWidth: collapsed ? 0 : 40, color: active ? "primary.main" : "text.secondary" }}>{item.icon}</ListItemIcon>
                {!collapsed && <ListItemText primary={label} primaryTypographyProps={{ fontSize: 14, fontWeight: active ? 700 : 500 }} />}
              </ListItemButton>
            </Tooltip>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{ borderBottom: 1, borderColor: "divider", ml: { md: `${drawerWidth}px` }, width: { md: `calc(100% - ${drawerWidth}px)` } }}
      >
        <Toolbar>
          <IconButton edge="start" onClick={() => (isDesktop ? setCollapsed((value) => !value) : setMobileOpen(true))} sx={{ mr: 1 }}>
            <MenuIcon />
          </IconButton>
          <Box sx={{ flex: 1 }}>
            <Typography variant="subtitle1" fontWeight={800}>{t("app.console")}</Typography>
            <Typography variant="caption" color="text.secondary">{t("app.consoleSubtitle")}</Typography>
          </Box>
          <Tooltip title={t("common.notifications")}>
            <IconButton onClick={(event) => setNotificationAnchor(event.currentTarget)}>
              <Badge badgeContent={lowStockCount} color="warning">
                <NotificationsOutlinedIcon />
              </Badge>
            </IconButton>
          </Tooltip>
          <Tooltip title={t("common.language")}>
            <IconButton onClick={(event) => setLanguageAnchor(event.currentTarget)}>
              <TranslateOutlinedIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title={mode === "light" ? t("common.darkMode") : t("common.lightMode")}>
            <IconButton onClick={onToggleMode}>{mode === "light" ? <DarkModeOutlinedIcon /> : <LightModeOutlinedIcon />}</IconButton>
          </Tooltip>
          <IconButton onClick={(event) => setProfileAnchor(event.currentTarget)}>
            <Avatar sx={{ width: 34, height: 34 }}>{auth.profile?.fullName?.[0] ?? auth.user?.email[0] ?? "U"}</Avatar>
          </IconButton>
        </Toolbar>
      </AppBar>

      <Drawer
        variant={isDesktop ? "permanent" : "temporary"}
        open={isDesktop ? true : mobileOpen}
        onClose={() => setMobileOpen(false)}
        ModalProps={{ keepMounted: true }}
        sx={{
          width: { md: drawerWidth },
          flexShrink: 0,
          "& .MuiDrawer-paper": { width: isDesktop ? drawerWidth : expandedWidth, borderRight: 1, borderColor: "divider" }
        }}
      >
        {drawer}
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, minWidth: 0, pt: 10, px: { xs: 2, md: 3 }, pb: 4 }}>
        {children}
      </Box>

      <Menu anchorEl={profileAnchor} open={Boolean(profileAnchor)} onClose={() => setProfileAnchor(null)}>
        <MenuItem disabled>{auth.profile?.fullName ?? auth.user?.email}</MenuItem>
        <MenuItem onClick={() => { setProfileAnchor(null); navigate("/settings"); }}>{t("common.profileSettings")}</MenuItem>
        <MenuItem
          onClick={async () => {
            setProfileAnchor(null);
            await auth.logout();
            navigate("/login", { replace: true });
          }}
        >
          <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
          {t("common.logout")}
        </MenuItem>
      </Menu>

      <Menu anchorEl={languageAnchor} open={Boolean(languageAnchor)} onClose={() => setLanguageAnchor(null)}>
        {(Object.keys(languageLabels) as Language[]).map((option) => (
          <MenuItem
            key={option}
            selected={language === option}
            onClick={() => {
              setLanguage(option);
              setLanguageAnchor(null);
            }}
          >
            {languageLabels[option]}
          </MenuItem>
        ))}
      </Menu>

      <Menu anchorEl={notificationAnchor} open={Boolean(notificationAnchor)} onClose={() => setNotificationAnchor(null)}>
        <MenuItem disabled>{t("common.notifications")}</MenuItem>
        <MenuItem onClick={() => { setNotificationAnchor(null); navigate("/products"); }}>
          {lowStockCount > 0 ? t("common.lowStockAlert", { count: lowStockCount }) : t("common.noLowStock")}
        </MenuItem>
      </Menu>
    </Box>
  );
}
