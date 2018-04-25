
package de.hska.employee.config.dev

import de.hska.employee.config.Settings.DEV
import java.security.Principal
import org.slf4j.LoggerFactory.getLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.server.WebFilter

interface LogRequest {
    /**
     * Bean-Definition, um die Daten eines Requests und des zugehörigen Response
     * zu protokollieren.
     *
     * @return Ein WebFilter, der protokolliert und den nächsten WEbFilter
     * aufruft.
     */
    @Bean
    @Profile(DEV)
    fun loggingFilter() =
        WebFilter { exchange, chain ->
            with(exchange.request) {
                exchange.getPrincipal<Principal>().subscribe {
                    LOGGER.debug("Principal:         ${it.name}")
                }
                LOGGER.debug("""
                    |REQUEST >>>
                    |URI:               $uri
                    |HTTP-Methode:      $methodValue
                    |Context-Pfad:      ${path.contextPath().value()}
                    |Pfad:              ${path.pathWithinApplication().value()}
                    |Query-Parameter:   $queryParams
                    |Headers:           $headers
                    |<<<
                    |""".trimMargin("|"))
            }

            chain.filter(exchange)
        }

    private
    companion object {
        val LOGGER = getLogger(LogRequest::class.java)
    }
}
