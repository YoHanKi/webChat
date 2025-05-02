package com.api.config;

import com.api.security.filter.JsonUsernamePasswordAuthenticationFilter;
import com.api.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // 1) PasswordEncoder 빈
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2) DaoAuthenticationProvider 빈: UserDetailsService + PasswordEncoder 연결
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 3) AuthenticationManager 빈: AuthenticationConfiguration 사용 :contentReference[oaicite:0]{index=0}
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 4) SecurityFilterChain: JSON 로그인 필터 + Provider 등록
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager) throws Exception {
        // JSON 바디 로그인 처리 필터
        var jsonFilter = new JsonUsernamePasswordAuthenticationFilter(authManager);
        jsonFilter.setFilterProcessesUrl("/api/login");
        jsonFilter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler());
        jsonFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/user/register").permitAll()
                        .anyRequest().authenticated()
                )
                // 직접 등록한 DaoAuthenticationProvider 사용
                .authenticationProvider(authenticationProvider())
                // JSON 로그인 필터를 UsernamePasswordAuthenticationFilter 위치에 삽입
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
