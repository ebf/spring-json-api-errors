package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * Implementation of an [ExceptionResolver] that is responsible for handling
 * [MethodArgumentNotValidException]s, [BindException]s
 * from bean validation.
 */
class ValidationExceptionResolver: ExceptionResolver {

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 2
    }

    override fun resolve(throwable: Throwable): ResolvedException? {
        if (throwable is MethodArgumentNotValidException) {
            return handleMethodArgumentNotValidException(throwable)
        }

        if (throwable is BindException) {
            return handleBindException(throwable)
        }

        return null
    }

    /**
     * Handle the case where an argument annotated with `@Valid` such as
     * an [RequestBody] or [RequestPart] argument fails validation.
     *
     * It sends an HTTP 415 error, sets the "Accept" header and returns an exception response
     * object with validation error messages.
     *
     * @param ex the MethodArgumentNotValidException to be handled
     * @return an exception response with a list of validation errors
     */
    private fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResolvedException {
        val errors = ex.bindingResult.fieldErrors.map { error -> toErrorMessageResolvable(error) }
        return ResolvedException(status = HttpStatus.UNPROCESSABLE_ENTITY, errors = errors)
    }

    /**
     * Handle the case where an [@ModelAttribute][ModelAttribute] method
     * argument has binding or validation errors and is not followed by another
     * method argument of type [BindingResult].
     *
     * It sends an HTTP 415 error, sets the "Accept" header and returns an exception response
     * object with validation error messages.
     *
     * @param ex the MethodArgumentNotValidException to be handled
     * @return an exception response with a list of validation errors
     */
    private fun handleBindException(ex: BindException): ResolvedException {
        val errors = ex.bindingResult.fieldErrors.map { error -> toErrorMessageResolvable(error) }
        return ResolvedException(status = HttpStatus.UNPROCESSABLE_ENTITY, errors = errors)
    }

    private fun toErrorMessageResolvable(error: FieldError): ErrorMessageResolvable {
        var code = error.code
        var defaultMessage = error.defaultMessage

        // custom message code always starts with an {
        if (error.defaultMessage?.startsWith('{') == true) {
            code = error.defaultMessage!!.replace("{", "").replace("}", "")
            defaultMessage = null
        }

        return ErrorMessageResolvable(
            code = code!!,
            arguments = error.arguments,
            defaultMessage = defaultMessage,
            source = mapOf(Pair("pointer", error.field.replace(".", "/")))
        )
    }

}