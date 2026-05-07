package edu.tut.cms.common.utils;

import edu.tut.cms.common.context.CurrentUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据用户信息生成JWT令牌
     */
    public String generateToken(CurrentUser user) {
        return Jwts.builder()
                .claim("id", user.getId())
                .claim("uid", user.getUid())
                .claim("userName", user.getUserName())
                .claim("userRole", user.getUserRole())
                .claim("userStatus", user.getUserStatus())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析JWT令牌并返回用户信息
     */
    public CurrentUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return CurrentUser.builder()
                .id(claims.get("id", Long.class))
                .uid(claims.get("uid", String.class))
                .userName(claims.get("userName", String.class))
                .userRole(claims.get("userRole", Integer.class))
                .userStatus(claims.get("userStatus", Integer.class))
                .build();
    }
}
