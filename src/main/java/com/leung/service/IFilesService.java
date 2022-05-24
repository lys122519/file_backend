package com.leung.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leung.entity.Files;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leung.entity.User;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author leung
 * @since 2022-03-28
 */
public interface IFilesService extends IService<Files> {
    boolean saveFile(Files file);
    boolean selectAndUpdateById(Integer id);
    boolean selectAndUpdateBatchByIds(List<Integer> ids);
    /**
     * 查找是否有相同md5 如果有则返回url 否则返回空
     * @param md5
     * @return
     */
    String selectFileByMD5(String md5);

    public IPage<Files> getPage(int currentPage, int pageSize);
    public IPage<Files> getPage(int currentPage, int pageSize, String fileName);

}
