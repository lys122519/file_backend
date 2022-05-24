package com.leung.mapper;

import com.leung.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author leung
 * @since 2022-05-22
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    List<Comment> findCommentDetail(@Param("articleId") Integer articleId);
}
