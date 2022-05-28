package com.leung.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leung.common.Constants;
import com.leung.common.Result;
import com.leung.config.AuthAccess;
import com.leung.entity.Files;
import com.leung.entity.User;
import com.leung.mapper.FilesMapper;
import com.leung.service.IFilesService;
import com.leung.utils.OBSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Description: 文件上传接口
 * @author: leung
 * @date: 2022-03-28 15:28
 */
@RestController
@RequestMapping("/file")
public class FilesController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private IFilesService fileService;

    @Resource
    private FilesMapper fileMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    //private static final String BASE_URL = "http://localhost:9090/file/";
    private static final String BASE_URL = "https://file-but.obs.cn-north-4.myhuaweicloud.com/";

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        fileService.selectAndUpdateById(id);


        //设置缓存
        List<Files> list = fileService.list(null);
        setRedisCache(Constants.FILES_KEY, JSONUtil.toJsonStr(list));


        return Result.success();
    }


    /**
     * 更新
     *
     * @param file
     * @return
     */
    @PostMapping("/update")
    public Result save(@RequestBody Files file) {

        fileService.updateById(file);

        //设置缓存
        List<Files> list = fileService.list(null);
        setRedisCache(Constants.FILES_KEY, JSONUtil.toJsonStr(list));

        return Result.success();
    }

    /**
     * 前台查询所有
     *
     * @return
     */
    @AuthAccess
    @GetMapping("/front/all")
    public Result findAll() {
        //先从缓冲获取数据
        String jsonStr = stringRedisTemplate.opsForValue().get(Constants.FILES_KEY);

        List<Files> files;
        if (StrUtil.isBlank(jsonStr)) {
            //没有查询到json
            //从数据库取出数据
            files = fileMapper.selectList(null);
            //将数据缓存到redis
            stringRedisTemplate.opsForValue().set(Constants.FILES_KEY, JSONUtil.toJsonStr(files));
        } else {
            //从redis中获取数据
            files = JSONUtil.toBean(jsonStr, new TypeReference<List<Files>>() {
            }, true);
        }


        return Result.success(files);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return Result.success(fileService.selectAndUpdateBatchByIds(ids));


        //return Result.success(fileService.updateBatchById(files));
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result findById(@PathVariable Integer id) {
        Files files = fileService.getById(id);
        return Result.success(files);
    }

    /**
     * 分页查询接口
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name
    ) {
        IPage<Files> page = fileService.getPage(pageNum, pageSize, name);
        if (pageNum > page.getPages()) {
            page = fileService.getPage((int) page.getPages(), pageSize, name);
        }
        return Result.success(page);
    }

    /**
     * 文件上传接口 OBS
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();

        //存储到磁盘
        File uploadParentFile = new File(fileUploadPath);

        //判断配置的文件目录是否存在，不存在则创造新的文件目录
        if (!uploadParentFile.exists()) {
            uploadParentFile.mkdirs();
        }

        // 定义一个文件的唯一标识码
        String uuid = IdUtil.fastSimpleUUID();

        // 文件名加后缀
        String fileUUID = uuid + StrUtil.DOT + type;
        File uploadFile = new File(fileUploadPath + fileUUID);

        //文件最终URL
        String finalUrl = "";
        // 将获取到的文件存储到磁盘目录
        file.transferTo(uploadFile);
        //获取文件md5
        String fileMD5 = SecureUtil.md5(uploadFile);
        //根据md5查询是否有重复文件
        String urlFromSql = fileService.selectFileByMD5(fileMD5);
        //从数据库查不到相同的md5，则上传，否则将已有文件的url其该文件最终url
        if (urlFromSql != null) {
            finalUrl += urlFromSql;
            //相同md5的文件存在，删除已保存文件
            uploadFile.delete();
        } else {
            //无相同md5文件，则上传
            finalUrl = BASE_URL + fileUUID;
            OBSUtils.uploadFile(fileUUID, uploadFile.getPath());

        }

        // 存储数据库
        Files saveFile = new Files();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size / 1024);
        saveFile.setUrl(finalUrl);
        saveFile.setMd5(fileMD5);
        fileService.saveFile(saveFile);

        //设置缓存
        List<Files> list = fileService.list(null);
        setRedisCache(Constants.FILES_KEY, JSONUtil.toJsonStr(list));

        return finalUrl;

    }





    ///**
    // * 文件上传接口
    // *
    // * @param file
    // * @return
    // * @throws IOException
    // */
    //@PostMapping("/upload")
    //public String upload(@RequestParam MultipartFile file) throws IOException {
    //    String originalFilename = file.getOriginalFilename();
    //    String type = FileUtil.extName(originalFilename);
    //    long size = file.getSize();
    //
    //    //存储到磁盘
    //    File uploadParentFile = new File(fileUploadPath);
    //
    //    //判断配置的文件目录是否存在，不存在则创造新的文件目录
    //    if (!uploadParentFile.exists()) {
    //        uploadParentFile.mkdirs();
    //    }
    //
    //    // 定义一个文件的唯一标识码
    //    String uuid = IdUtil.fastSimpleUUID();
    //
    //    // 文件名加后缀
    //    String fileUUID = uuid + StrUtil.DOT + type;
    //    File uploadFile = new File(fileUploadPath + fileUUID);
    //
    //    //文件最终URL
    //    String finalUrl = "";
    //    // 将获取到的文件存储到磁盘目录
    //    file.transferTo(uploadFile);
    //    //获取文件md5
    //    String fileMD5 = SecureUtil.md5(uploadFile);
    //    //根据md5查询是否有重复文件
    //    String urlFromSql = fileService.selectFileByMD5(fileMD5);
    //    //从数据库查不到相同的md5，则上传，否则将已有文件的url其该文件最终url
    //    if (urlFromSql != null) {
    //        finalUrl += urlFromSql;
    //        //相同md5的文件存在，删除已保存文件
    //        uploadFile.delete();
    //    } else {
    //        //无相同md5文件，则上传
    //        finalUrl = BASE_URL + fileUUID;
    //        OBSUtils.uploadFile(file.getName(), uploadFile.getPath());
    //
    //    }
    //
    //    // 存储数据库
    //    Files saveFile = new Files();
    //    saveFile.setName(originalFilename);
    //    saveFile.setType(type);
    //    saveFile.setSize(size / 1024);
    //    saveFile.setUrl(finalUrl);
    //    saveFile.setMd5(fileMD5);
    //    fileService.saveFile(saveFile);
    //
    //    //设置缓存
    //    List<Files> list = fileService.list(null);
    //    setRedisCache(Constants.FILES_KEY, JSONUtil.toJsonStr(list));
    //
    //    //flushRedis(Constants.FILES_KEY);
    //
    //    return finalUrl;
    //
    //}

    /**
     * 文件下载接口 http://localhost:9090/file/{fileUUID}
     *
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream;
        //根据文件唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);

        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        //通过文件路径读取文件字节流
        byte[] bytes = FileUtil.readBytes(uploadFile);
        //通过输出流返回文件
        outputStream = response.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }


    //设置缓存
    private void setRedisCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    //private void flushRedis(String key) {
    //    stringRedisTemplate.delete(key);
    //}
}
