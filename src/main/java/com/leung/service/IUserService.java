package com.leung.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leung.entity.dto.UserDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author leung
 * @since 2022-03-26
 */
public interface IUserService extends IService<User> {
    public IPage<User> getPage(int currentPage, int pageSize);
    public IPage<User> getPage(int currentPage, int pageSize, User user);
    UserDTO login(UserDTO userDTO);
    User register(User user);

    Page<User> findPage(Page<User> page, User user);
}
