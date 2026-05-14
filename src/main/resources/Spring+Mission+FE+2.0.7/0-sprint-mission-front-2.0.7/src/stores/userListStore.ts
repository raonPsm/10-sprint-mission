import {create} from 'zustand';
import {getUsers} from '../api/user.ts';
import {UserDto} from '../types/api.ts';


interface UserListStore {
  users: UserDto[];
  fetchUsers: () => Promise<void>;
}

const useUserListStore = create<UserListStore>((set) => ({
  users: [],
  fetchUsers: async () => {
    try {
      const users = await getUsers();
      set({users});
    } catch (error) {
      console.error('사용자 목록 조회 실패:', error);
    }
  }
}));

export default useUserListStore; 