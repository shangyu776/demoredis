package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {

    private String name;

    private StringRedisTemplate stringRedisTemplate;

    private SimpleRedisLock(SimpleRedisLock simpleRedisLock){
        this.name= name;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    private static final String KEY_PREFIX="lock:";

    @Override
    public boolean tryLock(Long timeoutSec) {

        long threadId = Thread.currentThread().getId();
        boolean success =  stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX,threadId+"",timeoutSec, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {

        stringRedisTemplate.delete(KEY_PREFIX+name);
    }
}
