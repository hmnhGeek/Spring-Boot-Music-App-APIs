package com.musicapp.music_app.config;


import com.musicapp.music_app.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Steps to create this config file.
 *
 * 1. A User entity should be created which maps to user details (username, password & roles) in a mongodb collection.
 * 2. A repository (eg: UserRepository) to interact with the `users` collection.
 * 3. A {@code UserDetailsService} implementation (from {@code org.springframework.security.core.userdetails}) to fetch user details.
 * 4. A configuration {@code SecurityConfig} to integrate everything with Spring Security.
 */

// The @EnableWebSecurity annotation activates Spring Security's features, such as request authorization and authentication mechanisms.
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // If you're trying to use Spring Boot 2.6.0 or higher, you'll need to either resolve the circular references as mentioned by others, or set spring.main.allow-circular-references to true.

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    // @Bean: Declares this method as a Spring bean, meaning its return value will be managed by Spring.
    /**
     *
     * @param http Configures web-based security for specific HTTP requests.
     * @return A type of {@code SecurityFilterChain} object using the builder pattern.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        /*
          authorizeHttpRequests: Sets rules for which endpoints require authentication and authorization.
          /public/**: Open to everyone (no authentication needed).
          /journal/** and /user/**: Requires the user to be authenticated (logged in).
          /admin/**: Requires the user to have the ADMIN role.
          anyRequest(): All other requests must be authenticated.

          Basic Authentication is stateless; i.e., every request must send headers and that auth credentials are not
          stored in any state on the server. However, this can be bypassed using sessions where a cookie is provided to
          maintain the session and then a logout functionality is also required.
         */

        return http.authorizeHttpRequests(request -> request
//                        .requestMatchers("/api/songs/**").permitAll()
                        .requestMatchers("/api/songs/**", "/users/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults()) // httpBasic(Customizer.withDefaults()): Enables basic authentication (username/password prompt in the browser or API client).
                .csrf(AbstractHttpConfigurer::disable) // csrf(AbstractHttpConfigurer::disable): Disables CSRF (Cross-Site Request Forgery) protection. This might be fine for APIs but could be unsafe for web apps if not carefully managed.
                .build();
    }

    /**
     * Configures global authentication settings for the application.
     *
     * This method binds the custom UserDetailsService implementation
     * and a password encoder to the Spring Security authentication manager.
     * It ensures that user details are retrieved from the provided
     * `UserDetailsServiceImpl` and that passwords are validated using the
     * specified password encoder (BCryptPasswordEncoder).
     *
     * The `UserDetailsService` is responsible for loading user-specific data
     * (like username, password, and roles) for authentication and authorization.
     * The `passwordEncoder()` method ensures that the passwords stored in the
     * system are securely hashed and can be matched during the authentication process.
     *
     * @param auth the AuthenticationManagerBuilder used to configure authentication
     * @throws Exception if authentication setup fails
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * Creates and returns a bean for the PasswordEncoder.
     * This method provides a BCryptPasswordEncoder, which is a secure hashing
     * algorithm for encoding passwords. BCrypt automatically incorporates a
     * randomly generated salt to ensure that even identical passwords produce
     * different hashes, providing strong security against rainbow table attacks.
     *
     * Rainbow table attacks involve using precomputed tables of hash values to
     * reverse-engineer the original password. Using a unique salt with each hash
     * mitigates this attack, as it ensures that the same password will generate
     * different hash values each time.
     * For more information on rainbow table attacks, see:
     * https://en.wikipedia.org/wiki/Rainbow_table
     *
     * The encoder is used during both password storage (hashing) and
     * verification (matching the hashed password with the raw password).
     *
     * @return a BCryptPasswordEncoder instance for secure password encoding
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}