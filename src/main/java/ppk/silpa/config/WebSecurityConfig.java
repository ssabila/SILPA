package ppk.silpa.config;

import ppk.silpa.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter authenticationFilter;

    public WebSecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter authenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) ->
                        authorize
                                // Endpoint Pengguna
                                .requestMatchers(HttpMethod.GET, "/api/pengguna/saya").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/api/pengguna/saya").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/api/pengguna/saya/kata-sandi").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/pengguna/saya").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/pengguna/saya/nama").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/pengguna/saya/email").authenticated()

                                // Endpoint Publik
                                .requestMatchers(
                                        "/api/auth/**", "/files/**", "/swagger-ui.html", "/swagger-ui/**",
                                        "/v3/api-docs", "/v3/api-docs/**", "/api/info-perizinan/**"
                                ).permitAll()

                                // Endpoint Mahasiswa
                                .requestMatchers(HttpMethod.POST, "/api/perizinan").hasAuthority("MAHASISWA")
                                .requestMatchers(HttpMethod.GET, "/api/perizinan/saya").hasAuthority("MAHASISWA")
                                .requestMatchers(HttpMethod.PUT, "/api/perizinan/{id}/revisi").hasAuthority("MAHASISWA")
                                .requestMatchers(HttpMethod.DELETE, "/api/perizinan/{id}").hasAuthority("MAHASISWA")
                                .requestMatchers(HttpMethod.PATCH, "/api/perizinan/{id}/deskripsi").hasAuthority("MAHASISWA")
                                .requestMatchers(HttpMethod.GET, "/api/mahasiswa/dashboard").hasAuthority("MAHASISWA")

                                // Endpoint Admin
                                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/api/statistik/**").hasAuthority("ADMIN")

                                // Endpoint Perizinan Lainnya
                                .requestMatchers(HttpMethod.GET, "/api/perizinan/filter").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/perizinan/{id}").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/perizinan").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/perizinan/{id}/status").hasAuthority("ADMIN")

                                // Endpoint Notifikasi
                                .requestMatchers("/api/notifikasi/saya").authenticated()

                                // Default: Semua request lain harus terotentikasi
                                .anyRequest().authenticated()
                ).sessionManagement( session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

