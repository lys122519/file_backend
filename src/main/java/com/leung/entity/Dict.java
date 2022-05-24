package com.leung.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;

/**
 * @Description:
 * @author: leung
 * @date: 2022-04-08 14:41
 */
@TableName("sys_dict")
@ApiModel(value = "Dict对象", description = "")
public class Dict {
    private String name;
    private String value;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
