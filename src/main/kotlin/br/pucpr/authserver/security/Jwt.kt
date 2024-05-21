package br.pucpr.authserver.security

import br.pucpr.authserver.users.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.jackson.io.JacksonDeserializer
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

@Component
class Jwt(val properties: SecurityProperties) {
    fun createToken(user: User): String =
        UserToken(user).let {
            Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(properties.secret.toByteArray()))
                .serializeToJsonWith(JacksonSerializer())
                .setIssuedAt(utcNow().toDate())
                .setExpiration(utcNow().plusHours(properties.expireHours).toDate())
                .setIssuer(properties.issuer)
                .setSubject(user.id.toString())
                .claim(USER_FIELD, it)
                .compact()
        }

    fun extract(req: HttpServletRequest): Authentication? {
        try {
            val header = req.getHeader(AUTHORIZATION)
            if (header == null || !header.startsWith(PREFIX)) return null
            val token = header.replace(PREFIX, "").trim()

            val claims: Claims = Jwts
                .parserBuilder().setSigningKey(properties.secret.toByteArray())
                .deserializeJsonWith(
                    JacksonDeserializer(
                        mapOf(USER_FIELD to UserToken::class.java)
                    )
                ).build()
                .parseClaimsJws(token)
                .body

            if(claims.issuer != properties.issuer) return null

            return claims.get(USER_FIELD, UserToken::class.java).toAuthentication()
        } catch (e: Throwable) {
            log.debug("Token rejected", e)
            return null
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(Jwt::class.java)

        const val PREFIX = "Bearer"
        const val USER_FIELD = "user"

        private fun utcNow() = ZonedDateTime.now(ZoneOffset.UTC)
        private fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())

        private fun UserToken.toAuthentication() : Authentication {
            val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }

            return UsernamePasswordAuthenticationToken.authenticated(this, id, authorities)
        }
    }
}
