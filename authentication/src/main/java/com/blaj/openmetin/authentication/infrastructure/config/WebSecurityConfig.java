package com.blaj.openmetin.authentication.infrastructure.config;

import com.blaj.openmetin.authentication.infrastructure.properties.SecurityProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final SecurityProperties securityProperties;

  @Bean
  public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            requests -> {
              requests
                  .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                  .permitAll();
              requests.requestMatchers("/me", "/api/**").authenticated();
              requests.anyRequest().denyAll();
            })
        .httpBasic(Customizer.withDefaults())
        .formLogin(Customizer.withDefaults())
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(List.of(securityProperties.allowedOrigins()));
    corsConfiguration.setAllowedMethods(List.of(securityProperties.allowedMethods()));
    corsConfiguration.setAllowCredentials(true);
    corsConfiguration.setAllowedOriginPatterns(List.of(securityProperties.allowedOrigins()));
    corsConfiguration.setAllowedHeaders(List.of("*"));

    var urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
    urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

    return urlBasedCorsConfigurationSource;
  }

  @Bean
  public CorsFilter corsFilter() {
    return new CorsFilter(corsConfigurationSource());
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    var userDetails =
        User.builder()
            .username(securityProperties.basicAuthUsername())
            .password(passwordEncoder.encode(securityProperties.basicAuthPassword()))
            .roles("USER")
            .build();

    return new InMemoryUserDetailsManager(userDetails);
  }
}
