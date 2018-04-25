package de.hska.employee.config

import org.springframework.boot.Banner
import org.springframework.boot.SpringBootVersion
import org.springframework.core.SpringVersion
import org.springframework.security.core.SpringSecurityCoreVersion

object Settings {
    /**
     * Konstante für das Spring-Profile "dev".
     */
    const val DEV = "dev"
    private
    val VERSION = "1.0"

    /**
     * Banner für den Start des Microservice in der Konsole.
     */
    val BANNER = Banner { _, _, out ->
        out.println("""

            |     _____ _____  _    _ _____  _____  ______   __ _____
            |    / ____|  __ \| |  | |  __ \|  __ \|  ____| /_ | ____|
            |   | |  __| |__) | |  | | |__) | |__) | |__     | | |__
            |   | | |_ |  _  /| |  | |  ___/|  ___/|  __|    | |___ \
            |   | |__| | | \ \| |__| | |    | |    | |____   | |___) |
            |    \_____|_|  \_\\____/|_|    |_|    |______|  |_|____/
            |
            |
            |
            |(C) Gruppe 15, SWA, SS18, Hochschule Karlsruhe
            |Version          $VERSION
            |Spring Boot      ${SpringBootVersion.getVersion()}
            |Spring Security  ${SpringSecurityCoreVersion.getVersion()}
            |Spring Framework ${SpringVersion.getVersion()}
            |JDK              ${System.getProperty("java.version")}
            |Betriebssystem   ${System.getProperty("os.name")}
            |""".trimMargin("|"))
    }

    private
    val parentPkgName by lazy {
        val pkgName = Settings::class.java.`package`.name
        pkgName.substring(0, pkgName.lastIndexOf('.'))
    }
    private
    val appName = parentPkgName.substring(parentPkgName.lastIndexOf('.') + 1)

    /**
     * Properties, die berücksichtigt werden, wenn der Microservice in der
     * Konsole gestartet wird.
     */
    val PROPS = mapOf(
            "spring.application.name" to appName,
            "spring.application.version" to VERSION,
            "spring.devtools.livereload.enabled" to false,
            "spring.devtools.restart.trigger-file=" to "/restart.txt",
            "spring.profiles.default" to "prod",
            // Functional bean definition Kotlin DSL
            //"context.initializer.classes" to "$parentPkgName.BeansInitializer"

            "spring.jackson.serialization.indent_output" to true,
            "spring.jackson.default-property-inclusion" to "non_null",

            // -Dreactor.trace.operatorStacktrace=true
            "spring.reactor.stacktrace-mode.enabled" to true,

            "spring.security.user.password" to "p",

            //"server.compression.enabled" to true,
            //"server.compression.mime-types" to "application/json",

            "server.ssl.enabled" to true,
            "server.ssl.key-alias" to "zimmermann",
            "server.ssl.key-password" to "zimmermann",
            "server.ssl.key-store" to "classpath:keystore.p12",
            "server.ssl.key-store-password" to "zimmermann",
            "server.ssl.key-storeType" to "PKCS12",
            "server.ssl.protocol" to "TLSv1.2",
            "server.ssl.trust-store" to "classpath:truststore.p12",
            "server.ssl.trust-store-password" to "zimmermann",
            "server.ssl.trust-storeType" to "PKCS12",
            "server.http2.enabled" to true,

            "management.endpoints.web.exposure.include" to "*",
            "management.endpoint.health.enabled" to true,
            //"management.endpoint.health.show-details" to true,
            "management.endpoint.mappings.enabled" to true,
            "management.endpoint.shutdown.enabled" to true,
            "management.endpoint.restart.enabled" to true,

            "spring.cloud.service-registry.auto-registration.enabled" to false)

    //"server.error.whitelabel.enabled" to false,
}
