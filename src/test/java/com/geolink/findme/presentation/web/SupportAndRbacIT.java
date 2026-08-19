package com.geolink.findme.presentation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geolink.findme.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration du support client et du RBAC des endpoints admin (cahier des charges §2.3,
 * §2.4) : formulaire public de support (sans auth) → 201, liste support réservée
 * ADMIN/SUPPORT_AGENT, liste utilisateurs réservée ADMIN (un USER ou SUPPORT_AGENT → 403, anonyme
 * → 401).
 */
class SupportAndRbacIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void formulaire_public_de_support_retourne_201_sans_authentification() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nom", "Awa Ndiaye", "email", "awa@example.com", "message", "Bonjour, j'ai une question."));
        mockMvc.perform(post("/api/support").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("awa@example.com"));
    }

    @Test
    void liste_support_accessible_au_support_agent() throws Exception {
        mockMvc.perform(get("/api/admin/support").header("Authorization", bearer("SUPPORT_AGENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void liste_utilisateurs_interdite_au_support_agent_403() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer("SUPPORT_AGENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void liste_utilisateurs_interdite_au_role_user_403() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpoints_admin_sans_token_retournent_401() throws Exception {
        mockMvc.perform(get("/api/admin/support"))
                .andExpect(status().isUnauthorized());
    }
}
