import { reactive, computed } from 'vue'

export interface UserPermissions {
  menus: string[]
  actions: string[]
}

export interface UserInfo {
  id: number
  username: string
  email?: string
  role: string
  permissions: UserPermissions
  avatar?: string
}

interface PermissionState {
  user: UserInfo | null
  isLoaded: boolean
}

// 权限状态
const state = reactive<PermissionState>({
  user: null,
  isLoaded: false
})

/**
 * 权限管理 Store
 */
export const usePermissionStore = () => {
  // 初始化：从localStorage加载用户信息
  const init = () => {
    const userInfo = localStorage.getItem('user_info')
    if (userInfo) {
      try {
        state.user = JSON.parse(userInfo)
        state.isLoaded = true
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  }

  // 设置用户信息
  const setUser = (user: UserInfo) => {
    state.user = user
    state.isLoaded = true
    localStorage.setItem('user_info', JSON.stringify(user))
  }

  // 清除用户信息
  const clearUser = () => {
    state.user = null
    state.isLoaded = false
    localStorage.removeItem('user_info')
    localStorage.removeItem('auth_token')
  }

  // 获取用户信息
  const getUser = computed(() => state.user)

  // 获取角色
  const getRole = computed(() => state.user?.role || '')

  // 获取权限列表
  const getPermissions = computed(() => state.user?.permissions || { menus: [], actions: [] })

  // 检查是否有菜单权限
  const hasMenu = (menu: string): boolean => {
    if (!state.user) return false
    const permissions = state.user.permissions
    if (!permissions) return false
    
    // 超级管理员或actions包含*则拥有所有权限
    if (permissions.actions?.includes('*')) return true
    
    return permissions.menus?.includes(menu) || false
  }

  // 检查是否有操作权限
  const hasAction = (action: string): boolean => {
    if (!state.user) return false
    const permissions = state.user.permissions
    if (!permissions) return false
    
    // 超级管理员拥有所有权限
    if (permissions.actions?.includes('*')) return true
    
    return permissions.actions?.includes(action) || false
  }

  // 检查是否是指定角色
  const hasRole = (role: string): boolean => {
    return state.user?.role === role
  }

  // 检查是否有任意一个角色
  const hasAnyRole = (...roles: string[]): boolean => {
    if (!state.user) return false
    return roles.includes(state.user.role)
  }

  // 是否已登录
  const isAuthenticated = computed(() => !!state.user && !!localStorage.getItem('auth_token'))

  return {
    // 状态
    state,
    getUser,
    getRole,
    getPermissions,
    isAuthenticated,
    
    // 方法
    init,
    setUser,
    clearUser,
    hasMenu,
    hasAction,
    hasRole,
    hasAnyRole
  }
}

// 创建单例
let _store: ReturnType<typeof usePermissionStore> | null = null

export const getPermissionStore = () => {
  if (!_store) {
    _store = usePermissionStore()
    _store.init()
  }
  return _store
}

export default getPermissionStore
