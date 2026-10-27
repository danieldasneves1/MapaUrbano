package v1.mapaurbano.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import v1.mapaurbano.entity.Usuario;

import javax.crypto.SecretKey;
import java.security.Security;
import java.util.Date;

@Component
public class TokenProvider {

    @Value("${jwt.expiration}")
    private long tempo;

    @Value("${jwt.key}")
    private String chave;

    public String gerarToken(Usuario usuario){
        return buildToken(usuario.getUsername());
    }

    private String buildToken(String username){
        Date now = new Date();
        Date exp = new Date(now.getTime() + tempo);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getAssinaturaKey())
                .compact();
    }

    private SecretKey getAssinaturaKey(){
        return Keys.hmacShaKeyFor(chave.getBytes());
    }

    public boolean isTokenValido(String token){
        try{
            getClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    public String getUsername (String token){
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(getAssinaturaKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
