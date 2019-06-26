package de.ebf.spring.jsonapi.errors.config

import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilderFactory
import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry
import de.ebf.spring.jsonapi.errors.messages.DefaultErrorMessageSource
import de.ebf.spring.jsonapi.errors.resolvers.MappingExceptionResolver
import de.ebf.spring.jsonapi.errors.resolvers.ResponseStatusExceptionResolver
import de.ebf.spring.jsonapi.errors.resolvers.ValidationExceptionResolver
import de.ebf.spring.jsonapi.errors.resolvers.WebServletExceptionResolver
import de.ebf.spring.jsonapi.errors.writer.HttpErrorsWriter
import de.ebf.spring.jsonapi.errors.writer.ServletHttpErrorsWriter
import org.springframework.beans.ConversionNotSupportedException
import org.springframework.beans.TypeMismatchException
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.*
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.context.request.async.AsyncRequestTimeoutException

@Configuration
class JsonApiErrorAutoConfiguration @Autowired constructor(
    private val configurers: Collection<JsonApiErrorConfigurer>
) {

    @Bean
    @ConditionalOnMissingBean(HttpErrorsWriter::class)
    fun defaultHttpErrorsWriter(httpMessageConverters: ObjectProvider<HttpMessageConverters>): HttpErrorsWriter {
        return ServletHttpErrorsWriter(httpMessageConverters.getObject())
    }

    @Bean
    @ConditionalOnMissingBean(JsonApiErrorsBuilderFactory::class)
    fun httpErrorBuilderFactory(): JsonApiErrorsBuilderFactory {
        return JsonApiErrorsBuilderFactory()
    }

    @Bean
    @ConditionalOnMissingBean(JsonApiErrorsBuilder::class)
    @ConditionalOnBean(JsonApiErrorsBuilderFactory::class)
    fun defaultHttpErrorBuilder(factory: JsonApiErrorsBuilderFactory): JsonApiErrorsBuilder {
        val registry = ErrorMappingRegistry()
        val messageSource = ResourceBundleMessageSource()
        messageSource.addBasenames("de/ebf/spring/jsonapi/errors/messages")
        messageSource.setUseCodeAsDefaultMessage(false)
        messageSource.setAlwaysUseMessageFormat(true)

        factory
            .withExceptionResolver(WebServletExceptionResolver())
            .withExceptionResolver(ValidationExceptionResolver())
            .withExceptionResolver(ResponseStatusExceptionResolver())
            .withExceptionResolver(MappingExceptionResolver(registry))
            .withErrorMessageSource(DefaultErrorMessageSource(messageSource))

        configurers.forEach { configurer ->
            configurer.configure(registry)
            configurer.configure(factory)
        }

        return factory.build()
    }

    @Configuration
    @AutoConfigureBefore(JsonApiErrorAutoConfiguration::class)
    class WebJsonApiErrorConfigurer: JsonApiErrorConfigurer {

        override fun configure(registry: ErrorMappingRegistry) {
            registry.register(HttpMediaTypeNotAcceptableException::class.javaObjectType)
                .code("exception.media_not_acceptable").status(HttpStatus.NOT_ACCEPTABLE)
            registry.register(ServletRequestBindingException::class.javaObjectType)
                .code("exception.request_binding").status(HttpStatus.BAD_REQUEST)
            registry.register(TypeMismatchException::class.javaObjectType)
                .code("exception.type_mismatch").status(HttpStatus.BAD_REQUEST)
            registry.register(HttpMessageNotReadableException::class.javaObjectType)
                .code("exception.not_readable_message").status(HttpStatus.BAD_REQUEST)
            registry.register(HttpMessageNotWritableException::class.javaObjectType)
                .code("exception.not_writable_message")
            registry.register(ConversionNotSupportedException::class.javaObjectType)
                .code("exception.unsupported_conversion")
            registry.register(AsyncRequestTimeoutException::class.javaObjectType)
                .code("exception.async_timeout").status(HttpStatus.SERVICE_UNAVAILABLE)
        }

    }

    @Configuration
    @AutoConfigureBefore(JsonApiErrorAutoConfiguration::class)
    @ConditionalOnClass(name = ["org.springframework.security.web.access.AccessDeniedHandler"])
    class WebSecurityJsonApiErrorConfigurer: JsonApiErrorConfigurer {

        companion object {
            private const val ACCESS_DENIED = "exception.access_denied"
            private const val ACCOUNT_LOCKED = "exception.account_locked"
            private const val ACCOUNT_DISABLED = "exception.account_disabled"
            private const val ACCOUNT_EXPIRED = "exception.account_expired"
            private const val UNAUTHENTICATED  = "exception.not_authenticated"
            private const val INVALID_CREDENTIALS = "exception.invalid_credentials"
        }

        override fun configure(registry: ErrorMappingRegistry) {
            registry.register(AccessDeniedException::class.javaObjectType)
                .code(ACCESS_DENIED).status(HttpStatus.FORBIDDEN)
            registry.register(InsufficientAuthenticationException::class.javaObjectType)
                .code(ACCESS_DENIED).status(HttpStatus.FORBIDDEN)
            registry.register(AccountExpiredException::class.javaObjectType)
                .code(ACCOUNT_EXPIRED).status(HttpStatus.UNAUTHORIZED)
            registry.register(LockedException::class.javaObjectType)
                .code(ACCOUNT_LOCKED).status(HttpStatus.UNAUTHORIZED)
            registry.register(DisabledException::class.javaObjectType)
                .code(ACCOUNT_DISABLED).status(HttpStatus.UNAUTHORIZED)
            registry.register(BadCredentialsException::class.javaObjectType)
                .code(INVALID_CREDENTIALS).status(HttpStatus.UNAUTHORIZED)
            registry.register(UsernameNotFoundException::class.javaObjectType)
                .code(INVALID_CREDENTIALS).status(HttpStatus.UNAUTHORIZED)
            registry.register(AuthenticationCredentialsNotFoundException::class.javaObjectType)
                .code(UNAUTHENTICATED).status(HttpStatus.UNAUTHORIZED)
        }

    }
}