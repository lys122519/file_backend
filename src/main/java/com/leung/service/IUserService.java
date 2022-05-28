package com.leung.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leung.entity.dto.UserDTO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author leung
 * @since 2022-03-26
 */
public interface IUserService extends IService<User> {
    IPage<User> getPage(int currentPage, int pageSize);

    IPage<User> getPage(int currentPage, int pageSize, User user);

    //用户名登录
    UserDTO login(UserDTO userDTO);

    User register(User user);


    Page<User> findPage(Page<User> page, User user);

    //修改密码
    void updatePassword(UserDTO userDTO);

    //邮箱登录
    UserDTO loginEmail(UserDTO userDTO);

    //发送验证码
    void sendEmailCode(String email, Integer type);

    //忘记密码
    void resetPass(UserDTO userDTO);
}
