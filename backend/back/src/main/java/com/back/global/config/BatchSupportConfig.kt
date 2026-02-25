package com.back.global.config

import io.netty.channel.ConnectTimeoutException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.ResourceAccessException
import java.net.SocketTimeoutException
import java.time.Duration
import java.util.Set

@Configuration
class BatchSupportConfig {
    @Bean
    fun taskExecutor(): AsyncTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 8
            maxPoolSize = 8
            threadNamePrefix = "batch-thread-"
            initialize()
        }

    @Bean
    fun crawlingTaskExecutor(): AsyncTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 4
            queueCapacity = 100
            threadNamePrefix = "crawler-"
            initialize()
        }

    @Bean
    fun retryPolicy(): SimpleRetryPolicy =
        SimpleRetryPolicy(
            4,
            mapOf(
                SocketTimeoutException::class.java to true,
                ResourceAccessException::class.java to true,
                ConnectTimeoutException::class.java to true
            ),
            true
        )
}
