
package de.hska.employee

import de.hska.employee.config.Settings.BANNER
import de.hska.employee.config.Settings.PROPS
import de.hska.employee.entity.Employee
import de.hska.employee.rest.EmployeeHandler
import de.hska.employee.rest.EmployeeStreamHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.WebApplicationType.REACTIVE
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.web.reactive.function.server.router
import java.nio.file.Files.find

@SpringBootApplication
class Application {
    /**
     * Bean-Function, um das Routing mit _Spring WebFlux_ funktional zu
     * konfigurieren.
     *
     * @param handler Objekt der Handler-Klasse [EmployeeHandler] zur Behandlung
     *      von Requests.
     * @param streamHandler Objekt der Handler-Klasse [EmployeeStreamHandler]
     *      zur Behandlung von Requests mit Streaming.
     * @return Die konfigurierte Router-Function.
     */
    @Bean
    fun router(handler: EmployeeHandler, streamHandler: EmployeeStreamHandler) = router {

        "/".nest {
            accept(APPLICATION_JSON).nest {
              GET("/", handler::find)
                GET("/$ID_PATH_PATTERN", handler::findById)
                POST("/", handler::create)
                PUT("/$ID_PATH_PATTERN", handler::update)
                PATCH("/$ID_PATH_PATTERN", handler::patch)
                DELETE("/$ID_PATH_PATTERN", handler::deleteById)
                DELETE("/", handler::deleteByEmail)
            }

            (accept(TEXT_EVENT_STREAM) and "/stream").nest {
                GET("/", streamHandler::findAll)
            }
        }
    }

    companion object {
        /**
         * Name der Pfadvariablen für IDs.
         */
        val ID_PATH_VAR = "id"

        private
        val ID_PATH_PATTERN = "{$ID_PATH_VAR:${Employee.ID_PATTERN}}"
    }
}


/**
 * Hauptprogramm, um den Microservice zu starten.
 *
 * @param args Evtl. zusätzliche Argumente für den Start des Microservice
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<Application>(*args) {
        webApplicationType = REACTIVE
        setBanner(BANNER)
        setDefaultProperties(PROPS)
        addListeners(ApplicationPidFileWriter())
    }
}
