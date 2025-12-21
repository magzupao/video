package com.video.app.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tech.jhipster.async.ExceptionHandlingAsyncTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@Profile("!testdev & !testprod")
public class AsyncConfiguration implements AsyncConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncConfiguration.class);

    private final TaskExecutionProperties taskExecutionProperties;

    public AsyncConfiguration(TaskExecutionProperties taskExecutionProperties) {
        this.taskExecutionProperties = taskExecutionProperties;
    }

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        LOG.debug("Creating Async Task Executor");
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        return new ExceptionHandlingAsyncTaskExecutor(executor);
    }

    @Bean(name = "videoTaskExecutor")
    public Executor videoTaskExecutor() {
        LOG.info("Configurando ThreadPoolTaskExecutor para procesamiento asíncrono de videos");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Número de hilos base siempre activos
        executor.setCorePoolSize(5);

        // Máximo de hilos que pueden crearse
        executor.setMaxPoolSize(10);

        // Capacidad de la cola de tareas en espera
        executor.setQueueCapacity(100);

        // Prefijo para identificar los hilos en logs
        executor.setThreadNamePrefix("video-async-");

        // Esperar a que terminen las tareas antes de shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Tiempo máximo de espera para shutdown (30 segundos)
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        LOG.info(
            "ThreadPoolTaskExecutor configurado: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
            executor.getCorePoolSize(),
            executor.getMaxPoolSize(),
            executor.getQueueCapacity()
        );

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
