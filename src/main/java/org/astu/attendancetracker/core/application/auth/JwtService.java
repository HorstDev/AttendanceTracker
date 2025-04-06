package org.astu.attendancetracker.core.application.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    //todo Переместить в application.yaml
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    // Извлекает логин пользователя из jwt-токена
    public String extractUserLogin(String token) {
        // Subject - здесь обычно кладут Id или логин пользователя
        // В нашем случае здесь лежит логин
        return extractClaim(token, Claims::getSubject);
    }

    // Извлечение конкретного Claim
    // Function - берет на вход Claims и возвращает T
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        // Выполняется функция (преобразование из claims в T)
        return claimsResolver.apply(claims);
    }

    // Генерация JWT без утверждений
    public String generateToken(CustomUserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Генерация JWT с утверждениями
    public String generateToken(
            Map<String, Object> extraClaims,
            CustomUserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // 24 часа
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Проверка токена на валидность
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        final String username = extractUserLogin(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Проверка, истек ли токен
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Извлечение утверждения об истечении токена (дата истечения)
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Возвращает Claims (утверждения: имя пользователя, роли и т.п.)
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
