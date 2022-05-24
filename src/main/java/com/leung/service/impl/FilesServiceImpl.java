package com.leung.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leung.entity.Files;
import com.leung.entity.User;
import com.leung.mapper.FilesMapper;
import com.leung.service.IFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.models.auth.In;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author leung
 * @since 2022-03-28
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FilesServiceImpl extends ServiceImpl<FilesMapper, Files> implements IFilesService {


    @Resource
    public FilesMapper fileMapper;

    @Override
    public boolean saveFile(Files file) {
        return fileMapper.insert(file) > 0;
    }

    /**
     * 逻辑删除
     * @param id
     * @return
     */
    @Override
    public boolean selectAndUpdateById(Integer id) {
        Files files = fileMapper.selectById(id);
        return fileMapper.deleteById(files) > 0;
    }

    @Override

    public boolean selectAndUpdateBatchByIds(List<Integer> ids) {
        try {
            QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", ids);
            List<Files> files = fileMapper.selectList(queryWrapper);
            for (Files file : files) {
                fileMapper.deleteById(file);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @Override
    public String selectFileByMD5(String md5) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5", md5);
        List<Files> filesList = fileMapper.selectList(queryWrapper);
        return filesList.size() == 0 ? null : filesList.get(0).getUrl();


    }

    @Override
    public IPage<Files> getPage(int currentPage, int pageSize) {
        IPage<Files> page = new Page<>(currentPage, pageSize);
        fileMapper.selectPage(page, null);
        return page;
    }


    /**
     * 分页查询
     *
     * @param currentPage
     * @param pageSize
     * @param fileName
     * @return
     */
    @Override
    public IPage<Files> getPage(int currentPage, int pageSize, String fileName) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(fileName)) {
            queryWrapper.like("name", fileName);
        }
        IPage<Files> page = new Page<>(currentPage, pageSize);
        fileMapper.selectPage(page, queryWrapper);
        return page;
    }
}
