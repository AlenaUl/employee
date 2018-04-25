/*
 * Copyright (C) 2013 - 2016 Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.employee.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.hibernate.validator.constraints.UniqueElements
import java.net.URL
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern


/**
 * Unveränderliche Daten eines Mitarbeiters. In DDD ist Mitarbeiter ist ein _Aggregate Root_.
 * @property id ID eines Mitarbeiters als UUID [ID_PATTERN]].
 * @property lastname Nachname eines Mitarbeiters mit einem bestimmten Muster [LASTNAME_PATTERN].
 *      [MIN_KATEGORIE] und [MAX_KATEGORIE].
 * @property birthday Das Geburtsdatum eines Mitarbeiters.
 * @property skills Die Interessen eines Mitarbeiters.
 * @property email Email eines Mitarbeiters.
 * @property _links HATEOAS-Links, wenn genau 1 JSON-Objekt in einem Response
 *      zurückgeliefert wird. Die Links werden nicht in der DB gespeichert.
 * @property links HATEOAS-Links, wenn ein JSON-Array in einem Response
 *      zurückgeliefert wird. Die Links werden nicht in der DB gespeichert.
 */
@JsonPropertyOrder(
        "nachname", "birthday", "skills", "email" )
data class Employee(
    @get:Pattern(regexp = ID_PATTERN, message = "{employee.id.pattern}")
    @JsonIgnore
    val id: String?,

    @get:NotEmpty(message = "{employee.lastname.notEmpty}")
    @get:Pattern(
            regexp = LASTNAME_PATTERN,
            message = "{employee.lastname.pattern}")
    val lastname: String,

    @get:Past(message = "{employee.birthday.past}")
    // In einer "Data Class": keine Aufbereitung der Konstruktor-Argumente
    //@JsonFormat(shape = STRING)
    //@field:JsonDeserialize(using = DateDeserializer.class)
    val birthday: LocalDate?,

    @get:UniqueElements(message = "{employee.skills.uniqueElements}")
    val skills: List<SkillsType>?,

    @get:Email(message = "{employee.email.pattern}")
    val email: String
) {

    // wird spaeter nicht in der DB gespeichert
    @Suppress("PropertyName", "VariableNaming")
    var _links: Map<String, Map<String, String>>? = null

    var links: List<Map<String, String>>? = null

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Kunde-) Objekt die gleiche
     *      Emailadresse hat.
     */
    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Employee
        return email == other.email
    }

    /**
     * Hashwert aufgrund der Emailadresse.
     * @return Der Hashwert.
     */
    override fun hashCode() = email.hashCode()

    companion object {
        private
        const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster für eine UUID.
         */
        const val ID_PATTERN =
            "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-" +
                    "$HEX_PATTERN{4}-$HEX_PATTERN{12}"

        private
        const val LASTNAME_PREFIX = "o'|von|von der|von und zu|van"

        private
        const val NAME_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+"
        /**
         * Muster für einen Nachnamen
         */
        const val LASTNAME_PATTERN =
            "($LASTNAME_PREFIX)?$NAME_PATTERN(-$NAME_PATTERN)?"
    }
}
