package com.leung.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.common.Constants;
import com.leung.entity.Menu;
import com.leung.entity.RoleMenu;
import com.leung.entity.User;
import com.leung.entity.dto.UserDTO;
import com.leung.exception.ServiceException;
import com.leung.mapper.MenuMapper;
import com.leung.mapper.RoleMapper;
import com.leung.mapper.RoleMenuMapper;
import com.leung.mapper.UserMapper;
import com.leung.service.IMenuService;
import com.leung.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leung.utils.TokenUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author leung
 * @since 2022-03-26
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private IMenuService menuService;

    @Override
    public IPage<User> getPage(int currentPage, int pageSize) {
        IPage<User> page = new Page<>(currentPage, pageSize);
        userMapper.selectPage(page, null);
        return page;
    }

    @Override
    public IPage<User> getPage(int currentPage, int pageSize, User user) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.like(Strings.isNotEmpty(user.getUsername()), User::getUsername, user.getUsername());
        lambdaQueryWrapper.like(Strings.isNotEmpty(user.getEmail()), User::getEmail, user.getEmail());
        lambdaQueryWrapper.like(Strings.isNotEmpty(user.getAddress()), User::getAddress, user.getAddress());
        lambdaQueryWrapper.orderByDesc(User::getId);
        IPage<User> page = new Page<>(currentPage, pageSize);
        userMapper.selectPage(page, lambdaQueryWrapper);
        return page;
    }

    @Override
    public UserDTO login(UserDTO userDTO) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());
        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList.size() == 1) {
            BeanUtil.copyProperties(userList.get(0), userDTO, true);
            //设置token
            String token = TokenUtils.getToken(userList.get(0).getId().toString(), userList.get(0).getPassword());
            userDTO.setToken(token);

            String role = userList.get(0).getRole();
            //设置用户菜单列表
            List<Menu> roleMenus = getRoleMenu(role);
            userDTO.setMenus(roleMenus);

            return userDTO;
        } else {

            throw new ServiceException(Constants.CODE_600, "用户名或密码错误");
        }

    }

    @Override
    public User register(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList.size() == 0) {
            user.setNickname(user.getUsername());
            userMapper.insert(user);
            return user;
        } else {
            throw new ServiceException(Constants.CODE_600, "用户名已存在");
        }
    }

    /**
     * 查找
     * @param page
     * @param user
     * @return
     */
    @Override
    public Page<User> findPage(Page<User> page, User user) {
        return userMapper.findPage(page, user.getUsername(), user.getEmail(), user.getAddress());
    }


    /**
     * 获取当前角色的菜单列表
     *
     * @param roleFlag
     * @return
     */
    private List<Menu> getRoleMenu(String roleFlag) {
        Integer roleId = roleMapper.selectByFlag(roleFlag);
        //当前角色的所有菜单id集合
        List<Integer> menuIds = roleMenuMapper.selectByRoleId(roleId);
        //查出所有菜单
        List<Menu> menus = menuService.findMenus("");

        //new一个删选完成之后的list
        List<Menu> roleMenus = new ArrayList<>();

        //筛选当前用户菜单
        for (Menu menu : menus) {
            if (menuIds.contains(menu.getId())) {
                roleMenus.add(menu);
            }

            List<Menu> children = menu.getChildren();
            //移除children中不在menuIds集合中的元素
            children.removeIf(child -> !menuIds.contains(child.getId()));
        }
        return roleMenus;
    }
}
