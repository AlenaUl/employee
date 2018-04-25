@file:Suppress("TooManyFunctions")

package de.hska.employee.service

import de.hska.employee.entity.SkillsType.JAVA
import de.hska.employee.entity.SkillsType.APACHE_CASSANDRA
import de.hska.employee.entity.Employee
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Random
import java.util.UUID.randomUUID
import java.time.LocalDate
import javax.validation.Valid
import kotlin.math.abs
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.validation.annotation.Validated
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono


/**
 * Anwendungslogik für Mitarbeiter.
 */
@Component
@Validated
class EmployeeService {
    /**
     * Einen Mitarbeiter anhand seiner ID suchen
     * @param id Die Id des gesuchten Mitarbeiter
     * @return Der gefundene Mitarbeiter oder ein leeres Mono-Objekt
     */
    fun findById(id: String) = if (id[0] == 'f' || id[0] == 'F') {
                Mono.empty()
            } else {
                createEmployee(id).toMono()
            }

    private
    fun findByEmail(email: String): Mono<Employee> {
        var id = randomUUID().toString()
        if (id[0] == 'f') {
            // damit findById nicht empty() liefert (s.u.)
            id = id.replaceFirst("f", "1")
        }

        return findById(id).flatMap {
            it.copy(email = email).toMono()
        }
    }

    /**
     * Mitarbeiter anhand von Suchkriterien suchen
     * @param queryParams Die Suchkriterien
     * @return Die gefundenen Mitarbeiter oder ein leeres Flux-Objekt
     */
    @Suppress("ReturnCount")
    fun find(queryParams: MultiValueMap<String, String>): Flux<Employee> {
        if (queryParams.isEmpty()) {
            return findAll()
        }

        for ((key, value) in queryParams) {
            if (value.size != 1) {
                return Flux.empty()
            }

            val paramValue = value[0]
            when (key) {
                "email" -> return findByEmail(paramValue).flux()
                "lastname" -> return findByLastname(paramValue)
            }
        }

        return Flux.empty()
    }

    /**
     * Alle Mitarbeiter als Flux ermitteln, wie sie später auch von der DB kommen.
     * @return Alle Mitarbeiter
     */
    fun findAll(): Flux<Employee> {
        val employees = ArrayList<Employee>(MAX_EMPLOYEES)
        repeat(MAX_EMPLOYEES) {
            var id = randomUUID().toString()
            if (id[0] == 'f') {
                id = id.replaceFirst("f", "1")
            }
            val employee = createEmployee(id)
            employees.add(employee)
        }
        return employees.toFlux()
    }

    @Suppress("ReturnCount")
    private
    fun findByLastname(lastname: String): Flux<Employee> {
        if (lastname.isBlank()) {
            return findAll()
        }

        if (lastname[0] == 'Z') {
            return Flux.empty()
        }

        val anzahl = lastname.length
        val employees = ArrayList<Employee>(anzahl)
        repeat(anzahl) {
            var id = randomUUID().toString()
            if (id[0] == 'f') {
                id = id.replaceFirst("f", "1")
            }
            val employee = createEmployee(id, lastname)
            employees.add(employee)
        }
        return employees.toFlux()
    }

    /**
     * Einen neuen Mitarbeiter anlegen.
     * @param employee Das Objekt des neu anzulegenden Mitarbeiter.
     * @return Der neu angelegte Mitarbeiter mit generierter ID
     */
    fun create(@Valid employee: Employee): Mono<Employee> {
        val newEmployee = employee.copy(id = randomUUID().toString())
        LOGGER.trace("Neuer Mitarbeiter: {}", newEmployee)
        return newEmployee.toMono()
    }

    /**
     * Einen vorhandenen Mitarbeiter aktualisieren.
     * @param employee Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Mitarbeiter
     * @return Der aktualisierte Mitarbeiter oder ein leeres Mono-Objekt, falls
     * es keinen Mitarbeiter mit der angegebenen ID gibt
     */
    fun update(@Valid employee: Employee, id: String) =
            findById(id)
                    .flatMap {
                        val employeeWithId = employee.copy(id = id)
                        LOGGER.trace("Aktualisierter Mitarbeiter: {}", employeeWithId)
                        employeeWithId.toMono()
                    }

    /**
     * Einen vorhandenen Mitarbeiter löschen.
     * @param employeeId Die ID des zu löschenden Mitarbeiter.
     */
    fun deleteById(employeeId: String) = findById(employeeId)

    /**
     * Einen vorhandenen Mitarbeiter löschen.
     * @param email Die Email des zu löschenden Mitarbeiter.
     */
    fun deleteByEmail(email: String): Mono<Employee> = findByEmail(email)

    private
    fun createEmployee(id: String): Employee {
        val countLastnames = LASTNAMES.size
        var lastnameIdx = RANDOM.nextInt() % countLastnames
        if (lastnameIdx < 0) {
            lastnameIdx += countLastnames
        }
        val lastname = LASTNAMES[lastnameIdx]
        return createEmployee(id, lastname)
    }

    private
    fun createEmployee(id: String, lastname: String): Employee {
        val birthday = LocalDate.now().minusYears(getYear())

        return Employee(
                id = id,
                lastname = lastname,
                email = "$lastname@firma.de",
                birthday = birthday,
                skills = listOf(JAVA, APACHE_CASSANDRA))
    }

    @Suppress("MagicNumber")
    private
    fun getYear() = (abs(RANDOM.nextLong()) % 60) + 1

    private
    companion object {
        const val MAX_EMPLOYEES = 8
        val LASTNAMES = listOf("Ulrich", "Ogbe", "Müller", "Schmidt", "Nochjemand")
        val RANDOM by lazy {
            val seed = LocalDateTime.now()
                    .toEpochSecond(ZoneOffset.ofHours(0))
            Random(seed)
        }
        val LOGGER = getLogger(EmployeeService::class.java)
    }
}
