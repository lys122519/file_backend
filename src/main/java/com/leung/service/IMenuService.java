package com.leung.service;

import com.leung.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author leung
 * @since 2022-04-01
 */
public interface IMenuService extends IService<Menu> {

    List<Menu> findMenus(String name);
}
