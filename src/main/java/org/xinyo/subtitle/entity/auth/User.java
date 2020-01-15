package org.xinyo.subtitle.entity.auth;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author CHENG
 * @since 2020-01-03
 */
@Data
@TableName("auth_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String password;

    private String email;

    private String roleId;

    /**
     * 0-正常
     */
    private Integer status;

    @TableField(exist = false)
    private String token;


}
