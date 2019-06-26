package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.core.Ordered

/**
 * Default implementation of the [ErrorMessageSource] that uses the
 * [MessageSourceAccessor] and [MessageSource] to resolve exception messages.
 */
class DefaultErrorMessageSource constructor(
    private var messageSourceAccessor: MessageSourceAccessor,
    private var order: Int = Ordered.LOWEST_PRECEDENCE
): ErrorMessageSource {

    constructor(messageSource: MessageSource): this(MessageSourceAccessor(messageSource))
    constructor(messageSource: MessageSource, order: Int): this(MessageSourceAccessor(messageSource), order)
    constructor(messageSourceAccessor: MessageSourceAccessor): this(messageSourceAccessor, Ordered.LOWEST_PRECEDENCE)

    override fun getOrder(): Int {
        return order
    }

    override fun get(resolvable: ErrorMessageResolvable): JsonApiErrors.ErrorMessage {
        var title: String? = null

        var arguments = arrayOf<Any>()
        resolvable.arguments?.forEach { arguments = arguments.plus(it) }

        try {
            title = messageSourceAccessor.getMessage(DefaultMessageSourceResolvable(
                arrayOf("${resolvable.code}.title"), arguments, resolvable.defaultMessage
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