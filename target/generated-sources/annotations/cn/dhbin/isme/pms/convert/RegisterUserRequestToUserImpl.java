package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.entity.User;
import cn.dhbin.isme.pms.domain.request.RegisterUserRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:17+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class RegisterUserRequestToUserImpl implements RegisterUserRequestToUser {

    @Override
    public User to(RegisterUserRequest arg0) {
        if ( arg0 == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( arg0.getUsername() );
        user.setPassword( arg0.getPassword() );
        user.setEnable( arg0.getEnable() );

        return user;
    }
}
