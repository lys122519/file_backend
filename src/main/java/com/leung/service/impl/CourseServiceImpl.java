package com.leung.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Course;
import com.leung.mapper.CourseMapper;
import com.leung.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
@Service

public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {
    @Resource
    private CourseMapper courseMapper;

    @Override
    public Page<Course> findPage(Page<Course> page, String name){
        return courseMapper.findPage(page,name);
    }


    @Override
    public IPage<Course> getPage(int currentPage, int pageSize) {
        IPage<Course> page = new Page<>(currentPage, pageSize);
        courseMapper.selectPage(page, null);
        return page;
    }

    @Override
    public IPage<Course> getPage(int currentPage, int pageSize, Course course) {
        LambdaQueryWrapper<Course> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.like(Strings.isNotEmpty(course.getName()), Course::getName, course.getName());
        //lambdaQueryWrapper.like(Strings.isNotEmpty(course.getEmail()), Course::getEmail, course.getEmail());
        //lambdaQueryWrapper.like(Strings.isNotEmpty(course.getAddress()), Course::getAddress, course.getAddress());
        //lambdaQueryWrapper.orderByDesc(Course::getId);
        IPage<Course> page = new Page<>(currentPage, pageSize);
        courseMapper.selectPage(page, lambdaQueryWrapper);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setStudentCourse(Integer courseId, Integer studentId) {
        courseMapper.deleteStudentCourse(courseId,studentId);
        courseMapper.setStudentCourse(courseId, studentId);
    }
}
