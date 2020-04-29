package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.context.support.MessageSourceAccessor

/**
 * Default implementation of the [ErrorMessageSource] that uses Spring's
 * [MessageSourceAccessor] and [MessageSource] to resolve exception messages.
 */
class DelegatingErrorMessageSource constructor(
    private var messageSourceAccessor: MessageSourceAccessor
): ErrorMessageSource {

    constructor(messageSource: MessageSource): this(MessageSourceAccessor(messageSource))

    override fun get(resolvable: Resolvable): JsonApiErrors.ErrorMessage {
        var title: String? = null

        val arguments = resolvable.arguments

        try {
            title = messageSourceAccessor.getMessage(DefaultMessageSourceResolvable(
                arrayOf("${resolvable.code}.title"), arguments, null
            ))
        } catch (e: NoSuchMessageException) {
            // ignore
        }

        val details = messageSourceAccessor.getMessage(DefaultMessageSourceResolvable(
            arrayOf("${resolvable.code}.message", resolvable.code), arguments, resolvable.defaultMessage
        ))

        return JsonApiErrors.ErrorMessage(
            title = title,
            detail = details,
            code = resolvable.code,
            source = resolvable.source
        )
    }

}