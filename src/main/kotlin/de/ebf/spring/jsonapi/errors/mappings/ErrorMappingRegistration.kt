package de.ebf.spring.jsonapi.errors.mappings

import org.springframework.http.HttpStatus

/**
 * Class that contains the configuration on how should an exception be
 * handled and converted to a [de.ebf.spring.jsonapi.errors.JsonApiErrors].
 *
 * @see de.ebf.spring.jsonapi.errors.resolvers.MappingExceptionResolver
 * @see de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer
 * @see ErrorMappingRegistry
 */
class ErrorMappingRegistration {

    var code: String? = null
    var status: HttpStatus? = HttpStatus.INTERNAL_SERVER_ERROR

    fun code(code: String): ErrorMappingRegistration = apply { this.code = code }

    fun status(status: HttpStatus): ErrorMappingRegistration = apply { this.status = status }

}