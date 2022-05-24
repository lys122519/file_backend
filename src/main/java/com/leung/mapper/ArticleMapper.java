package com.leung.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leung.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author leung
 * @since 2022-05-21
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    Page<Article> findPage(Page<Article> page, @Param("name") String name);
}
