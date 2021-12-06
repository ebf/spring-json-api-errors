package de.ebf.spring.jsonapi.errors.exceptions

import de.ebf.spring.jsonapi.errors.messages.Resolvable

/**
 * Convenient exception class that can be used to construct error message responses using [Resolvable].
 *
 * You can further decorate this exception with a [org.springframework.web.bind.annotation.ResponseStatus]
 * annotation in order to configure HTTP Status code that should be assigned to an HTTP response.
 *
 * @author <a href="mailto:vladimir.spasic@ebf.com">Vladimir Spasic</a>
 * @since 17.10.19, Thu
 **/
open class JsonApiResolvableException(
    override val code: String,
    message: String,
    cause: Throwable?
): RuntimeException(message, cause), Resolvable {

    constructor(code: String, message: String): this(code, message, null)

    override val arguments: Array<Any>
        get() = emptyArray()

    override val source: Map<String, Any>
        get() = emptyMap()

    override val defaultMessage: String?
        get() = message
}