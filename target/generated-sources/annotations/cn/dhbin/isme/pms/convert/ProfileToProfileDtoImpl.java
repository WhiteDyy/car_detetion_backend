package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.dto.ProfileDto;
import cn.dhbin.isme.pms.domain.entity.Profile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:17+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class ProfileToProfileDtoImpl implements ProfileToProfileDto {

    @Override
    public ProfileDto to(Profile arg0) {
        if ( arg0 == null ) {
            return null;
        }

        ProfileDto profileDto = new ProfileDto();

        profileDto.setId( arg0.getId() );
        profileDto.setGender( arg0.getGender() );
        profileDto.setAvatar( arg0.getAvatar() );
        profileDto.setAddress( arg0.getAddress() );
        profileDto.setEmail( arg0.getEmail() );
        profileDto.setUserId( arg0.getUserId() );
        profileDto.setNickName( arg0.getNickName() );

        return profileDto;
    }
}
