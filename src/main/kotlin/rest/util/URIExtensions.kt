package de.hska.employee.rest.util

import de.hska.employee.rest.util.Constants.HREF
import de.hska.employee.rest.util.Constants.REL
import de.hska.employee.rest.util.Constants.SELF
import java.net.URI

/**
 * Extension-Function für URI, um zu einer URI, die HATEOAS-Links zu einem
 * Datensatz zu erstellen.
 */
fun URI.singleLinks(): Map<String, Map<String, String>> {
    val list = "list"
    val add = "add"
    val update = "update"
    val remove = "remove"

    val selfUriStr: String = this.toString()
    val baseUri by lazy {
        val indexLastSlash = selfUriStr.lastIndexOf('/')
        selfUriStr.substring(0, indexLastSlash)
    }

    return mapOf(
            SELF to mapOf(HREF to selfUriStr),
            list to mapOf(HREF to baseUri),
            add to mapOf(HREF to baseUri),
            update to mapOf(HREF to selfUriStr),
            remove to mapOf(HREF to selfUriStr))
}


/**
 * Extension-Function für URI, um zu einer Basis-URI und einer UUID, die HATEOAS-Links
 * zu einem Datensatz innerhalb eines JSON-Arrays zu erstellen.
 */
fun URI.itemLinks(id: String): List<Map<String, String>> {
    val scheme = this.scheme
    val host = this.host
    val port = this.port
    val path = this.path
    val uri = URI(scheme, null, host, port, path, null, null)
    return listOf(
            mapOf(REL to SELF),
            mapOf(HREF to "$uri$id"))
}
