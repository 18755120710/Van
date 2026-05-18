package butvan.cn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AgentExecutorConfig {

    @Bean
    public ThreadPoolTaskExecutor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);// 核心线程数
        executor.setMaxPoolSize(8);// 最大线程数
        executor.setQueueCapacity(100);// 等待队列大小
        executor.setThreadNamePrefix("agent-run-");// 线程名前缀，方便排查日志

        executor.initialize();// 初始化线程

        return executor;
    }
}
