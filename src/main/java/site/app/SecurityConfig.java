package site.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import site.model.User;
import site.repository.SpeakerRepository;
import site.repository.UserRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public final ApplicationContext context;

    private final Environment environment;

    private final UserRepository userRepository;

    private final SpeakerRepository speakerRepository;

    public SecurityConfig(ApplicationContext context, Environment environment, UserRepository userRepository,
        SpeakerRepository speakerRepository) {
        this.context = context;
        this.environment = environment;
        this.userRepository = userRepository;
        this.speakerRepository = speakerRepository;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new AuthenticationProvider() {

            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String email = (String) authentication.getPrincipal();
                String providedPassword = (String) authentication.getCredentials();

                //admin hack
                if ("admin".equals(email) && providedPassword.equals(
                    environment.getProperty("admin.password"))) {
                    return new UsernamePasswordAuthenticationToken(email, providedPassword,
                        Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
                }

                User user = userRepository.findUserByEmail(email);
                if (user == null) {
                    user = speakerRepository.findByEmail(email);
                    if (StringUtils.isEmpty(user.getPassword())) {
                        user = null;
                    }
                }

                if (user == null || !passwordEncoder().matches(providedPassword, user.getPassword())) {
                    throw new BadCredentialsException(
                        "Username/Password does not match for " + authentication.getPrincipal());
                }

                return new UsernamePasswordAuthenticationToken(email, providedPassword,
                    Collections.singleton(new SimpleGrantedAuthority("USER")));
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        });
    }

    private RequestMatcher[] antMatcher(String... patterns) {
        return Stream.of(patterns).map(AntPathRequestMatcher::antMatcher).toArray(RequestMatcher[]::new);
    }

    private RequestMatcher[] antMatcher(HttpMethod httpMethod, String... patterns) {
        return Stream.of(patterns)
            .map(pattern -> AntPathRequestMatcher.antMatcher(httpMethod, pattern))
            .toArray(RequestMatcher[]::new);
    }

    @Bean
    SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            .and()
            .csrf()
            .disable()
            .authorizeHttpRequests()
            .requestMatchers(
                antMatcher("/", "/login", "/about", "/nav/**", "/cfp", "/cfp-thank-you", "/privacy-policy",
                    "/signup", "/resetPassword", "/createNewPassword", "/successfulPasswordChange",
                    "/image/**", "/tickets/**", "/team", "/venue", "/speaker/**", "/speakers", "/agenda/**",
                    "/404", "/captcha-image", "/pwa", "/pwa/**", "/qr/**", "/asset/**", "/assets/**",
                    "/css/**", "/fonts/**", "/images/**", "/js/**"))
            .permitAll()
            .requestMatchers(
                antMatcher(HttpMethod.GET, "/halls", "/halls/**", "/sessions", "/sessions/**", "/submissions",
                    "/submissions/**", "/sw.js", "/manifest.json"))
            .permitAll()
            .requestMatchers(antMatcher("/admin/**"))
            .hasAuthority("ADMIN") // #6
            .requestMatchers(antMatcher("/raffle/**"))
            .hasAuthority("ADMIN") // #7
            .requestMatchers(antMatcher("/api/**"))
            .hasAuthority("ADMIN") // #7
            .requestMatchers(antMatcher("/user/**"))
            .hasAuthority("USER") //will contain schedule and etc
            .anyRequest()
            .authenticated()  // 8
            .and()
            .formLogin()
            .successHandler(SecurityConfig::redirectToAdmin) // #9
            .loginPage("/login") // #10
            .permitAll();
        return http.build(); // #5
    }

    private static void redirectToAdmin(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        boolean isAdmin =
            authentication.getAuthorities().stream().anyMatch(p -> "ADMIN".equals(p.getAuthority()));
        if (isAdmin) {
            new DefaultRedirectStrategy().sendRedirect(request, response, "/admin");
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
