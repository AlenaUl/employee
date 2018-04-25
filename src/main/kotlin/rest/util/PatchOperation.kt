package de.hska.employee.rest.util


/**
 * Hilfsklasse für _HTTP PATCH_ mit Datensätzen, wie z.B.
 * `{"op": "replace", "path": "/email", "value": "new.email@test.de"}`
 * @property op PATCH-Operation, z.B. _add_, _remove_, _replace_.
 * @property path Pfad zur adressierten Property, z.B. _/email_.
 * @property value Der neue Wert für die Property.
 */
data class PatchOperation(val op: String, val path: String, val value: String)
