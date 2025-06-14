package uk.ac.bristol.controller.interceptor;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SpInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        System.out.println("Request Method：" + request.getMethod() + ", URI：" + request.getRequestURI());

        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) {
            String token = JwtUtil.getJWTFromCookie(request, response);
            if (token == null) {
                return false;
            }
            Claims claims;
            try {
                claims = JwtUtil.parseJWT(token);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token invalid or expired.");
                return false;
            }

            if (!Boolean.TRUE.equals(claims.get("isAdmin", Boolean.class))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied. Admin only.");
                return false;
            }
        }
        return true;
    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        System.out.println("postHandle");
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        System.out.println("afterCompletion");
//    }
}
