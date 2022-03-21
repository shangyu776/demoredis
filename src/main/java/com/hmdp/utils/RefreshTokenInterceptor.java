package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;


    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1 获取session
        String token = request.getHeader("authorization");
        //获取请求头中到token
        Map<Object, Object> map = stringRedisTemplate.opsForHash()
                .entries(RedisConstants.LOGIN_CODE_KEY+token);

        if(map.isEmpty()){
            response.setStatus(401);
            return true;
        }

        UserDTO userDTO = BeanUtil.fillBeanWithMap(map,new UserDTO(),false);

        //存储ThreadLocal
        UserHolder.saveUser(userDTO);

        //刷新token
        stringRedisTemplate.expire(RedisConstants.LOGIN_CODE_KEY+token,
                RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        //6放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
