package de.hska.employee.config

import java.util.Locale
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails
        .MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User

interface SecurityConfig {
    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu
     * konfigurieren.
     *
     * @param http Injiziertes Objekt von `ServerHttpSecurity` als
     *      Ausgangspunkt für die Konfiguration.
     * @return Objekt von `SecurityWebFilterChain`
     */
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity) = http.authorizeExchange()
                    .pathMatchers(POST, EMPLOYEE_PATH).permitAll()
                    .pathMatchers(GET, EMPLOYEE_PATH, EMPLOYEE_ID_PATH).hasRole(ADMIN)
                    .pathMatchers(PUT, EMPLOYEE_PATH).hasRole(ADMIN)
                    .pathMatchers(PATCH, EMPLOYEE_ID_PATH).hasRole(ADMIN)
                    .pathMatchers(DELETE, EMPLOYEE_ID_PATH).hasRole(ADMIN)

                    .pathMatchers(GET, ACTUATOR_PATH, "$ACTUATOR_PATH/*")
                    .hasRole(ENDPOINT_ADMIN)
                    .pathMatchers(POST, "$ACTUATOR_PATH/*")
                    .hasRole(ENDPOINT_ADMIN)

                    .anyExchange().authenticated()

                    .and()
                    .httpBasic()
                    .and()
                    .formLogin().disable()
                    .csrf().disable()
                    // FIXME Disable FrameOptions: Clickjacking
                    .build()

    /**
     * Bean, um Test-User anzulegen. Dazu gehören jeweils ein Benutzername, ein
     * Passwort und diverse Rollen.
     *
     * @return Ein Objekt, mit dem diese Test-User verwaltet werden, z.B. für
     * die künftige Suche.
     */
    @Bean
    @Suppress("DEPRECATION")
    fun userDetailsRepository(): MapReactiveUserDetailsService {
        val admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("p")
                .roles(ADMIN, EMPLOYEE, ENDPOINT_ADMIN)
                .build()
        val alpha = User.withDefaultPasswordEncoder()
                .username("alpha")
                .password("p")
                .roles(EMPLOYEE)
                .build()
        return MapReactiveUserDetailsService(admin, alpha)
    }

    private
    companion object {
        val ADMIN = "ADMIN"
        val EMPLOYEE = "EMPLOYEE"
        val ENDPOINT_ADMIN = "ENDPOINT_ADMIN"

        val EMPLOYEE_PATH = "/"
        val EMPLOYEE_ID_PATH = "/*"
        val ACTUATOR_PATH = "/actuator"

        @Suppress("unused")
        val REALM by lazy {
            // Name der REALM = Name des Parent-Package in Grossbuchstaben,
            // z.B. EMPLOYEE
            val pkg = SecurityConfig::class.java.`package`.name
            val parentPkg = pkg.substring(0, pkg.lastIndexOf('.'))
            parentPkg.substring(parentPkg.lastIndexOf('.') + 1)
                    .toUpperCase(Locale.getDefault())
        }
    }
}
