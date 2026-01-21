package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.dto.UserPageDto;
import cn.dhbin.isme.pms.domain.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:16+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class UserToUserPageDtoImpl implements UserToUserPageDto {

    @Override
    public UserPageDto to(User arg0) {
        if ( arg0 == null ) {
            return null;
        }

        UserPageDto userPageDto = new UserPageDto();

        userPageDto.setId( arg0.getId() );
        userPageDto.setUsername( arg0.getUsername() );
        userPageDto.setEnable( arg0.getEnable() );
        userPageDto.setCreateTime( arg0.getCreateTime() );
        userPageDto.setUpdateTime( arg0.getUpdateTime() );

        return userPageDto;
    }
}
