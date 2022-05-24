package com.leung.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leung.entity.RoleMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 用户菜单表操纵mapper
 * @author: leung
 * @date: 2022-04-08 15:26
 */
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    @Select("select menu_id from sys_role_menu where role_id = #{roleId}")
    List<Integer> selectByRoleId(@Param("roleId") Integer roleId);
}
