import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, PageQuery } from '../types/user'
import type { PageResult } from '../types/api'
import { userApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const userList = ref<User[]>([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)

  const fetchUserList = async (params?: PageQuery): Promise<void> => {
    loading.value = true
    try {
      const queryParams = {
        page: params?.page || currentPage.value,
        pageSize: params?.pageSize || pageSize.value
      }
      const res = await userApi.listUsers(queryParams)
      const data = res.data as PageResult<User>
      userList.value = data.list
      total.value = data.total
      currentPage.value = data.page
      pageSize.value = data.pageSize
    } finally {
      loading.value = false
    }
  }

  const fetchUserById = async (id: number): Promise<User | null> => {
    try {
      const res = await userApi.getUserById(id)
      return res.data as User
    } catch {
      return null
    }
  }

  const setPage = (page: number): void => {
    currentPage.value = page
    fetchUserList()
  }

  const setPageSize = (size: number): void => {
    pageSize.value = size
    currentPage.value = 1
    fetchUserList()
  }

  return {
    userList,
    total,
    currentPage,
    pageSize,
    loading,
    fetchUserList,
    fetchUserById,
    setPage,
    setPageSize
  }
})
