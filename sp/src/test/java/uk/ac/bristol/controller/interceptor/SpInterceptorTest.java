package uk.ac.bristol.controller.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.bristol.config.SpMVCConfigSupport;
import uk.ac.bristol.controller.DummyController;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DummyController.class)
@Import({SpInterceptor.class, SpMVCConfigSupport.class})
public class SpInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    static Stream<Arguments> paths() {
        return Stream.of(
                arguments("/api/user/hello"),
                arguments("/api/asset/hello"),
                arguments("/api/warning/hello")
        );
    }

    @ParameterizedTest
    @MethodSource("paths")
    public void testPathWithoutToken(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("paths")
    public void testPathWithNonAdminToken(String path) throws Exception {
        String token = JwtUtil.generateJWT(Map.of("id", "test", "isAdmin", false));

        mockMvc.perform(get(path).cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("paths")
    public void testPathWithAdminToken(String path) throws Exception {
        String token = JwtUtil.generateJWT(Map.of("id", "admin", "isAdmin", true));

        mockMvc.perform(get(path).cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }

    @Test
    public void testAdminPathWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAdminPathWithNonAdminToken() throws Exception {
        String token = JwtUtil.generateJWT(Map.of("id", "test", "isAdmin", false));

        mockMvc.perform(get("/api/admin/hello").cookie(new Cookie("token", token)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAdminPathWithAdminToken() throws Exception {
        String token = JwtUtil.generateJWT(Map.of("id", "admin", "isAdmin", true));

        mockMvc.perform(get("/api/admin/hello").cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }
}
