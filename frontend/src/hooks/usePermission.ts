import { getPermissionStore } from '@/stores/permission'

/**
 * 权限检查 Hook
 * 用于在组件中检查权限
 */
export const usePermission = () => {
  const store = getPermissionStore()

  return {
    // 检查菜单权限
    hasMenu: store.hasMenu,
    
    // 检查操作权限
    hasAction: store.hasAction,
    
    // 检查角色
    hasRole: store.hasRole,
    hasAnyRole: store.hasAnyRole,
    
    // 获取用户信息
    user: store.getUser,
    role: store.getRole,
    permissions: store.getPermissions,
    isAuthenticated: store.isAuthenticated
  }
}

export default usePermission
