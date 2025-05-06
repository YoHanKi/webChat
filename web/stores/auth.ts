// stores/auth.ts
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: typeof window !== 'undefined' ? sessionStorage.getItem('username') : null as string | null,
        role: typeof window !== 'undefined' ? sessionStorage.getItem('role') : null as string | null
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
            // Map<String,Object> 에 "user"와 "role"이 들어있다고 가정
            const data = await res.json()
            this.user = data.user as string
            this.role = data.role as string
            sessionStorage.setItem('username', data.user)
            sessionStorage.setItem('role', data.role)
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
        validate(user) {
            if (user !== null) {
                // null이 아니라면 검증
                fetch('/api/user/auth', {
                    method: 'GET',
                    headers: { 'Content-Type': 'application/json' }
                })
                    .then(res => {
                        if (!res.ok) {
                            this.logout()
                            throw new Error('세션이 만료되었습니다.')
                        }
                    })
                    .catch(err => {
                        console.error('Session validation failed:', err)
                        this.logout()
                })
            }
        },
        logout() {
            this.user = null
            this.role = null
            sessionStorage.removeItem('username')
            sessionStorage.removeItem('role')

            fetch('/api/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            }).catch(err => {
                console.error('Logout failed:', err)
            })
        }
    }
})