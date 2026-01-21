package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.dto.PermissionDto;
import cn.dhbin.isme.pms.domain.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:16+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class PermissionToPermissionDtoImpl implements PermissionToPermissionDto {

    @Override
    public PermissionDto to(Permission arg0) {
        if ( arg0 == null ) {
            return null;
        }

        PermissionDto permissionDto = new PermissionDto();

        permissionDto.setId( arg0.getId() );
        permissionDto.setName( arg0.getName() );
        permissionDto.setCode( arg0.getCode() );
        permissionDto.setType( arg0.getType() );
        permissionDto.setParentId( arg0.getParentId() );
        permissionDto.setPath( arg0.getPath() );
        permissionDto.setRedirect( arg0.getRedirect() );
        permissionDto.setIcon( arg0.getIcon() );
        permissionDto.setComponent( arg0.getComponent() );
        permissionDto.setLayout( arg0.getLayout() );
        permissionDto.setKeepAlive( arg0.getKeepAlive() );
        permissionDto.setMethod( arg0.getMethod() );
        permissionDto.setDescription( arg0.getDescription() );
        permissionDto.setShow( arg0.getShow() );
        permissionDto.setEnable( arg0.getEnable() );
        permissionDto.setOrder( arg0.getOrder() );

        return permissionDto;
    }
}
