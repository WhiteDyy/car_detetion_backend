package cn.dhbin.isme.pms.convert;

import cn.dhbin.isme.pms.domain.entity.Permission;
import cn.dhbin.isme.pms.domain.request.CreatePermissionRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T15:37:17+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class CreatePermissionRequestToPermissionImpl implements CreatePermissionRequestToPermission {

    @Override
    public Permission to(CreatePermissionRequest arg0) {
        if ( arg0 == null ) {
            return null;
        }

        Permission permission = new Permission();

        permission.setName( arg0.getName() );
        permission.setCode( arg0.getCode() );
        permission.setType( arg0.getType() );
        permission.setParentId( arg0.getParentId() );
        permission.setPath( arg0.getPath() );
        permission.setRedirect( arg0.getRedirect() );
        permission.setIcon( arg0.getIcon() );
        permission.setComponent( arg0.getComponent() );
        permission.setLayout( arg0.getLayout() );
        permission.setMethod( arg0.getMethod() );
        permission.setDescription( arg0.getDescription() );
        permission.setShow( arg0.getShow() );
        permission.setEnable( arg0.getEnable() );
        permission.setOrder( arg0.getOrder() );

        return permission;
    }
}
