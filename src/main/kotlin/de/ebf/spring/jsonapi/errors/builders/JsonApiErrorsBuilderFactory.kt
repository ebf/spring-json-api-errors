package de.ebf.spring.jsonapi.errors.builders

import de.ebf.spring.jsonapi.errors.logging.ErrorLogger
import de.ebf.spring.jsonapi.errors.messages.DelegatingErrorMessageSource
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageSource
import de.ebf.spring.jsonapi.errors.resolvers.ExceptionResolver
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
    private val errorMessageBundles = mutableSetOf<String>()
    private var defaultErrorMessageCode = "exception.error-message"
    private var includeStackTrace: Boolean = false
    private var errorLogger: ErrorLogger = NoopLogger()
    private var errorMessageSource: ErrorMessageSource? = null

    /**
     * Configure a custom [ErrorMessageSource] that should be used to resolve exception messages.
     * When a custom message source is used, the configured error message bundles are ignored
     */
    fun withErrorMessageSource(errorMessageSource: ErrorMessageSource) = apply {
        Assert.notNull(errorMessageSource, "Error message source can not be null")
        this.errorMessageSource = errorMessageSource
    }

    fun withMessageBundles(vararg bundles: String) = apply {
        Assert.notEmpty(bundles, "Message bundle locations can not be empty")
        Assert.noNullElements(bundles, "Message bundle locations can not contain null elements")

        this.errorMessageBundles.addAll(bundles)
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

    fun includeStackTrace(includeStackTrace: Boolean) = apply {
        this.includeStackTrace = includeStackTrace
    }

    fun build(): JsonApiErrorsBuilder {
        Assert.notEmpty(exceptionResolvers, "You need to define at least one Exception resolver")

        // sort resolvers
        val exceptionResolvers = ArrayList(this.exceptionResolvers).sortedWith(OrderComparator.INSTANCE)

        // build error message source
        if (this.errorMessageSource == null) {
            val source = ResourceBundleMessageSource()
            source.addBasenames(*this.errorMessageBundles.toTypedArray())
            source.setUseCodeAsDefaultMessage(false)
            source.setAlwaysUseMessageFormat(true)
            this.errorMessageSource = DelegatingErrorMessageSource(source)
        }

        val builder = DefaultJsonApiErrorsBuilder()
        builder.errorLogger = errorLogger
        builder.includeStackTrace = includeStackTrace
        builder.exceptionResolvers = exceptionResolvers
        builder.errorMessageSource = errorMessageSource!!
        builder.defaultErrorMessageCode = defaultErrorMessageCode

        return builder
    }

    private class NoopLogger: ErrorLogger {
        override fun log(throwable: Throwable?) {}
    }

}