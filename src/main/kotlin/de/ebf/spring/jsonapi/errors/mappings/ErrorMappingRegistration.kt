package de.ebf.spring.jsonapi.errors.mappings

import org.springframework.http.HttpStatus
import java.util.function.Function

/**
 * Class that contains the configuration on how should an exception be
 * handled and converted to a [de.ebf.spring.jsonapi.errors.JsonApiErrors].
 *
 * @see de.ebf.spring.jsonapi.errors.resolvers.MappingExceptionResolver
 * @see de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer
 * @see ErrorMappingRegistry
 */
class ErrorMappingRegistration<T: Throwable> {

    var code: String? = null
        private set

    var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        private set

    var resolver: Function<T, List<Any>>? = null
        private set

    fun code(code: String): ErrorMappingRegistration<T> = apply { this.code = code }

    fun status(status: HttpStatus): ErrorMappingRegistration<T> = apply { this.status = status }

    fun resolver(resolver: Function<T, List<Any>>): ErrorMappingRegistration<T> = apply { this.resolver = resolver }

}