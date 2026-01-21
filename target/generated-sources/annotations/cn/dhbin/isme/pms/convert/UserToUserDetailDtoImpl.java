package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.dto.UserDetailDto;
import cn.dhbin.isme.pms.domain.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:16+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class UserToUserDetailDtoImpl implements UserToUserDetailDto {

    @Override
    public UserDetailDto to(User arg0) {
        if ( arg0 == null ) {
            return null;
        }

        UserDetailDto userDetailDto = new UserDetailDto();

        userDetailDto.setId( arg0.getId() );
        userDetailDto.setUsername( arg0.getUsername() );
        userDetailDto.setEnable( arg0.getEnable() );
        userDetailDto.setCreateTime( arg0.getCreateTime() );
        userDetailDto.setUpdateTime( arg0.getUpdateTime() );

        return userDetailDto;
    }
}
