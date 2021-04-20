package com.syong.gulimall.product.execption;

import com.syong.common.exception.BizCodeEnum;
import com.syong.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 将controller中的异常集中处理
 * 1、使用@RestControllerAdvice并指定处理哪个包下的异常
 * 2、使用@ExceptionHandler(value = MethodArgumentNotValidException.class)
 */
@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.syong.gulimall.product.controller")
@RestControllerAdvice(basePackages = "com.syong.gulimall.product.controller")
public class GulimallExeceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现异常{}，异常类型{}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String,String > errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((item)->{
            String message = item.getDefaultMessage();
            String field = item.getField();

            errorMap.put(field,message);
        });

        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMsg())
                .put("data",errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(),BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }

}
