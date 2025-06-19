package uk.ac.bristol.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.bristol.controller.filter.TokenFilter;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TokenFilter> loginFilter() {
        FilterRegistrationBean<TokenFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(new TokenFilter());
        registration.addUrlPatterns("/api/user/*");
        registration.addUrlPatterns("/api/admin/*");
        registration.setOrder(1);
        return registration;
    }
}