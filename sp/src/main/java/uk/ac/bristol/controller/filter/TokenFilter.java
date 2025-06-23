package uk.ac.bristol.controller.filter;

import uk.ac.bristol.controller.Code;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <a href="https://qiita.com/rion0726ittoti/items/895b1feaa827c21488ad">filter</a>
 */

@WebFilter(urlPatterns = "/*")
public class TokenFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest request && servletResponse instanceof HttpServletResponse response)) {
            throw new ServletException("Protocols other than HTTP are not supported");
        }

        String token = JwtUtil.getJWTFromCookie(request, response);
        if (token == null) return;
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(Code.LOGIN_TOKEN_ERR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401, \"message\":\"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
