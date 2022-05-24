package com.leung.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leung.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leung.entity.User;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author leung
 * @since 2022-04-01
 */
public interface IRoleService extends IService<Role> {
    public IPage<Role> getPage(int currentPage, int pageSize);
    public IPage<Role> getPage(int currentPage, int pageSize, Role role);

    boolean setRoleMenu(Integer roleId, List<Integer> menuIds);

    List<Integer> getRoleMenu(Integer roleId);
}
