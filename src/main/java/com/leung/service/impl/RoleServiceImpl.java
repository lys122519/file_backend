package com.leung.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Menu;
import com.leung.entity.Role;
import com.leung.entity.RoleMenu;
import com.leung.entity.User;
import com.leung.mapper.MenuMapper;
import com.leung.mapper.RoleMapper;
import com.leung.mapper.RoleMenuMapper;
import com.leung.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author leung
 * @since 2022-04-01
 */
@Service

public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private MenuMapper menuMapper;


    @Override
    public IPage<Role> getPage(int currentPage, int pageSize) {
        IPage<Role> page = new Page<>(currentPage, pageSize);
        roleMapper.selectPage(page, null);
        return page;
    }

    @Override
    public IPage<Role> getPage(int currentPage, int pageSize, Role role) {
        LambdaQueryWrapper<Role> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.like(Strings.isNotEmpty(role.getName()), Role::getName, role.getName());

        lambdaQueryWrapper.orderByDesc(Role::getId);
        IPage<Role> page = new Page<>(currentPage, pageSize);
        roleMapper.selectPage(page, lambdaQueryWrapper);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean setRoleMenu(Integer roleId, List<Integer> menuIds) {
        QueryWrapper<RoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        //先删除当前角色id所有的绑定关系
        roleMenuMapper.delete(queryWrapper);


        //再添加新的信息
        RoleMenu roleMenu;
        List<Integer> menuIdsCopy = CollUtil.newArrayList(menuIds);
        for (Integer menuId : menuIds) {
            Menu menu = menuMapper.selectById(menuId);
            //二级菜单 并且 传过来的menuid数组里没有它的父级id
            if (menu.getPid() != null && !menuIdsCopy.contains(menu.getPid())) {
                //补充父级id
                roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menu.getPid());
                roleMenuMapper.insert(roleMenu);
                menuIdsCopy.add(menu.getPid());
            }
            roleMenu = new RoleMenu();
            roleMenu.setMenuId(menuId);
            roleMenu.setRoleId(roleId);
            roleMenuMapper.insert(roleMenu);
        }
        return true;
    }

    @Override
    public List<Integer> getRoleMenu(Integer roleId) {
        QueryWrapper<RoleMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(queryWrapper);
        List<Integer> roleMenuList = new ArrayList<>();
        for (RoleMenu roleMenu : roleMenus) {
            roleMenuList.add(roleMenu.getMenuId());
        }
        return roleMenuList;
    }

}
