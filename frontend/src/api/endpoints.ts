import { api, unwrap } from "./client";
import type { PageResponse } from "../types/api";

export interface PageQuery {
  page?: number;
  size?: number;
  q?: string;
  sort?: string;
  [key: string]: unknown;
}

export function pageParams(query: PageQuery) {
  return {
    page: query.page ?? 0,
    size: query.size ?? 10,
    ...(query.q ? { q: query.q } : {}),
    ...(query.sort ? { sort: query.sort } : {})
  };
}

export const listPage = <T>(path: string, query: PageQuery = {}) =>
  unwrap<PageResponse<T>>(api.get(path, { params: pageParams(query) }));

export const getOne = <T>(path: string, id: number) => unwrap<T>(api.get(`${path}/${id}`));
export const createOne = <T, P>(path: string, payload: P) => unwrap<T>(api.post(path, payload));
export const updateOne = <T, P>(path: string, id: number, payload: P) => unwrap<T>(api.put(`${path}/${id}`, payload));
export const deleteOne = (path: string, id: number) => unwrap<void>(api.delete(`${path}/${id}`));
