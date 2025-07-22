package uk.ac.bristol.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import uk.ac.bristol.controller.interceptor.PostInterceptor;
import uk.ac.bristol.controller.interceptor.SpInterceptor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class SpMVCConfigSupport extends WebMvcConfigurationSupport {

    @Autowired
    SpInterceptor spInterceptor;

    @Autowired
    PostInterceptor PostInterceptor;

    // redirect URI resources
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spInterceptor)
                .addPathPatterns("/api/user/**")
                .addPathPatterns("/api/asset/**")
                .addPathPatterns("/api/warning/**")
                .addPathPatterns("/api/admin/**");

        registry.addInterceptor(PostInterceptor)
                .addPathPatterns("/**");
    }

    @Bean("notificationExecutor")
    public Executor notificationThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("NotificationSender-");
        executor.initialize();
        return executor;
    }
}
