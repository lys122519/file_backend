package com.leung.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.common.Constants;
import com.leung.common.Result;
import com.leung.entity.dto.UserDTO;
import com.leung.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import com.leung.service.IUserService;
import com.leung.entity.User;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author leung
 * @since 2022-03-26
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    /**
     * 新增或修改
     *
     * @param user
     * @return
     */
    @PostMapping
    public Result save(@RequestBody User user) {
        return Result.success(userService.saveOrUpdate(user));
    }

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(userService.removeBatchByIds(ids));
    }

    /**
     * 查询单个
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 根据身份信息查找user
     * @param role
     * @return
     */
    @GetMapping("/role/{role}")
    public Result findUsersByRole(@PathVariable String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        List<User> list = userService.list(queryWrapper);
        return Result.success(list);
    }


    /**
     * 根据用户名查找用户信息
     * 极其不友好
     *
     * @param username
     * @return
     */
    @GetMapping("/username/{username}")
    public Result findOneByName(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return Result.success(userService.getOne(queryWrapper));
    }

    /**
     * 查询所有
     *
     * @return
     */
    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    /**
     * 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param user
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           User user) {
        //IPage<User> page = userService.getPage(pageNum, pageSize, user);
        //if (pageNum > page.getPages()) {
        //    page = userService.getPage((int) page.getPages(), pageSize, user);
        //}
        Page<User> page = userService.findPage(new Page<>(pageNum, pageSize), user);
        return Result.success(page);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDTO) {
        if (StrUtil.isNotBlank(userDTO.getUsername()) || StrUtil.isNotBlank(userDTO.getPassword())) {

            return Result.success(userService.login(userDTO));
        } else {
            return Result.error(Constants.CODE_400, "参数错误");
        }
    }

    /**
     * 修改密码
     * @param UserDTO
     * @return
     */
    @PostMapping("/password")   //    /user/password
    public Result password(@RequestBody UserDTO userDTO) {
        userService.updatePassword(userDTO);
        return Result.success();
    }



    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (StrUtil.isNotBlank(user.getUsername()) || StrUtil.isNotBlank(user.getPassword())) {
            return Result.success(userService.register(user));
        } else {
            return Result.error(Constants.CODE_400, "参数错误");
        }
    }

    /**
     * 导出接口
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        //从数据库中查询出所有数据
        List<User> list = userService.list();
        //通过hutool工具创建writer写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("avatarUrl", "头像");

        //次性写出list内的对象到excel，使用默认格式，强制输出标题
        writer.write(list, true);

        //设置浏览器响应格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        // 关闭writer，释放内存
        writer.close();
        //此处记得关闭输出Servlet流
        IoUtil.close(out);
    }

    @PostMapping("/import")
    public Result importFile(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 表头不能是中文，且必须与javabean属性对应
        //List<User> userList = reader.readAll(User.class);

        //忽略表头中文，直接读取表内容
        List<List<Object>> list = reader.read(1);
        List<User> userList = CollUtil.newArrayList();
        for (List<Object> row : list) {
            User user = new User();
            user.setUsername(row.get(0).toString());
            user.setPassword(row.get(1).toString());
            user.setNickname(row.get(2).toString());
            user.setEmail(row.get(3).toString());
            user.setPhone(row.get(4).toString());
            user.setAddress(row.get(5).toString());
            user.setAvatarUrl(row.get(6).toString());
            userList.add(user);
        }

        userService.saveBatch(userList);

        reader.close();
        inputStream.close();
        return Result.success(true);
    }

}
