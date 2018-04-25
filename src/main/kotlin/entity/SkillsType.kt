package de.hska.employee.entity

import com.fasterxml.jackson.annotation.JsonValue


/**
 * Enum für Interessen. Dazu können auf der Clientseite z.B. Checkboxen
 * realisiert werden.
 * @property value Der interne Wert
 */
enum class SkillsType(val value: String) {
    /**
     * _Kotlin_ mit dem internen Wert `K` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    KOTLIN("K"),
    /**
     * _Java_ mit dem internen Wert `J` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    JAVA("J"),
    /**
     * _Apache_Cassandra_ mit dem internen Wert `A` für z.B. das Mapping in einem
     * JSON-Datensatz oder das Abspeichern in einer DB.
     */
    APACHE_CASSANDRA("A");

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben. Dieser Wert
     * wird durch Jackson in einem JSON-Datensatz verwendet.
     * [https://github.com/FasterXML/jackson-databind/wiki]
     * @return Interner Wert
     */
    @JsonValue
    override fun toString() = value

    companion object {
        private
        val NAME_CACHE = HashMap<String, SkillsType>().apply {
            enumValues<SkillsType>().forEach {
                put(it.value, it)
                put(it.value.toLowerCase(), it)
                put(it.name, it)
                put(it.name.toLowerCase(), it)
            }
        }

        /**
         * Konvertierung eines Strings in einen Enum-Wert
         * @param value Der String, zu dem ein passender Enum-Wert ermittelt
         * werden soll.
         * Keine Unterscheidung zwischen Gross- und Kleinschreibung.
         * @return Passender Enum-Wert oder null
         */
        fun build(value: String?) = NAME_CACHE[value]
    }
}
