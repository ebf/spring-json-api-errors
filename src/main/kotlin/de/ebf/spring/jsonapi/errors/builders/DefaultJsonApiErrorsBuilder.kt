package de.ebf.spring.jsonapi.errors.builders

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import de.ebf.spring.jsonapi.errors.exceptions.JsonApiException
import de.ebf.spring.jsonapi.errors.logging.ErrorLogger
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageSource
import de.ebf.spring.jsonapi.errors.resolvers.ExceptionResolver
import de.ebf.spring.jsonapi.errors.resolvers.ResolvedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.Assert
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.StringBuilder

class DefaultJsonApiErrorsBuilder: JsonApiErrorsBuilder, InitializingBean {

    private var logger = LoggerFactory.getLogger(javaClass)

    lateinit var errorLogger: ErrorLogger
    lateinit var defaultErrorMessageCode: String
    lateinit var errorMessageSource: ErrorMessageSource
    lateinit var exceptionResolvers: Collection<ExceptionResolver>

    var includeStackTrace: Boolean = false

    override fun afterPropertiesSet() {
        Assert.notNull(errorLogger, "Error logger must be set")
        Assert.notNull(errorMessageSource, "Error Message source must be set")
        Assert.hasText(defaultErrorMessageCode, "Default error message code can not be empty")
        Assert.notEmpty(exceptionResolvers, "There must be at least one exception resolver present")
    }

    override fun build(throwable: Throwable): ResponseEntity<JsonApiErrors>? {
        errorLogger.log(throwable)

        logger.debug("Building JSON API Errors for an exception", throwable)

        val resolvedException = resolve(throwable)
        val errors = resolvedException.errors.sortedBy { resolvable -> resolvable.code }.map { resolvable ->
            try {
                errorMessageSource.get(resolvable)
            } catch (e: Exception) {
                throw JsonApiException("An error occurred while extracting message for " +
                        "$resolvable and Exception ${throwable.javaClass}", e)
            }
        }

        val stackTrace = if (includeStackTrace) toStackTrace(throwable) else null

        return ResponseEntity
            .status(resolvedException.status)
            .headers(resolvedException.headers)
            .body(JsonApiErrors(errors = errors, stackTrace = stackTrace))
    }

    fun resolve(throwable: Throwable): ResolvedException {
        var resolvedException: ResolvedException? = null
        val iterator = exceptionResolvers.iterator()

        while (resolvedException == null && iterator.hasNext()) {
            val resolver = iterator.next()

            try {
                resolvedException = resolver.resolve(throwable)
            } catch (e: Exception) {
                throw JsonApiException("An error occurred while resolving exception using " +
                        "Resolver ${resolver.javaClass} and Exception ${throwable.javaClass}", e)
            }

            if (resolvedException != null) {
                logger.debug("Exception is resolved using {}: {}", resolver.javaClass, resolvedException)
            }
        }

        if (resolvedException == null) {
            logger.debug("Could not find any Exception resolver for exception {}," +
                    "using default message with code: {}", throwable.javaClass, defaultErrorMessageCode)

            resolvedException = ResolvedException(status = HttpStatus.INTERNAL_SERVER_ERROR, errors = listOf(
                ErrorMessageResolvable(code = defaultErrorMessageCode)
            ))
        }

        return resolvedException
    }

    private fun toStackTrace(throwable: Throwable): String {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer, true));
        return writer.buffer.toString();
    }
}