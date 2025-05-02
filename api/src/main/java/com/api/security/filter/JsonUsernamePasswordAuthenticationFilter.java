package com.api.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;
import java.util.Map;

public class JsonUsernamePasswordAuthenticationFilter
        extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter(AuthenticationManager authManager) {
        super.setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        if (!request.getContentType().startsWith("application/json")) {
            return super.attemptAuthentication(request, response);
        }

        try {
            Map<String, String> creds = objectMapper
                    .readValue(request.getInputStream(), Map.class);
            String username = creds.get("username");
            String password = creds.get("password");

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            setDetails(request, authToken);
            return this.getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid JSON", e);
        }
    }
}