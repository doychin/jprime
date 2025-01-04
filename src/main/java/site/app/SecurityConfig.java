package site.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@ConditionalOnProperty(prefix = "jprime", name = "security.enabled", havingValue = "true",
    matchIfMissing = true)
public class SecurityConfig {

    interface AuthoritiesConverter extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {}

    @SuppressWarnings("unchecked")
    @Bean
    AuthoritiesConverter realmRolesAuthoritiesConverter() {
        return claims -> {
            var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));
            return roles.stream()
                .flatMap(Collection::stream)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        };
    }

    @Bean
    GrantedAuthoritiesMapper authenticationConverter(
        Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
        return authorities -> authorities.stream()
            .filter(OidcUserAuthority.class::isInstance)
            .map(OidcUserAuthority.class::cast)
            .map(OidcUserAuthority::getIdToken)
            .map(OidcIdToken::getClaims)
            .map(authoritiesConverter::convert)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http,
        final ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(requests -> {
                requests.requestMatchers(HttpMethod.GET, "/halls/**", "/sessions/**", "/submissions/**")
                    .permitAll();
                requests.requestMatchers("/assets/**", "/css/**", "/fonts/**", "/images/**", "/js/**",
                    "/nav/**", "/image/**", "/tickets/**", "/speaker/**", "/agenda/**", "/pwa/**", "/qr/**",
                    "/*").permitAll();

                requests.requestMatchers("/admin/**", "/raffle/**").hasAuthority("Administrator");
                requests.requestMatchers("/api/**").hasAuthority("Tickets");
            });

        http.oauth2Login(Customizer.withDefaults());

        http.logout(logout -> {
            var logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
            logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/");
            logout.logoutSuccessHandler(logoutSuccessHandler);
            logout.invalidateHttpSession(true);
        });

        return http.build(); // #5
    }
}
