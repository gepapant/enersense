package com.gepapant.enersense.security;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
        controllers = SecurityAuthorizationTest.TestEndpoints.class
)
@Import({
        SecurityConfig.class,
        SecurityAuthorizationTest.TestEndpoints.class
})
@TestPropertySource(properties = {
        "enersense.security.jwt-secret=012345678901234567890123456789012345678901234567"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @Test
    void loginShouldBePublic() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void devicesShouldRequireAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/devices")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void operatorShouldReadDevices()
            throws Exception {

        mockMvc.perform(
                        get("/api/devices")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_OPERATOR"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void adminShouldReadDevices()
            throws Exception {

        mockMvc.perform(
                        get("/api/devices")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void operatorShouldNotCreateFacility()
            throws Exception {

        mockMvc.perform(
                        post("/api/facilities")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_OPERATOR"
                                                )
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void adminShouldCreateFacility()
            throws Exception {

        mockMvc.perform(
                        post("/api/facilities")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void operatorShouldNotAccessUsers()
            throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_OPERATOR"
                                                )
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void adminShouldAccessUsers()
            throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void operatorShouldAcknowledgeAlert()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/alerts/1/acknowledge"
                        )
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_OPERATOR"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void adminShouldAcknowledgeAlert()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/alerts/1/acknowledge"
                        )
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    @RestController
    @RequestMapping("/api")
    public static class TestEndpoints {

        @PostMapping("/auth/login")
        public Map<String, String> login() {

            return Map.of(
                    "status",
                    "ok"
            );
        }

        @GetMapping("/devices")
        public Map<String, String> devices() {

            return Map.of(
                    "status",
                    "ok"
            );
        }

        @PostMapping("/facilities")
        public Map<String, String> createFacility() {

            return Map.of(
                    "status",
                    "ok"
            );
        }

        @GetMapping("/users")
        public Map<String, String> users() {

            return Map.of(
                    "status",
                    "ok"
            );
        }

        @PatchMapping(
                "/alerts/{alertId}/acknowledge"
        )
        public Map<String, String> acknowledgeAlert(
                @PathVariable Long alertId
        ) {

            return Map.of(
                    "status",
                    "ok"
            );
        }
    }
}