package com.leung.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author leung
 * @since 2022-03-26
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {


    Page<User> findPage(Page<User> page, @Param("name") String name, @Param("email") String email, @Param("address") String address);

}
