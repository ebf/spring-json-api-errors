package de.ebf.spring.jsonapi.errors.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

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
     * Location of the default message bundle used to provide localization for exceptions
     */
    val defaultMessageBundle: String = "de/ebf/spring/jsonapi/errors/messages",
    /**
     * Additional locations of message bundles used to provide localization for exceptions
     */
    val messageBundles: Set<String> = setOf()
)