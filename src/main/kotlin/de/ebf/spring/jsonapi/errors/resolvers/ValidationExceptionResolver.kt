package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.util.StringUtils
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.ElementKind

/**
 * Implementation of an [ExceptionResolver] that is responsible for handling
 * [MethodArgumentNotValidException]s, [BindException]s
 * from bean validation.
 */
class ValidationExceptionResolver: ExceptionResolver {

    companion object {
        private val VALID_ELEMENT_KINDS = setOf(ElementKind.PARAMETER, ElementKind.PROPERTY, ElementKind.BEAN)
    }

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 2
    }

    override fun resolve(throwable: Throwable): ResolvedException? {
        if (throwable is MethodArgumentNotValidException) {
            return handleMethodArgumentNotValidException(throwable)
        }

        if (throwable is BindingResult) {
            return handleBindingResult(throwable)
        }

        if (throwable is ConstraintViolationException) {
            return handleConstraintViolationException(throwable)
        }

        return null
    }

    /**
     * Handle the case where an argument annotated with `@Valid` such as
     * an [org.springframework.web.bind.annotation.RequestBody] or
     * [org.springframework.web.bind.annotation.RequestPart] argument fails validation.
     *
     * It sends an HTTP 422 error and returns an exception response with validation error messages.
     *
     * @param ex the MethodArgumentNotValidException to be handled
     * @return an exception response with a list of validation errors
     */
    private fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResolvedException {
        val errors = ex.bindingResult.fieldErrors.map { error -> toErrorMessageResolvable(error) }
        return ResolvedException(status = HttpStatus.UNPROCESSABLE_ENTITY, errors = errors)
    }

    /**
     * Handle the case where an [@ModelAttribute][org.springframework.web.bind.annotation.ModelAttribute]
     * method argument has binding or validation errors and is not followed by another
     * method argument of type [BindingResult].
     *
     * It sends an HTTP 422 error and returns an exception response with validation error messages.
     *
     * @param ex the MethodArgumentNotValidException to be handled
     * @return an exception response with a list of validation errors
     */
    private fun handleBindingResult(ex: BindingResult): ResolvedException {
        val errors = ex.fieldErrors.map { error -> toErrorMessageResolvable(error) }
        return ResolvedException(status = HttpStatus.UNPROCESSABLE_ENTITY, errors = errors)
    }

    /**
     * Handle the case where an argument annotated with `@Valid` such as
     * an [org.springframework.web.bind.annotation.RequestBody] or
     * [org.springframework.web.bind.annotation.RequestPart] argument fails validation.
     *
     * It sends an HTTP 422 error and returns an exception response with validation error messages.
     *
     * @param ex the ConstraintViolationException to be handled
     * @return an exception response with a list of validation errors
     */
    private fun handleConstraintViolationException(ex: ConstraintViolationException): ResolvedException {
        val errors = ex.constraintViolations.map { violation ->
            val path = extractPath(violation)

            // message templates usually start with an {, we need to remove them
            val code = normalizeTemplate(violation.messageTemplate)

            // construct the resolvable with the normalized code, field path and default message
            ErrorMessageResolvable(
                    code = code,
                    arguments = arrayOf(code),
                    defaultMessage = violation.message,
                    source = mapOf(Pair("pointer", path))
            )
        }.toList()

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
            defaultMessage = defaultMessage,
            arguments = error.arguments ?: emptyArray(),
            source = mapOf(Pair("pointer", error.field.replace(".", "/")))
        )
    }

    private fun extractPath(violation: ConstraintViolation<*>): String {
        return violation.propertyPath
                .filter { node -> VALID_ELEMENT_KINDS.contains(node.kind) }
                .map { it.name }
                .filter { StringUtils.hasText(it) }
                .joinToString("/")
    }

    private fun normalizeTemplate(template: String): String {
        return if (template.startsWith("{")) {
            template.replace("{", "").replace("}", "")
        } else template
    }

}