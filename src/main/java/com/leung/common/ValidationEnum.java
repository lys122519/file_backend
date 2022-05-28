package com.leung.common;

/**
 * @Description:
 * @author: leung
 * @date: 2022-05-28 15:37
 */
public enum ValidationEnum {
    /**
     * 登录
     */
    LOGIN(1),
    /**
     * 找回密码
     */
    FORGET(2);

    private Integer code;

    ValidationEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
