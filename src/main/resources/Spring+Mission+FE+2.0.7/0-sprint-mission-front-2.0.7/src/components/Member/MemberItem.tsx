import defaultProfile from '@/assets/default_profile.png';
import useBinaryContentStore from '@/stores/binaryContentStore.ts';
import {UserDto} from '@/types/api.ts';
import {useEffect} from 'react';
import {Avatar, StatusDot} from '../../styles/common.ts';
import {MemberAvatarContainer, MemberInfo, StyledMemberItem} from './styles.ts';

interface MemberItemProps {
  member: UserDto;
}

function MemberItem({member}: MemberItemProps): JSX.Element {
  const {binaryContents, fetchBinaryContent} = useBinaryContentStore();

  useEffect(() => {
    if (member.profile?.id && !binaryContents[member.profile.id]) {
      fetchBinaryContent(member.profile.id);
    }
  }, [member.profile?.id, binaryContents, fetchBinaryContent]);

  return (
      <StyledMemberItem>
        <MemberAvatarContainer>
          <Avatar
              src={member.profile?.id ? binaryContents[member.profile.id]?.url || defaultProfile : defaultProfile}
              alt={member.username}
          />
          <StatusDot $online={member.online}/>
        </MemberAvatarContainer>
        <MemberInfo>
          {member.username}
        </MemberInfo>
      </StyledMemberItem>
  );
}

export default MemberItem; 