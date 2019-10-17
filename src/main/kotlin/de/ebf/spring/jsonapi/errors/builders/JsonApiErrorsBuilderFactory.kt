package de.ebf.spring.jsonapi.errors.builders

import de.ebf.spring.jsonapi.errors.logging.ErrorLogger
import de.ebf.spring.jsonapi.errors.messages.DefaultErrorMessageSource
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageSourceComposite
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageSource
import de.ebf.spring.jsonapi.errors.resolvers.ExceptionResolver
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.core.OrderComparator
import org.springframework.util.Assert

/**
 * Factory class used to construct and configure the [JsonApiErrorsBuilder] which
 * [ErrorMessageSource]s should the be used when translating error message codes.
 *
 * You can also add multiple [ExceptionResolver]s that would resolve the error
 * message code from the thrown exception.
 *
 * In case the [de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable] can not
 * be found an exception, you can configure the default error code that would be used
 * to resolve the error message.
 *
 * Be aware that [ErrorMessageSource]s and [ExceptionResolver]s implement
 * [org.springframework.core.Ordered] interface and each implementation would be
 * sorted depending on the specified order.
 */
class JsonApiErrorsBuilderFactory {

    private val exceptionResolvers = mutableSetOf<ExceptionResolver>()
    private val errorMessageSources = mutableSetOf<ErrorMessageSource>()
    private var defaultErrorMessageCode = "exception.error-message"
    private var errorLogger: ErrorLogger = NoopLogger()

    fun withErrorMessageSource(errorMessageSource: ErrorMessageSource) = apply {
        Assert.notNull(errorMessageSource, "Error message source can not be null")
        this.errorMessageSources.add(errorMessageSource)
    }

    fun witMessageSource(messageSource: MessageSource) = apply {
        Assert.notNull(messageSource, "Message source can not be null")
        this.errorMessageSources.add(DefaultErrorMessageSource(messageSource))
    }

    fun withMessageBundles(vararg bundles: String) = apply {
        Assert.notEmpty(bundles, "Message bundle locations can not be empty")
        Assert.noNullElements(bundles, "Message bundle locations can not contain null elements")

        val source = ResourceBundleMessageSource()
        source.addBasenames(*bundles)
        source.setUseCodeAsDefaultMessage(false)
        source.setAlwaysUseMessageFormat(true)
        witMessageSource(source)
    }

    fun withExceptionResolver(exceptionResolver: ExceptionResolver) = apply {
        Assert.notNull(exceptionResolver, "Exception resolver can not be null")
        this.exceptionResolvers.add(exceptionResolver)
    }

    fun withExceptionResolvers(exceptionResolvers: Collection<ExceptionResolver>) = apply {
        Assert.notNull(exceptionResolvers, "Exception resolver collection can not be null")
        this.exceptionResolvers.addAll(exceptionResolvers)
    }

    fun withDefaultErrorMessageCode(defaultErrorMessageCode: String) = apply {
        Assert.hasText(defaultErrorMessageCode, "Default error message code can not be empty")
        this.defaultErrorMessageCode = defaultErrorMessageCode
    }

    fun withErrorLogger(errorLogger: ErrorLogger) = apply {
        this.errorLogger = errorLogger
    }

    fun build(): JsonApiErrorsBuilder {
        Assert.notEmpty(errorMessageSources, "You need to define at least one Error message source")
        Assert.notEmpty(exceptionResolvers, "You need to define at least one Exception resolver")

        // sort resolvers
        val exceptionResolvers = ArrayList(this.exceptionResolvers).sortedWith(OrderComparator.INSTANCE)

        // sort sources
        val errorMessageSource = ErrorMessageSourceComposite()
        this.errorMessageSources.sortedWith(OrderComparator.INSTANCE).forEach { source ->
            errorMessageSource.addErrorMessageSource(source)
        }

        val builder = DefaultJsonApiErrorsBuilder()
        builder.errorLogger = errorLogger
        builder.exceptionResolvers = exceptionResolvers
        builder.errorMessageSource = errorMessageSource
        builder.defaultErrorMessageCode = defaultErrorMessageCode

        return builder
    }

    private class NoopLogger: ErrorLogger {
        override fun log(throwable: Throwable?) {}
    }

}