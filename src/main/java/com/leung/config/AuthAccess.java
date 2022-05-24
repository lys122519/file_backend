package com.leung.config;

import java.lang.annotation.*;

/**
 * @Description:
 * @author: leung
 * @date: 2022-05-09 20:08
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthAccess {

}
