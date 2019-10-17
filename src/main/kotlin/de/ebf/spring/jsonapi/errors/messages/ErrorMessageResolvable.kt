package de.ebf.spring.jsonapi.errors.messages

import org.springframework.util.ObjectUtils

/**
 * Class used by the [ErrorMessageSource] to resolve the title and details error
 * messages for the given exception code and arguments.
 *
 * @see ErrorMessageSource
 */
data class ErrorMessageResolvable (
    override val code: String,
    override val arguments: Array<Any> = arrayOf(),
    override val source: Map<String, Any> = emptyMap(),
    override val defaultMessage: String? = null
): Resolvable {

    constructor(code: String): this(code, arrayOf(), emptyMap(), null)
    constructor(code: String, arguments: Array<Any>): this(code, arguments, emptyMap(), null)
    constructor(code: String, arguments: Array<Any>, source: Map<String, Any>): this(code, arguments, source, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ErrorMessageResolvable

        return ObjectUtils.nullSafeEquals(code, other.code) &&
                ObjectUtils.nullSafeEquals(source, other.source) &&
                ObjectUtils.nullSafeEquals(arguments, other.arguments) &&
                ObjectUtils.nullSafeEquals(defaultMessage, other.defaultMessage)
    }

    override fun hashCode(): Int {
        var result = ObjectUtils.nullSafeHashCode(code)
        result = 31 * result + ObjectUtils.nullSafeHashCode(source)
        result = 31 * result + ObjectUtils.nullSafeHashCode(arguments)
        result = 31 * result + ObjectUtils.nullSafeHashCode(defaultMessage)
        return result
    }
}