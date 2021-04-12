package com.syong.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.syong.common.utils.PageUtils;
import com.syong.gulimall.member.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author syong
 * @email syong@gmail.com
 * @date 2021-04-12 16:18:36
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

