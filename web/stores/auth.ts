// stores/auth.ts
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: typeof window !== 'undefined' ? sessionStorage.getItem('username') : null as string | null
    }),
    actions: {
        init() {
            if (typeof window !== 'undefined') {
                this.user = sessionStorage.getItem('username')
            }
        },
        async login(username: string, password: string) {
            const res = await fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            })
            if (!res.ok) throw new Error('로그인에 실패했습니다.')
            this.user = username
            sessionStorage.setItem('username', username)
        },
        async register(username: string, password: string) {
            const res = await fetch('/api/user/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            })
            if (!res.ok) {
                const data = await res.json()
                throw new Error(data.error || '회원가입에 실패했습니다.')
            }
        },
        logout() {
            this.user = null
            sessionStorage.removeItem('username')
        }
    }
})