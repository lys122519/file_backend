package com.leung.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.common.Constants;
import com.leung.common.ValidationEnum;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 发送人
    @Value("${spring.mail.username}")
    private String from;

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
     *
     * @param page
     * @param user
     * @return
     */
    @Override
    public Page<User> findPage(Page<User> page, User user) {
        return userMapper.findPage(page, user.getUsername(), user.getEmail(), user.getAddress());
    }

    /**
     * 修改密码
     *
     * @param userDTO
     */
    @Override
    public void updatePassword(UserDTO userDTO) {

        User user = new User();

        String newPassword = userDTO.getNewPassword();
        String oldPassword = userDTO.getPassword();
        if (newPassword.equals(oldPassword)) {
            throw new ServiceException(Constants.CODE_600, "两次密码一致");
        } else {
            //密码不一致
            user.setId(userDTO.getId());
            user.setPassword(newPassword);
        }
        user.setPassword(userDTO.getNewPassword());
        int result = userMapper.updateById(user);
        if (result < 1) {
            throw new ServiceException(Constants.CODE_600, "修改密码失败");
        }
    }

    /**
     * 邮箱登录
     * 邮箱 + 验证码
     *
     * @param userDTO
     * @return
     */
    @Override
    public UserDTO loginEmail(UserDTO userDTO) {
        String email = userDTO.getEmail();
        String code = userDTO.getCode();

        List<User> userList = checkUser(email);

        //查询到该用户
        //查询redis 是否有 验证码
        String codeFromRedis = stringRedisTemplate.opsForValue().get(Constants.MAIL_CODE + email);

        if (codeFromRedis != null) {
            if (codeFromRedis.equals(code)) {
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
                throw new ServiceException(Constants.CODE_600, "验证码错误");
            }
        } else {
            //redis没有对应的验证码
            throw new ServiceException(Constants.CODE_600, "验证码已过期");
        }


        //return null;
    }


    /**
     * 发送邮箱验证码
     *
     * @param email
     */
    @Override
    public void sendEmailCode(String email, Integer type) {
        checkUser(email);


        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(from);
        mailMessage.setTo(email);
        //随机四位验证码
        String code = RandomUtil.randomNumbers(4);


        if (ValidationEnum.LOGIN.getCode().equals(type)) {
            mailMessage.setSubject("[xx系统]登录邮箱验证");
            mailMessage.setText("您本次登录的验证码为：" + code + "，有效期3分钟，请妥善保管，避免泄露。");


        } else if (ValidationEnum.FORGET.getCode().equals(type)) {
            mailMessage.setSubject("[xx系统]找回密码邮箱验证");

            mailMessage.setText("您本次找回密码的验证码为：" + code + "，有效期3分钟，请妥善保管，避免泄露。");
        }


        String codeFromRedis = stringRedisTemplate.opsForValue().get(Constants.MAIL_CODE + email);
        if ("".equals(codeFromRedis) || codeFromRedis == null) {
            //redis没有对应的验证码

            javaMailSender.send(mailMessage);
            //将code放入redis 180s过期
            stringRedisTemplate.opsForValue().set(Constants.MAIL_CODE + email, code, 180, TimeUnit.SECONDS);
        } else {
            throw new ServiceException(Constants.CODE_600, "当前您的验证码仍然有效请勿重复发送");
        }

    }

    /**
     * 重置密码
     *
     * @param userDTO
     */
    @Override
    public void resetPass(UserDTO userDTO) {
        checkUser(userDTO.getEmail());
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();

        queryWrapper.eq("email", userDTO.getEmail());
        User user = new User();

        user.setPassword(userDTO.getNewPassword());


        String email = userDTO.getEmail();
        String code = userDTO.getCode();

        //查询到该用户
        //查询redis 是否有 验证码
        String codeFromRedis = stringRedisTemplate.opsForValue().get(Constants.MAIL_CODE + email);

        if (codeFromRedis != null) {
            if (codeFromRedis.equals(code)) {

                user.setPassword(userDTO.getNewPassword());
                int result = userMapper.update(user, queryWrapper);
                if (result < 1) {
                    throw new ServiceException(Constants.CODE_600, "修改密码失败");
                }

            } else {
                throw new ServiceException(Constants.CODE_600, "验证码错误");
            }

        } else {
            //redis没有对应的验证码
            throw new ServiceException(Constants.CODE_600, "验证码已过期");
        }
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


    private List<User> checkUser(String email) {
        //验证通过 查询 则返回userDto
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //根据邮箱查找
        queryWrapper.eq("email", email);
        //queryWrapper.eq("password", userDTO.getPassword());
        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList.size() != 1) {
            throw new ServiceException(Constants.CODE_600, "没有该用户，请检查邮箱是否输入正确");
        }
        return userList;
    }
}
