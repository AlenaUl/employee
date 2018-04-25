package de.hska.employee.rest

import com.fasterxml.jackson.core.JsonParseException
import de.hska.employee.Application.Companion.ID_PATH_VAR
import de.hska.employee.entity.Employee
import de.hska.employee.rest.util.EmployeePatcher
import de.hska.employee.rest.util.InvalidSkillException
import de.hska.employee.rest.util.PatchOperation
import de.hska.employee.rest.util.itemLinks
import de.hska.employee.rest.util.singleLinks
import de.hska.employee.service.EmployeeService
import java.net.URI
import javax.validation.ConstraintViolationException
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.bodyToFlux
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse
        .badRequest
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.core.publisher.onErrorResume
import reactor.core.publisher.toMono


/**
 * Eine Handler-Function wird von der Router-Function
 * [de.hska.employee.Application.router] aufgerufen, nimmt einen Request entgegen
 * und erstellt den Response.
 * @constructor Einen EmployeeHandler mit einem injizierten [EmployeeService]
 *      erzeugen.
 */
@Component
@Suppress("TooManyFunctions")
class EmployeeHandler(private val service: EmployeeService) {
    /**
     * Suche anhand der MA-ID
     * @param request Der eingehende Request
     * @return Ein Mono-Objekt mit dem Statuscode 200 und dem gefundenen
     *      Kunden einschließlich HATEOAS-Links, oder aber Statuscode 204.
     */
    fun findById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(ID_PATH_VAR)
        val employee = service.findById(id).map {
            it._links = request.uri().singleLinks()
            it
        }

        return employee.flatMap { ok().body(it.toMono()) }
                .switchIfEmpty(notFound().build())
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter. Es wird
     * `Mono<List<Employee>>` statt `Flux<Employee>` zurückgeliefert, damit
     * auch der Statuscode 204 möglich ist.
     * @param request Der eingehende Request mit den Query-Parametern.
     * @return Ein Mono-Objekt mit dem Statuscode 200 und einer Liste mit den
     *      gefundenen MA einschließlich HATEOAS-Links, oder aber
     *      Statuscode 204.
     */
    fun find(request: ServerRequest): Mono<ServerResponse> {
        val queryParams = request.queryParams()

        // https://stackoverflow.com/questions/45903813/...
        //     ...webflux-functional-how-to-detect-an-empty-flux-and-return-404
        val employees = service.find(queryParams).map {
            if (it.id != null) {
                it.links = request.uri().itemLinks(it.id)
            }
            it
        }
                .collectList()

        return employees.flatMap {
            if (it.isEmpty()) notFound().build() else ok().body(it.toMono())
        }
    }

    /**
     * Einen neuen MA-Datensatz anlegen.
     * @param request Der eingehende Request mit dem MA-Datensatz im Body.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder
     *      Statuscode 400 falls Constraints verletzt sind oder der
     *      JSON-Datensatz syntaktisch nicht korrekt ist.
     */
    fun create(request: ServerRequest): Mono<ServerResponse> =
            request.bodyToMono<Employee>()
                    .flatMap { service.create(it) }
                    .flatMap {
                        LOGGER.trace("Mitarbeiter abgespeichert: {}", it)
                        val location = URI("${request.uri()}${it.id}")
                        created(location).build()
                    }
                    .onErrorResume(ConstraintViolationException::class) {
                        var msg = it.message
                        if (msg == null) {
                            badRequest().build()
                        } else {
                            // Funktion "create" mit Parameter "employee"
                            msg = msg.replace("create.employee.", "")
                            badRequest().body(msg.toMono())
                        }
                    }
                    .onErrorResume(DecodingException::class) {
                        handleDecodingException(it)
                    }

    /**
     * Einen vorhandenen MA-Datensatz überschreiben.
     * @param request Der eingehende Request mit dem neuen MA-Datensatz im
     *      Body.
     * @return Response mit Statuscode 204 oder Statuscode 400, falls
     *      Constraints verletzt sind oder der JSON-Datensatz syntaktisch nicht
     *      korrekt ist.
     */
    fun update(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(ID_PATH_VAR)
        return request.bodyToMono<Employee>()
                .flatMap { service.update(it, id) }
                .flatMap { noContent().build() }
                .switchIfEmpty(notFound().build())
                .onErrorResume(ConstraintViolationException::class) {
                    var msg = it.message
                    if (msg == null) {
                        badRequest().build()
                    } else {
                        // Funktion "update" mit Parameter "employee"
                        msg = msg.replace("update.employee.", "")
                        badRequest().body(msg.toMono())
                    }
                }
                .onErrorResume(DecodingException::class) {
                    handleDecodingException(it)
                }
    }

    /**
     * Einen vorhandenen MA-Datensatz durch PATCH aktualisieren.
     * @param request Der eingehende Request mit dem PATCH-Datensatz im Body.
     * @return Response mit Statuscode 204 oder Statuscode 400, falls
     *      Constraints verletzt sind oder der JSON-Datensatz syntaktisch nicht
     *      korrekt ist.
     */
    fun patch(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(ID_PATH_VAR)

        return request.bodyToFlux<PatchOperation>()
                // Die einzelnen Patch-Operationen als Liste in einem Mono
                .collectList()
                .flatMap { patchOps ->
                    service.findById(id)
                            .flatMap {
                                val employeePatched =
                                        EmployeePatcher.patch(it, patchOps)
                                LOGGER.trace("Mitarbeiter mit Patch-Ops: {}",
                                        employeePatched)
                                service.update(employeePatched, id)
                            }
                            .flatMap { noContent().build() }
                            .switchIfEmpty(notFound().build())
                }
                .onErrorResume(ConstraintViolationException::class) {
                    var msg = it.message
                    if (msg == null) {
                        badRequest().build()
                    } else {
                        msg = msg.replace("update.employee.", "")
                        badRequest().body(msg.toMono())
                    }
                }
                .onErrorResume(InvalidSkillException::class) {
                    val msg = it.message
                    if (msg == null) {
                        badRequest().build()
                    } else {
                        badRequest().body(msg.toMono())
                    }
                }
                .onErrorResume(DecodingException::class) {
                    handleDecodingException(it)
                }
    }

    /**
     * Einen vorhandenen MA anhand seiner ID löschen.
     * @param request Der eingehende Request mit der ID als Pfad-Parameter.
     * @return Response mit Statuscode 204.
     */
    fun deleteById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(ID_PATH_VAR)
        return service.deleteById(id).flatMap { noContent().build() }
    }

    /**
     * Einen vorhandenen MA anhand seiner Emailadresse löschen.
     * @param request Der eingehende Request mit der Emailadresse als
     *      Query-Parameter.
     * @return Response mit Statuscode 204.
     */
    fun deleteByEmail(request: ServerRequest): Mono<ServerResponse> {
        val email = request.queryParam("email")
        return if (email.isPresent) {
            return service.deleteByEmail(email.get())
                    .flatMap { noContent().build() }
        } else {
            notFound().build()
        }
    }

    private
    fun handleDecodingException(e: DecodingException): Mono<ServerResponse> {
        val exception = e.cause
        return if (exception is JsonParseException) {
            val msg = exception.message
            LOGGER.debug(msg)
            if (msg == null) {
                badRequest().build()
            } else {
                badRequest().body(msg.toMono())
            }
        } else {
            status(INTERNAL_SERVER_ERROR).build()
        }
    }

    private
    companion object {
        val LOGGER = getLogger(EmployeeHandler::class.java)
    }
}
