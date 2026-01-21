package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.entity.Profile;
import cn.dhbin.isme.pms.domain.request.RegisterUserProfileRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:17+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class RegisterUserProfileRequestToProfileImpl implements RegisterUserProfileRequestToProfile {

    @Override
    public Profile to(RegisterUserProfileRequest arg0) {
        if ( arg0 == null ) {
            return null;
        }

        Profile profile = new Profile();

        profile.setGender( arg0.getGender() );
        profile.setAvatar( arg0.getAvatar() );
        profile.setAddress( arg0.getAddress() );
        profile.setEmail( arg0.getEmail() );
        profile.setNickName( arg0.getNickName() );

        return profile;
    }
}
