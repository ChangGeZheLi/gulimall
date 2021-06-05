package com.syong.gulimall.member.exception;

/**
 * @Description:
 */
public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("用户名已存在");
    }
}
