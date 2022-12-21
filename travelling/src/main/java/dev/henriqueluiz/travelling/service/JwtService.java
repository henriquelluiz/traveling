package dev.henriqueluiz.travelling.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import dev.henriqueluiz.travelling.model.AppUser;
import dev.henriqueluiz.travelling.repository.UserRepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final UserRepo userRepo;

    public Map<String, String> getAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        
        List<String> scope = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
        
        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(25, ChronoUnit.MINUTES))
            .subject(authentication.getName())
            .claim("scope", scope)
            .build();

        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.DAYS))
            .subject(authentication.getName())
            .claim("scope", Collections.emptyList())
            .build();

        String accessToken = this.encoder
            .encode(JwtEncoderParameters.from(accessClaims)).getTokenValue();
        
        String refreshToken = this.encoder
            .encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();
            
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        return tokens;
    }

    public Map<String, String> refreshToken(String token) {    
        Instant now = Instant.now();
        AppUser user = tokenUtil(token);
        List<String> scope = user.getAuthorities().stream()
            .map(role -> role.getName())
            .toList();

        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(25, ChronoUnit.MINUTES))
            .subject(user.getEmail())
            .claim("scope", scope)
            .build();

        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.DAYS))
            .subject(user.getEmail())
            .claim("scope", Collections.emptyList())
            .build();

        String accessToken = this.encoder
            .encode(JwtEncoderParameters.from(accessClaims)).getTokenValue();
        
        String refreshToken = this.encoder
            .encode(JwtEncoderParameters.from(refreshClaims)).getTokenValue();
        
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);    
        return tokens;
    }

    private AppUser tokenUtil(String token) {
        // LOG
        Jwt jwt = this.decoder.decode(token);
        String email = Objects.requireNonNull(jwt.getSubject());
        Instant expiresAt = Objects.requireNonNull(jwt.getExpiresAt());    
        
        if((expiresAt.isBefore(Instant.now()))) {
            throw new RuntimeException("Token is not valid");
        }

        return userRepo.findByEmail(email)
            .orElseThrow();
    }

    // TODO: Criar DTOs.
    // TODO: Tratamento de exceções
    // TODO: Fazer os testes.
    // TODO: Criar o recurso 'Travel'. 
}
