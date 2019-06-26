package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import org.springframework.core.Ordered

/**
 * Implementation of the [ExceptionResolver] interface that uses the
 * [de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistration] configured
 * in the [ErrorMappingRegistry] using the [de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer]
 *
 * @see de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer
 * @see de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistration
 */
class MappingExceptionResolver constructor(
    private val registry: ErrorMappingRegistry
): ExceptionResolver {

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 4
    }

    override fun resolve(throwable: Throwable): ResolvedException? {
        val mapping = registry.get(throwable.javaClass) ?: return null

        return ResolvedException(status = mapping.status!!, errors = listOf(
            ErrorMessageResolvable(code = mapping.code!!)
        ))
    }

}