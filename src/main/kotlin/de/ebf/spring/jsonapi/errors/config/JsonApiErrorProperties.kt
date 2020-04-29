package de.ebf.spring.jsonapi.errors.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.beans.ConstructorProperties

/**
 * @author : Vladimir Spasic
 * @since : 29.04.20, Wed
 **/
@ConstructorBinding
@ConfigurationProperties(prefix = "json-api.errors")
data class JsonApiErrorProperties(
    val includeStackTrace: Boolean = false,
    val messageBundles: Set<String> = setOf()
)