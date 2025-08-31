package uk.ac.bristol.controller.filter;

import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestBlockerFilter implements Filter {
    public static volatile boolean BLOCKING_REQUESTS = false;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest request && servletResponse instanceof HttpServletResponse response)) {
            throw new ServletException("Protocols other than HTTP are not supported");
        }
        if (BLOCKING_REQUESTS) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.getWriter().write("{\"status\":503,\"message\":\"System maintenance in progress.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
