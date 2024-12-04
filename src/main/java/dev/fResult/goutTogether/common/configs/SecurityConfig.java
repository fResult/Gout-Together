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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

  private final ResourceLoader resourceLoader;

  public SecurityConfig(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/api/v1/auths/login")
                    .permitAll()
                    .requestMatchers("/api/v1/auths/refresh")
                    .permitAll()
                    .requestMatchers("/api/v1/auths/logout")
                    .hasAnyRole(
                        UserRoleName.ADMIN.name(),
                        UserRoleName.CONSUMER.name(),
                        UserRoleName.COMPANY.name())
                    .requestMatchers("/api/v1/admins/**")
                    .hasRole(UserRoleName.ADMIN.name())
                    .anyRequest()
                    .permitAll())
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

    var privateKeyPkcs8Resource = resourceLoader.getResource("classPath:private_key_pkcs8.pem");
    var publicKeyResource = resourceLoader.getResource("classPath:public_key.pem");

    var privateKeyContent = new String(privateKeyPkcs8Resource.getContentAsByteArray());
    var publicKeyContent = new String(publicKeyResource.getContentAsByteArray());

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
