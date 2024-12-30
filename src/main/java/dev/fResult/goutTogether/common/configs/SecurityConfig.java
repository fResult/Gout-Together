package dev.fResult.goutTogether.common.configs;

import static dev.fResult.goutTogether.common.Constants.*;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.models.RSAKeyProperties;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final String privateKeyBase64;
  private final String publicKeyBase64;

  public SecurityConfig(
      @Value("${goutapp.oauth.private-key}") String privateKeyBase64,
      @Value("${goutapp.oauth.public-key}") String publicKeyBase64) {
    this.privateKeyBase64 = privateKeyBase64;
    this.publicKeyBase64 = publicKeyBase64;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
        .authorizeHttpRequests(this::configureRequestAuthorization)
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .oauth2ResourceServer(this::configureJwtResourceServer)
        .sessionManagement(this::configureStatelessSession)
        .build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(ROLES_CLAIM);
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    var jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

    return jwtConverter;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {

    var daoProvider = new DaoAuthenticationProvider(passwordEncoder);
    daoProvider.setUserDetailsService(userDetailsService);

    return new ProviderManager(daoProvider);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Bean
  public JwtEncoder jwtEncoder(RSAKeyProperties rsaInstance) {
    var jwk =
        new RSAKey.Builder(rsaInstance.publicKey()).privateKey(rsaInstance.privateKey()).build();
    var jwkSource = new ImmutableJWKSet<SecurityContext>(new JWKSet(jwk));

    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAKeyProperties rsaInstance) {
    return NimbusJwtDecoder.withPublicKey(rsaInstance.publicKey()).build();
  }

  @Bean
  public RSAKeyProperties rsaInstance()
      throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {

    var privateKeyPkcs8Bytes = Base64.getDecoder().decode(privateKeyBase64);
    var publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);

    var privateKeyContent = new String(privateKeyPkcs8Bytes);
    var publicKeyContent = new String(publicKeyBytes);

    privateKeyContent =
        privateKeyContent
            .replaceAll("\\n", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "");
    publicKeyContent =
        publicKeyContent
            .replaceAll("\\n", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "");

    var keyFactory = KeyFactory.getInstance("RSA");

    var keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
    var privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpecPKCS8);

    var keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
    var publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);

    return new RSAKeyProperties(publicKey, privateKey);
  }

  /**
   * NOTE: Alternative solution, we can set by `@PreAuthorize("hasRole('ROLE_XXX')")` on
   * the @GetMapping, @XMapping or `@PreAuthorize("hasRole('ROLE_XXX') and hasRole('ROLE_YYY')")` if
   * many roles to set
   */
  private void configureRequestAuthorization(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          authorize) {

    authorize
        // Actuator
        .requestMatchers("/actuator/health")
        .permitAll()
        .requestMatchers("/actuator/metrics")
        .permitAll()

        // Auth
        .requestMatchers("/api/v1/auths/login")
        .permitAll()
        .requestMatchers("/api/v1/auths/refresh")
        .permitAll()

        // Tour Companies
        .requestMatchers(
            HttpMethod.GET, "/api/v1/tour-companies", "/api/v1/tour-companies/{id:\\d+}")
        .hasRole(UserRoleName.ADMIN.name())
        .requestMatchers(HttpMethod.POST, "/api/v1/tour-companies")
        .permitAll()
        .requestMatchers(
            HttpMethod.PATCH,
            "/api/v1/tour-companies/{id:\\d+}",
            "/api/v1/tour-companies/{id:\\d+}/approve")
        .hasRole(UserRoleName.ADMIN.name())
        .requestMatchers(HttpMethod.GET, "/api/v1/tour-companies/me")
        .hasRole(UserRoleName.COMPANY.name())
        .requestMatchers(HttpMethod.DELETE, "/api/v1/tour-companies/{id:\\d+}")
        .hasRole(UserRoleName.ADMIN.name())

        // Tours
        .requestMatchers(HttpMethod.GET, "/api/v1/tours", "/api/v1/tours/{id:\\d+}")
        .permitAll()
        .requestMatchers(HttpMethod.DELETE, "/api/v1/tours/{id:\\d+}")
        .hasRole(UserRoleName.ADMIN.name())
        .requestMatchers("/api/v1/tours")
        .hasRole(UserRoleName.COMPANY.name())

        // Users
        .requestMatchers(HttpMethod.POST, "/api/v1/users")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/users/me")
        .hasAnyRole(UserRoleName.ADMIN.name(), UserRoleName.CONSUMER.name())
        .requestMatchers("/api/v1/users/**")
        .hasRole(UserRoleName.ADMIN.name())

        // Payments
        .requestMatchers("/api/v1/payments/**")
        .hasRole(UserRoleName.CONSUMER.name())

        // Wallets
        .requestMatchers(HttpMethod.GET, "/api/v1/wallets/me")
        .hasRole(UserRoleName.CONSUMER.name())
        .requestMatchers(HttpMethod.POST, "/api/v1/wallets/top-up")
        .hasRole(UserRoleName.CONSUMER.name())

        // Self Managed Users
        .requestMatchers("/api/v1/me/**")
        .hasRole(UserRoleName.CONSUMER.name())

        // Administration purposes
        .requestMatchers("/api/v1/admins/**")
        .hasRole(UserRoleName.ADMIN.name())

        // The rest endpoints
        .anyRequest()
        .authenticated();
  }

  private OAuth2ResourceServerConfigurer<HttpSecurity> configureJwtResourceServer(
      OAuth2ResourceServerConfigurer<HttpSecurity> resourceServerConfig) {

    return resourceServerConfig.jwt(this::configureJwt);
  }

  private OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer configureJwt(
      OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer jwtConfig) {

    return jwtConfig.jwtAuthenticationConverter(jwtAuthenticationConverter());
  }

  private SessionManagementConfigurer<HttpSecurity> configureStatelessSession(
      SessionManagementConfigurer<HttpSecurity> session) {

    return session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }
}
