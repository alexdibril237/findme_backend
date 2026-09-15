package com.geolink.findme.unit.security;
import com.geolink.findme.security.RestAccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestAccessDeniedHandlerTest {

    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler(new ObjectMapper());

    @Test
    void handle_ecrit_un_403_avec_le_bon_code_erreur() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        handler.handle(request, response, new AccessDeniedException("interdit"));

        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(body.toString()).contains("ACCESS_DENIED");
    }

    @Test
    void handle_inclut_un_message_generique() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        handler.handle(request, response, new AccessDeniedException("interdit"));

        assertThat(body.toString()).contains("Permissions insuffisantes");
    }
}
