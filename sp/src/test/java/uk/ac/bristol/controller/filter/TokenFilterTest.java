package uk.ac.bristol.controller.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.bristol.config.FilterConfig;
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
@Import({FilterConfig.class})
public class TokenFilterTest {

    @Autowired
    private MockMvc mockMvc;

    static Stream<Arguments> protectedPaths() {
        return Stream.of(
                arguments("/api/user/hello"),
                arguments("/api/admin/hello"),
                arguments("/api/asset/hello"),
                arguments("/api/warning/hello")
        );
    }

    @Test
    public void testFilterApiWithMissingToken() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello")));
    }

    @Test
    public void testFilterApiWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/hello").cookie(new Cookie("token", "fake.token.value")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello")));
    }

    @Test
    public void testFilterApi() throws Exception {
        String token = JwtUtil.generateJWT(Map.of("id", "test_user"));

        mockMvc.perform(get("/api/hello").cookie(new Cookie("token", token)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello")));
    }

    @ParameterizedTest
    @MethodSource("protectedPaths")
    public void testProtectedPath_MissingToken(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("missing")));
    }

    @ParameterizedTest
    @MethodSource("protectedPaths")
    public void testProtectedPath_InvalidToken(String path) throws Exception {
        mockMvc.perform(get(path).cookie(new Cookie("token", "invalid.token")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("expired")));
    }

    @ParameterizedTest
    @MethodSource("protectedPaths")
    public void testProtectedPath_ValidToken(String path) throws Exception {
        String token = JwtUtil.generateJWT(Map.of("id", "test_user"));

        mockMvc.perform(get(path).cookie(new Cookie("token", token)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hello")));
    }

}
