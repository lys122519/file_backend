package com.leung.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leung.entity.Files;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
public interface IArticleService extends IService<Article> {
    public IPage<Article> getPage(int currentPage, int pageSize);
    public IPage<Article> getPage(int currentPage, int pageSize, String name);

    Page<Article> findPage(Page<Article> articlePage, String name);
}
