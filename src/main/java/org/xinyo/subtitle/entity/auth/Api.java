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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("auth_api")
public class Api implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String requestPath;


}
