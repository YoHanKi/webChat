package com.api.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter(AuthenticationManager authManager) {
        super(authManager);
        // /login 또는 /login/admin 요청을 매칭
        this.setRequiresAuthenticationRequestMatcher(
                new OrRequestMatcher(
                        new AntPathRequestMatcher("/api/login")
                )
        );
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (!request.getMethod().equals("POST")) {
            return super.attemptAuthentication(request, response);
        }

        Map<String, String> requestJSON = parseRequestJSON(request);
        String username = requestJSON.get(USERNAME);
        username = username != null ? username.trim() : "";
        String password = requestJSON.get(PASSWORD);
        password = password != null ? password : "";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);

        this.setDetails(request, authToken);

        return this.getAuthenticationManager().authenticate(authToken);

    }

    private Map<String, String> parseRequestJSON(HttpServletRequest request) {
        try (Reader reader = new InputStreamReader(request.getInputStream())) {
            return objectMapper.readValue(reader, new TypeReference<>() {});
        } catch (Exception e) {
            throw new AuthenticationServiceException("Failed to parse request JSON", e);
        }
    }
}