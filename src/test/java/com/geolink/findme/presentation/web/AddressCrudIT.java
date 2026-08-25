package com.geolink.findme.presentation.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.geolink.findme.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des adresses (endpoints figés du Projet 4) et des règles métier serveur
 * (cahier des charges §2.2) : création, quota de 4 max → 409, propriété stricte → 403,
 * ressource inexistante → 404, accès non authentifié → 401.
 */
class AddressCrudIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String addressBody(String numero) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "label", "Domicile", "pays", "Cameroun", "ville", "Douala", "quartier", "Akwa",
                "rue", "Rue Joffre", "numero", numero, "latitude", 4.05, "longitude", 9.7));
    }

    // addresses.user_id référence users(id) : un bearer() forgé (UUID non persisté) échoue avec une
    // violation de contrainte FK dès qu'on crée réellement une adresse. Il faut donc une inscription
    // réelle pour tout scénario qui écrit en base ; bearer() reste valable pour les lectures seules.
    private String signupToken() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@geolink.africa";
        var result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "motDePasse", "Password1",
                                "prenom", "Test", "nom", "User"))))
                .andExpect(status().isCreated())
                .andReturn();
        String accessToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
        return "Bearer " + accessToken;
    }

    @Test
    void creation_adresse_retourne_201() throws Exception {
        mockMvc.perform(post("/api/addresses")
                        .header("Authorization", signupToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addressBody("100")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.ville").value("Douala"));
    }

    @Test
    void liste_ne_renvoie_que_les_adresses_du_proprietaire() throws Exception {
        String ownerToken = signupToken();
        mockMvc.perform(post("/api/addresses").header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addressBody("200")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/addresses").header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        // Un autre utilisateur ne voit aucune adresse (lecture seule : bearer() forgé suffit).
        mockMvc.perform(get("/api/addresses").header("Authorization", bearer(UUID.randomUUID(), "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void cinquieme_adresse_retourne_409_quota_depasse() throws Exception {
        String ownerToken = signupToken();
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/addresses").header("Authorization", ownerToken)
                            .contentType(MediaType.APPLICATION_JSON).content(addressBody("30" + i)))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/addresses").header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(addressBody("305")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ADDRESS_QUOTA_EXCEEDED"));
    }

    @Test
    void acces_a_l_adresse_d_un_autre_utilisateur_retourne_403() throws Exception {
        var result = mockMvc.perform(post("/api/addresses").header("Authorization", signupToken())
                        .contentType(MediaType.APPLICATION_JSON).content(addressBody("400")))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        String addressId = created.get("id").asText();

        mockMvc.perform(get("/api/addresses/" + addressId)
                        .header("Authorization", bearer(UUID.randomUUID(), "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ADDRESS_ACCESS_DENIED"));
    }

    @Test
    void adresse_inexistante_retourne_404() throws Exception {
        mockMvc.perform(get("/api/addresses/" + UUID.randomUUID())
                        .header("Authorization", bearer(UUID.randomUUID(), "USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    void acces_sans_token_retourne_401() throws Exception {
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isUnauthorized());
    }
}
