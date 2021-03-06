package com.leung.config.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.leung.common.Constants;
import com.leung.config.AuthAccess;
import com.leung.entity.User;
import com.leung.exception.ServiceException;
import com.leung.service.IUserService;
import org.omg.CORBA.StringSeqHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @author: leung
 * @date: 2022-03-28 14:34
 */

public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");

        //
        if (!(handler instanceof HandlerMethod)) {
            return true;
        } else {
            //包含AuthAccess 放行
            HandlerMethod h = (HandlerMethod) handler;
            AuthAccess authAccess = h.getMethodAnnotation(AuthAccess.class);
            if (authAccess != null) {
                return true;
            }
        }


        // 如果不映射到方法直接通过
        //if (!(handler instanceof HandlerMethod)) {
        //    return true;
        //}

        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException(Constants.CODE_401, "无token，请重新登录");
        }

        // 获取 token 中的 user id
        String userId;
        try {
            userId = JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            throw new ServiceException(Constants.CODE_401, "token验证失败,请重新登录");
        }


        //可以将token放在redis中 避免去数据库查询用户
        User user = userService.getById(userId);
        //if (user == null) {
        //    throw new ServiceException(Constants.CODE_401, "用户不存在，请重新登录");
        //}

        // 用户密码加签 验证 token
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try {
            // 验证token
            jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new ServiceException(Constants.CODE_401, "token验证失败,请重新登录");
        }
        return true;
    }
}
