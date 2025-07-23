package uk.ac.bristol.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.bristol.controller.filter.TokenFilter;
import uk.ac.bristol.service.TokenBlacklistService;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TokenFilter> loginFilter(TokenBlacklistService tokenBlacklistService) {
        FilterRegistrationBean<TokenFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new TokenFilter(tokenBlacklistService));
        registration.addUrlPatterns("/api/user/*");
        registration.addUrlPatterns("/api/admin/*");
        registration.addUrlPatterns("/api/asset/*");
        registration.addUrlPatterns("/api/warning/*");
        registration.setOrder(1);
        return registration;
    }
}