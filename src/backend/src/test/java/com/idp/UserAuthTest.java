package com.idp;

// Traces: US-013, CONR-user-api-001

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idp.repository.AppUserRepository;
import com.idp.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AppUserRepository userRepository;
    @Autowired AuthTokenRepository tokenRepository;

    @BeforeEach
    void cleanUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerLoginAndMeResolveIssuedTokenToUsername() throws Exception {
        mvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "AliceAuth",
                      "password": "correct-password"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("aliceauth"));

        assertThat(userRepository.findByUsername("aliceauth")).isPresent();
        assertThat(userRepository.findByUsername("aliceauth").get().getPasswordHash()).doesNotContain("correct-password");

        String loginResponse = mvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "aliceauth",
                      "password": "correct-password"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("aliceauth"));
    }

    @Test
    void duplicateRegistrationAndInvalidLoginReturnErrors() throws Exception {
        mvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "dupe",
                      "password": "correct-password"
                    }
                    """))
            .andExpect(status().isCreated());

        mvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "DUPE",
                      "password": "another-password"
                    }
                    """))
            .andExpect(status().isBadRequest());

        mvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "dupe",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }
}
