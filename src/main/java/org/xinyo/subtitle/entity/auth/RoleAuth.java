package org.xinyo.subtitle.entity.auth;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author CHENG
 * @since 2020-01-03
 */
@Data
@TableName("auth_role_auth")
public class RoleAuth implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleId;

    private String apiId;


}
