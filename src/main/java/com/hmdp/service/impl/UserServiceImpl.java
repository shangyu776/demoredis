package com.hmdp.service.impl;

import ch.qos.logback.core.util.TimeUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session){
        //校验手机号
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("faile phone is worng");
        }
        String code = RandomUtil.randomNumbers(6);
        //set key value  ex 120
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        log.debug("success code="+code+"");

        return  Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO, HttpSession httpSession) {
        //校验手机号
        String phone = loginFormDTO.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("phone is faile");
        }

        String cachCode = (String) stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        String code = loginFormDTO.getCode();

        if(cachCode==null||!cachCode.equals(code)){
            return Result.fail("code is faile");
        }

        User user  = query().eq("phone",phone).one();

        if(user == null){
            user = createUserWithPhone(phone);

        }


        String token =  UUID.randomUUID().toString();

        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);

        Map map = BeanUtil.beanToMap(userDTO);
        //通过token放入user
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,map);
        //设置ttl有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,30, TimeUnit.MINUTES);

        return Result.ok(token);
    }


    //创建用户
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        user.setPhone(phone);
        save(user);
        return user;
    }

}
