package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.entity.Role;
import cn.dhbin.isme.pms.domain.request.CreateRoleRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:16+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class CreateRoleRequestToRoleImpl implements CreateRoleRequestToRole {

    @Override
    public Role to(CreateRoleRequest arg0) {
        if ( arg0 == null ) {
            return null;
        }

        Role role = new Role();

        role.setCode( arg0.getCode() );
        role.setName( arg0.getName() );
        role.setEnable( arg0.getEnable() );

        return role;
    }
}
