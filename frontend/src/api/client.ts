import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import { tokenStore } from "../auth/tokenStore";
import type { ApiResponse, TokenResponse } from "../types/api";

export const api = axios.create({
  baseURL: "/api/v1",
  headers: {
    "Content-Type": "application/json"
  }
});

let refreshPromise: Promise<string | null> | null = null;
let onUnauthorized: (() => void) | null = null;

export const setUnauthorizedHandler = (handler: () => void) => {
  onUnauthorized = handler;
};

api.interceptors.request.use(async (config) => {
  const isAuthEndpoint = config.url?.includes("/auth/login") || config.url?.includes("/auth/refresh");
  const token = tokenStore.getAccessToken() ?? (!isAuthEndpoint ? await refreshAccessToken() : null);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const canRefresh = error.response?.status === 401 || error.response?.status === 403;
    if (!original || !canRefresh || original._retry || original.url?.includes("/auth/refresh")) {
      return Promise.reject(error);
    }

    original._retry = true;
    refreshPromise ??= refreshAccessToken();
    const newAccessToken = await refreshPromise.finally(() => {
      refreshPromise = null;
    });

    if (!newAccessToken) {
      tokenStore.clear();
      onUnauthorized?.();
      return Promise.reject(error);
    }

    original.headers.Authorization = `Bearer ${newAccessToken}`;
    return api(original);
  }
);

async function refreshAccessToken() {
  const refreshToken = tokenStore.getRefreshToken();
  if (!refreshToken) {
    return null;
  }

  try {
    const response = await axios.post<ApiResponse<TokenResponse>>("/api/v1/auth/refresh", { refreshToken });
    tokenStore.setSession(response.data.data);
    return response.data.data.accessToken;
  } catch {
    return null;
  }
}

export function apiMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    const payload = error.response?.data as { message?: string } | undefined;
    return payload?.message ?? error.message;
  }
  return error instanceof Error ? error.message : "Unexpected error";
}

export async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) {
  const response = await request;
  return response.data.data;
}
