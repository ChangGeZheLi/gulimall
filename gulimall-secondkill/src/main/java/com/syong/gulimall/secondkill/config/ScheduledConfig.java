package com.syong.gulimall.secondkill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description: spring做定时任务
 */
@EnableScheduling
@EnableAsync
@Configuration
public class ScheduledConfig {
}
