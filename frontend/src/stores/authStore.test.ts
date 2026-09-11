describe('authStore', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.resetModules()
  })

  async function loadStore() {
    const mod = await import('./authStore')
    return mod.useAuthStore
  }

  it('starts unauthenticated when localStorage is empty', async () => {
    const useAuthStore = await loadStore()
    const state = useAuthStore.getState()
    expect(state.token).toBeNull()
    expect(state.user).toBeNull()
    expect(state.isAuthenticated()).toBe(false)
  })

  it('setToken parses JWT and persists to localStorage', async () => {
    const useAuthStore = await loadStore()
    const payload = { sub: 'user@test.com', username: 'testuser', userId: 'u-1' }
    const jwt = `header.${btoa(JSON.stringify(payload))}.signature`

    useAuthStore.getState().setToken(jwt)

    const state = useAuthStore.getState()
    expect(state.token).toBe(jwt)
    expect(state.user).toEqual({ email: 'user@test.com', username: 'testuser', userId: 'u-1' })
    expect(state.isAuthenticated()).toBe(true)
    expect(localStorage.getItem('jwt')).toBe(jwt)
  })

  it('logout clears token, user, and localStorage', async () => {
    const useAuthStore = await loadStore()
    const payload = { sub: 'user@test.com', username: 'testuser', userId: 'u-1' }
    const jwt = `header.${btoa(JSON.stringify(payload))}.signature`

    useAuthStore.getState().setToken(jwt)
    useAuthStore.getState().logout()

    const state = useAuthStore.getState()
    expect(state.token).toBeNull()
    expect(state.user).toBeNull()
    expect(state.isAuthenticated()).toBe(false)
    expect(localStorage.getItem('jwt')).toBeNull()
  })

  it('restores session from localStorage on load', async () => {
    const payload = { sub: 'saved@test.com', username: 'saved', userId: 'u-2' }
    const jwt = `h.${btoa(JSON.stringify(payload))}.s`
    localStorage.setItem('jwt', jwt)

    const useAuthStore = await loadStore()
    const state = useAuthStore.getState()
    expect(state.token).toBe(jwt)
    expect(state.user).toEqual({ email: 'saved@test.com', username: 'saved', userId: 'u-2' })
  })

  it('malformed JWT results in user: null', async () => {
    const useAuthStore = await loadStore()
    useAuthStore.getState().setToken('not.a.valid-jwt')

    expect(useAuthStore.getState().user).toBeNull()
  })
})
