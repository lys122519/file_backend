package com.leung.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.common.Result;
import com.leung.utils.TokenUtils;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import com.leung.service.ICommentService;
import com.leung.entity.Comment;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author leung
 * @since 2022-05-22
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private ICommentService commentService;

    @PostMapping
    public Result save(@RequestBody Comment comment) {

        if (comment.getId() == null) {
            //新增评论
            comment.setUserId(TokenUtils.getCurrentUser().getId());
            comment.setTime(DateUtil.now());


            if (comment.getPid() != null) {
                //如果是回复，才处理
                Integer pid = comment.getPid();
                Comment pComment = commentService.getById(pid);
                if (pComment.getOriginId() != null) {
                    //当前回复的评论有祖宗，则设置相同的祖宗
                    comment.setOriginId(pComment.getOriginId());
                } else {
                    //设置父级为当前的祖宗
                    comment.setOriginId(comment.getPid());
                }
            }

        }
        return Result.success(commentService.saveOrUpdate(comment));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(commentService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(commentService.removeBatchByIds(ids));
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(commentService.getById(id));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(commentService.list());
    }


    @GetMapping("/tree/{articleId}")
    public Result findTree(@PathVariable Integer articleId) {
        //查询评论及回复
        List<Comment> articleComments = commentService.findCommentDetail(articleId);

        //查询评论
        List<Comment> originList = articleComments.stream().filter(comment -> comment.getOriginId() == null).collect(Collectors.toList());

        //设置评论的回复
        for (Comment origin : originList) {
            //comments为回复对象集合
            List<Comment> comments = articleComments.stream().filter(comment -> origin.getId().equals(comment.getOriginId())).collect(Collectors.toList());
            comments.forEach(comment -> {
                Optional<Comment> pComment = articleComments.stream().filter(c1 -> c1.getId().equals(comment.getPid())).findFirst();
                pComment.ifPresent((v -> {
                    //找到父级评论的用户id和用户昵称，并设置给当前回复对象
                    comment.setpUserId(v.getUserId());
                    comment.setpNickName(v.getNickname());

                }));
            });

            origin.setChildren(comments);
        }

        return Result.success(originList);
    }


    //@GetMapping("/page")
    //public Result findPage(@RequestParam Integer pageNum,
    //@RequestParam Integer pageSize) {
    //    return Result.success(commentService.page(new Page<>(pageNum, pageSize)));
    //}
}
