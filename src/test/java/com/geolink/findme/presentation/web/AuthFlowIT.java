package com.geolink.findme.presentation.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geolink.findme.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration bout en bout des parcours d'authentification (endpoints figés du Projet 4).
 * Couvre les cas critiques (inscription, connexion, refresh, profil) et les cas limites du cahier
 * des charges §2.1 : email dupliqué → 409, identifiants invalides → 401, mot de passe faible → 400,
 * accès sans token → 401.
 */
class AuthFlowIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String signupBody(String email, String motDePasse) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "email", email, "motDePasse", motDePasse, "prenom", "Ada", "nom", "Lovelace"));
    }

    @Test
    void signup_retourne_201_avec_tokens_et_profil() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("new.user@geolink.africa", "Password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("new.user@geolink.africa"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void signup_email_deja_utilise_retourne_409() throws Exception {
        String body = signupBody("dup@geolink.africa", "Password1");
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_USED"));
    }

    @Test
    void signup_mot_de_passe_faible_sans_majuscule_ni_chiffre_retourne_400() throws Exception {
        // 8 caractères => passe la validation @Size, mais viole la politique métier (majuscule + chiffre).
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("weak@geolink.africa", "password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("WEAK_PASSWORD"));
    }

    @Test
    void signup_mot_de_passe_trop_court_retourne_400_validation() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("short@geolink.africa", "Pw1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void signin_valide_retourne_200_et_un_access_token() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("login@geolink.africa", "Password1")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "login@geolink.africa", "motDePasse", "Password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void signin_mauvais_mot_de_passe_retourne_401_generique() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("wrongpwd@geolink.africa", "Password1")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "wrongpwd@geolink.africa", "motDePasse", "MauvaisMdp9"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void users_me_sans_token_retourne_401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void users_me_avec_token_retourne_le_profil_connecte() throws Exception {
        var result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("me@geolink.africa", "Password1")))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = json.get("accessToken").asText();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@geolink.africa"));
    }

    @Test
    void refresh_avec_le_refresh_token_emet_un_nouvel_access_token() throws Exception {
        var result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("refresh@geolink.africa", "Password1")))
                .andExpect(status().isCreated())
                .andReturn();
        String refreshToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("refreshToken").asText();
        assertThat(refreshToken).isNotBlank();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
