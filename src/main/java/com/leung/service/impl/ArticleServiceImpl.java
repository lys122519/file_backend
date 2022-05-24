package com.leung.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Article;
import com.leung.entity.Files;
import com.leung.mapper.ArticleMapper;
import com.leung.mapper.FilesMapper;
import com.leung.service.IArticleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    @Resource
    public ArticleMapper articleMapper;


    @Override
    public IPage<Article> getPage(int currentPage, int pageSize) {
        IPage<Article> page = new Page<>(currentPage, pageSize);
        articleMapper.selectPage(page, null);
        return page;
    }

    @Override
    public IPage<Article> getPage(int currentPage, int pageSize, String name) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }
        IPage<Article> page = new Page<>(currentPage, pageSize);
        articleMapper.selectPage(page, queryWrapper);

        return page;
    }

    @Override
    public Page<Article> findPage(Page<Article> page, String name) {
        return articleMapper.findPage(page, name);
    }
}

