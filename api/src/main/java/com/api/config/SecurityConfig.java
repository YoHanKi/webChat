package com.api.config;

import com.api.security.filter.JsonUsernamePasswordAuthenticationFilter;
import com.api.security.handler.JsonAuthenticationFailureHandler;
import com.api.security.handler.JsonAuthenticationSuccessHandler;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;
    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

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
        jsonFilter.setAuthenticationSuccessHandler(jsonAuthenticationSuccessHandler);
        jsonFilter.setAuthenticationFailureHandler(jsonAuthenticationFailureHandler);

        http
                .csrf(AbstractHttpConfigurer::disable).cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(3)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/login?expired=true")
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/user/register", "/api/room/read").permitAll()
                        .anyRequest().authenticated()
                )
                // 로그아웃 시 세션 삭제
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .invalidateHttpSession(true)
                )

                // 직접 등록한 DaoAuthenticationProvider 사용
                .authenticationProvider(authenticationProvider())
                // JSON 로그인 필터를 UsernamePasswordAuthenticationFilter 위치에 삽입
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 5) CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 1) 허용할 오리진
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        // 2) 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        // 3) 허용할 요청 헤더
        config.setAllowedHeaders(List.of("*"));
        // 4) 자격증명(쿠키) 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 대해 위 설정 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
