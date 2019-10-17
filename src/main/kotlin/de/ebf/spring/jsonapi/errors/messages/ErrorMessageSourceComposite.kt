package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.core.Ordered

/**
 * Implementation of [ErrorMessageSource] that delegates the logic to underlying implementation.
 */
class ErrorMessageSourceComposite: ErrorMessageSource {

    private var sources = mutableListOf<ErrorMessageSource>()

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override fun get(resolvable: Resolvable): JsonApiErrors.ErrorMessage {
        var message: JsonApiErrors.ErrorMessage? = null
        var thrown: NoSuchMessageException? = null
        val iterator = sources.iterator()

        while (message == null && iterator.hasNext()) {
            try {
                message = iterator.next().get(resolvable)
            } catch (ex: NoSuchMessageException) {
                thrown = ex
            }
        }

        if (message != null) {
            return message
        }

        if (thrown == null) {
            thrown = NoSuchMessageException(resolvable.code, LocaleContextHolder.getLocale())
        }

        throw thrown
    }

    fun addErrorMessageSource(errorMessageSource: ErrorMessageSource) {
        sources.add(errorMessageSource)
    }
}