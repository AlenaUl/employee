package de.hska.employee.rest

import de.hska.employee.rest.util.itemLinks
import de.hska.employee.service.EmployeeService
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono


/**
 * Eine Handler-Function wird von der Router-Function
 * [de.hska.employeeApplication.router] aufgerufen, nimmt einen Request entgegen
 * und erstellt den Response.
 * @constructor Einen EmployeeStreamHandler mit einem injizierten [EmployeeService]
 *      erzeugen.
 */
@Component
class EmployeeStreamHandler(private val service: EmployeeService) {
    /**
     * Alle Mitarbeiter als Event-Stream zurückliefern.
     * @param request Das eingehende Request-Objekt.
     * @return Response mit dem MIME-Typ `text/event-stream`.
     */
    fun findAll(request: ServerRequest): Mono<ServerResponse> {
        val employee = service.findAll().map {
            val listUri = request.uri()
            it.links = listUri.itemLinks(it.id!!)
            it
        }

        return ok().contentType(TEXT_EVENT_STREAM).body(employee)
    }
}
