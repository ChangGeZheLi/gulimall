package com.syong.gulimall.member.exception;

/**
 * @Description:
 */
public class MobileExistException extends RuntimeException{

    public MobileExistException() {
        super("手机号已存在");
    }
}
