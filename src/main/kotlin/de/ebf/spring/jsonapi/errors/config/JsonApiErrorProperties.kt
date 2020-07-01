package de.ebf.spring.jsonapi.errors.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author : Vladimir Spasic
 * @since : 29.04.20, Wed
 **/
@ConstructorBinding
@ConfigurationProperties(prefix = "json-api.errors")
data class JsonApiErrorProperties(
    /**
     * Should the JSON API error return the stack trace of caught exception
     */
    val includeStackTrace: Boolean = false,
    /**
     * Should the JSON API Error messages be cached, defaults to {@literal true}
     */
    val cache: Boolean = true,
    /**
     * Location of the default message bundle used to provide localization for exceptions
     */
    val defaultMessageBundle: String = "de/ebf/spring/jsonapi/errors/messages",
    /**
     * Default locale used by the Message Source to translate error messages
     */
    val defaultLocale: Locale = Locale.getDefault(),
    /**
     * Default charset used by the Message Source to translate error messages, defaults to {@literal UTF-8}
     */
    val defaultEncoding: String = StandardCharsets.UTF_8.name(),
    /**
     * Additional locations of message bundles used to provide localization for exceptions
     */
    val messageBundles: Set<String> = setOf()
)