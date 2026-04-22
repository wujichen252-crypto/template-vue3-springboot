export interface ApiResult<T = unknown> {
  code: number
  data: T
  msg: string
  requestId: string
}

export interface PageResult<T = unknown> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface PageQuery {
  page?: number
  pageSize?: number
}
