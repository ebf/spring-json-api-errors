package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingMatrixVariableException
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.*

/**
 * Implementation of an [ExceptionResolver] that is responsible for handling
 * various web exceptions.
 */
class WebServletExceptionResolver: ExceptionResolver {

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }

    override fun resolve(throwable: Throwable): ResolvedException? {
        if (throwable is HttpRequestMethodNotSupportedException) {
            return handleHttpRequestMethodNotSupported(throwable)
        }

        if (throwable is HttpMediaTypeNotSupportedException) {
            return handleHttpMediaTypeNotSupported(throwable)
        }

        if (throwable is HttpMediaTypeNotAcceptableException) {
            return handleHttpMediaTypeNotAcceptableException(throwable)
        }

        if (throwable is MissingServletRequestPartException) {
            return handleMissingServletRequestPartException(throwable)
        }

        if (throwable is MissingServletRequestParameterException) {
            return handleMissingServletRequestParameterException(throwable)
        }

        if (throwable is MethodArgumentTypeMismatchException) {
            return handleMethodArgumentTypeMismatchException(throwable)
        }

        if (throwable is MissingRequestHeaderException) {
            return handleMissingRequestHeaderException(throwable)
        }

        if (throwable is MissingRequestCookieException) {
            return handleMissingRequestCookieException(throwable)
        }

        if (throwable is MissingMatrixVariableException) {
            return handleMissingMatrixVariableException(throwable)
        }

        if (throwable is NoHandlerFoundException) {
            return handleNoHandlerFoundException(throwable)
        }

        return null
    }

    /**
     * Handle the case where no request handler method was found for the particular HTTP request method.
     *
     * It sends an HTTP 405 error, sets the "Allow" header and returns an exception response
     * object with a error message.
     *
     * @param ex the HttpRequestMethodNotSupportedException to be handled
     * @return an exception response with a list of supported methods
     */
    private fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResolvedException {
        val headers = HttpHeaders()
        headers.allow = ex.supportedHttpMethods!!

        val errors = listOf(
            ErrorMessageResolvable("exception.method-not-supported",
                arrayOf(StringUtils.arrayToCommaDelimitedString(ex.supportedMethods))
            )
        )

        return ResolvedException(status = HttpStatus.METHOD_NOT_ALLOWED, headers = headers, errors = errors)
    }

    /**
     * Handle the case where the request handler cannot generate a response that is acceptable by the client
     *
     * It sends an HTTP 406 error, sets the "Allow" header and returns an exception response
     * object with a error message.
     *
     * @param ex the HttpMediaTypeNotAcceptableException to be handled
     * @return an exception response with a list of supported methods
     */
    private fun handleHttpMediaTypeNotAcceptableException(ex: HttpMediaTypeNotAcceptableException): ResolvedException {
        val headers = HttpHeaders()
        headers.accept = ex.supportedMediaTypes

        val errors = listOf(
            ErrorMessageResolvable("exception.content-type-not-supported",
                arrayOf(MediaType.toString(ex.supportedMediaTypes))
            )
        )

        return ResolvedException(status = HttpStatus.NOT_ACCEPTABLE, headers = headers, errors = errors)
    }

    /**
     * Handle the case where no [message converters][org.springframework.http.converter.HttpMessageConverter]
     * were found for the PUT or POSTed content.
     *
     * It sends an HTTP 415 error, sets the "Accept" header and returns an exception response
     * object with a error message.
     *
     * @param ex the HttpMediaTypeNotSupportedException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResolvedException {
        val headers = HttpHeaders()
        headers.accept = ex.supportedMediaTypes

        val errors = listOf(
            ErrorMessageResolvable("exception.content-type-not-supported",
                arrayOf(MediaType.toString(ex.supportedMediaTypes))
            )
        )

        return ResolvedException(status = HttpStatus.UNSUPPORTED_MEDIA_TYPE, headers = headers, errors = errors)
    }

    /**
     * Handle the case where the file request parameter is missing.
     *
     * It sends an HTTP 400 error and returns an exception response
     * object with a error message.
     *
     * @param ex the MissingServletRequestPartException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleMissingServletRequestPartException(ex: MissingServletRequestPartException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.missing_file_parameter", arrayOf(
                ex.requestPartName
            ), mapOf(
                Pair("parameter", ex.requestPartName)
            ))
        )

        return ResolvedException(status = HttpStatus.BAD_REQUEST, errors = errors)
    }

    /**
     * Handle the case where the required request parameter is missing.
     *
     * It sends an HTTP 400 error and returns an exception response
     * object with a error message.
     *
     * @param ex the MissingServletRequestParameterException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.missing_parameter", arrayOf(
                ex.parameterName, ex.parameterType
            ), mapOf(
                Pair("parameter", ex.parameterName)
            ))
        )

        return ResolvedException(status = HttpStatus.BAD_REQUEST, errors = errors)
    }

    /**
     * Handle the case where the request parameter type could not be resolved
     *
     * It sends an HTTP 400 error and returns an exception response
     * object with a error message.
     *
     * @param ex the MissingServletRequestParameterException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.invalid_parameter", arrayOf(
                ex.name, Objects.toString(ex.requiredType)
            ), mapOf(
                Pair("parameter", ex.name)
            ))
        )

        return ResolvedException(status = HttpStatus.BAD_REQUEST, errors = errors)
    }

    /**
     * Handle the case where the required cookie in the request is missing.
     *
     * It sends an HTTP 400 error and returns an exception response
     * object with a error message.
     *
     * @param ex the MissingRequestCookieException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleMissingRequestCookieException(ex: MissingRequestCookieException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.missing_cookie", arrayOf(
                ex.cookieName, ex.parameter.parameterType.simpleName
            ))
        )

        return ResolvedException(status = HttpStatus.BAD_REQUEST, errors = errors)
    }

    /**
     * Handle the case where the required header in the request is missing.
     *
     * It sends an HTTP 400 error and returns an exception response
     * object with a error message.
     *
     * @param ex the MissingRequestHeaderException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleMissingRequestHeaderException(ex: MissingRequestHeaderException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.missing_header", arrayOf(
                ex.headerName, ex.parameter.parameterType.simpleName
            ))
        )

        return ResolvedException(status = HttpStatus.BAD_REQUEST, errors = errors)
    }

    /**
     * Handle the case where the request parameter is missing.
     *
     * It sends an HTTP 400 error and returns an exception response
     * object with a error message.
     *
     * @param ex the MissingMatrixVariableException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleMissingMatrixVariableException(ex: MissingMatrixVariableException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.missing_matrix_variable", arrayOf(
                ex.variableName, ex.parameter.parameterType.simpleName
            ))
        )

        return ResolvedException(status = HttpStatus.BAD_REQUEST, errors = errors)
    }

    /**
     * Handle the case where the request handler can not be find for this request.
     *
     * It sends an HTTP 404 error and returns an exception response
     * object with a error message.
     *
     * @param ex the NoHandlerFoundException to be handled
     * @return an exception response with a list of supported media types
     */
    private fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResolvedException {
        val errors = listOf(
            ErrorMessageResolvable("exception.not_found", arrayOf(ex.requestURL))
        )

        return ResolvedException(status = HttpStatus.NOT_FOUND, errors = errors)
    }

}