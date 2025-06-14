package uk.ac.bristol.config;

import uk.ac.bristol.controller.interceptor.SpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class SpMVCConfigSupport extends WebMvcConfigurationSupport {

    @Autowired
    SpInterceptor spInterceptor;

    // redirect URI resources
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spInterceptor).addPathPatterns("/user/**").addPathPatterns("/admin/**");
    }
}
