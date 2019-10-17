package de.ebf.spring.jsonapi.errors.messages

/**
 * Interface used by the [ErrorMessageSource] to resolve the title and details error
 * messages for the given exception code and arguments.
 *
 * @author <a href="mailto:vladimir.spasic@ebf.com">Vladimir Spasic</a>
 * @since 17.10.19, Thu
 **/
interface Resolvable {

    /**
     * Message code used by the [ErrorMessageSource] to find a matching translation
     */
    val code: String

    /**
     * Arguments used by the [ErrorMessageSource] to format and interpolate the translation
     * string that was found for the [code].
     */
    val arguments: Array<Any>

    /**
     * According to the JSON API Specification an error object can contain a `source` object
     * that contains additional data that caused the error.
     */
    val source: Map<String, Any>

    /**
     * Default message string that is used if the translation is not found for [code]
     */
    val defaultMessage: String?

}