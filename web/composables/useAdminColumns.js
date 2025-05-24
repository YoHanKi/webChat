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
            {key: 'roomName', label: '방 이름'},
            {key: 'creator', label: '방장'},
            {key: 'createdAt', label: '생성일'},
            {key: 'actions', label: '관리'}
        ],

        chat: [
            { key: 'id', label: 'ID', width: '80px' },
            { key: 'roomName', label: '채팅방', width: '150px' },
            { key: 'sender', label: '발신자', width: '120px' },
            { key: 'content', label: '내용', width: 'auto' },
            { key: 'createdAt', label: '시간', width: '180px' },
            { key: 'actions', label: '관리', width: '120px' }
        ]
    })

    return {
        columns
    }
}
