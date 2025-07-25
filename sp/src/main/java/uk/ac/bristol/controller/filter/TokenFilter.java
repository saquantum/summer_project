package uk.ac.bristol.controller.filter;

import org.springframework.stereotype.Component;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.service.TokenBlacklistService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <a href="https://qiita.com/rion0726ittoti/items/895b1feaa827c21488ad">filter</a>
 */

@Component
public class TokenFilter implements Filter {

    private final TokenBlacklistService tokenBlacklistService;

    public TokenFilter(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest request && servletResponse instanceof HttpServletResponse response)) {
            throw new ServletException("Protocols other than HTTP are not supported");
        }

        String token;
        try {
            token = JwtUtil.getJWTFromCookie(request);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":400, \"message\":\"Failed to read token from cookie\"}");
            return;
        }

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + Code.LOGIN_TOKEN_MISSING + ", \"message\":\"Token is missing, please return to sign-in page\"}");
            return;
        }
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + Code.LOGIN_TOKEN_ERR + ", \"message\":\"Invalid or expired token\"}");
            return;
        }

        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + Code.LOGIN_TOKEN_ERR + ", \"message\":\"Already logout, token in blacklist\"}");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
