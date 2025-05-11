import { reactive } from 'vue'

export const useAdminColumns = () => {
    const columns = reactive({
        notice: [
            {key: 'id', label: 'ID'},
            {key: 'title', label: '제목'},
            {key: 'createdAt', label: '등록일'},
            {key: 'actions', label: '관리'}
        ],

        user: [
            {key: 'id', label: 'ID'},
            {key: 'username', label: '아이디'},
            {key: 'role', label: '권한'},
            {key: 'createdAt', label: '가입일'},
            {key: 'actions', label: '관리'}
        ],

        room: [
            {key: 'id', label: 'ID'},
            {key: 'name', label: '방 이름'},
            {key: 'creator', label: '방장'},
            {key: 'createdAt', label: '생성일'},
            {key: 'actions', label: '관리'}
        ]
    })

    return {
        columns
    }
}
