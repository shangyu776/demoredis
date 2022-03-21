package com.hmdp.utils;

import cn.hutool.core.util.RandomUtil;

public class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";

    public static final Long LOGIN_CODE_TTL=2L;

    public static final Long LOGIN_USER_TTL=30L;

    public static final String LOGIN_USER_KEY="login:token:";

    public static final String USER_NICK_NAME_PREFIX="user_";


}
