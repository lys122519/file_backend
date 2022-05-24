package com.leung.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.common.Constants;
import com.leung.common.Result;
import com.leung.entity.Article;
import com.leung.entity.Files;
import com.leung.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.leung.service.ICourseService;
import com.leung.entity.Course;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private ICourseService courseService;

    @PostMapping
    public Result save(@RequestBody Course course) {
        return Result.success(courseService.saveOrUpdate(course));
    }

    @PostMapping("/studentCourse/{studentId}/{courseId}")
    public Result save(@PathVariable Integer courseId,@PathVariable Integer studentId) {
        courseService.setStudentCourse(courseId,studentId);
        return Result.success();
    }


    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(courseService.removeById(id));
    }


    /**
     * 更新
     *
     * @param course
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Course course) {

        courseService.updateById(course);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(courseService.removeBatchByIds(ids));
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(courseService.getById(id));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(courseService.list());
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           Course course) {

        //IPage<Course> page = courseService.getPage(pageNum, pageSize, course);
        //if (pageNum > page.getPages()) {
        //    page = courseService.getPage((int) page.getPages(), pageSize, course);
        //}

        Page<Course> page = courseService.findPage(new Page<>(pageNum, pageSize), course.getName());
        return Result.success(page);

    }
}
