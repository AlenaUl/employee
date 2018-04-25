
package de.hska.employee.config

import de.hska.employee.config.dev.LogRequest
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive
        .EnableWebFluxSecurity

@Configuration
@EnableWebFluxSecurity
class AppConfig : LogRequest, SecurityConfig
