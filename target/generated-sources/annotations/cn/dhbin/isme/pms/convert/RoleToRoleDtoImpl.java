package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.dto.RoleDto;
import cn.dhbin.isme.pms.domain.entity.Role;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:16+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class RoleToRoleDtoImpl implements RoleToRoleDto {

    @Override
    public RoleDto to(Role arg0) {
        if ( arg0 == null ) {
            return null;
        }

        RoleDto roleDto = new RoleDto();

        roleDto.setId( arg0.getId() );
        roleDto.setCode( arg0.getCode() );
        roleDto.setName( arg0.getName() );
        roleDto.setEnable( arg0.getEnable() );

        return roleDto;
    }
}
