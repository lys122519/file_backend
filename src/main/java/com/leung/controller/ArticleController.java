package com.leung.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.common.Result;
import com.leung.entity.Course;
import com.leung.entity.Files;
import com.leung.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.leung.service.IArticleService;
import com.leung.entity.Article;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Resource
    private IArticleService articleService;

    @PostMapping
    public Result save(@RequestBody Article article) {

        if (article.getId() == null) {
            //新增
            article.setTime(DateUtil.now());
            article.setUserId(TokenUtils.getCurrentUser().getId());
        }
        return Result.success(articleService.saveOrUpdate(article));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(articleService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(articleService.removeBatchByIds(ids));
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(articleService.getById(id));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(articleService.list());
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {
        //IPage<Article> page = articleService.getPage(pageNum, pageSize, name);
        //if (pageNum > page.getPages()) {
        //    page = articleService.getPage((int) page.getPages(), pageSize, name);
        //}

        Page<Article> page = articleService.findPage(new Page<Article>(pageNum, pageSize), name);


        return Result.success(page);
        //return Result.success(articleService.page(new Page<>(pageNum, pageSize)));
    }
}
