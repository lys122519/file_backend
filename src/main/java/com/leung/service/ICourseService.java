package com.leung.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Article;
import com.leung.entity.Course;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
public interface ICourseService extends IService<Course> {

    /**
     * 连表查询 1对1
     * @param page
     * @param name
     * @return
     */
    Page<Course> findPage(Page<Course> page, String name);

    IPage<Course> getPage(int currentPage, int pageSize);

    IPage<Course> getPage(int currentPage, int pageSize, Course course);

    void setStudentCourse(Integer courseId, Integer studentId);
}
