package de.ebf.spring.jsonapi.errors.builders

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import de.ebf.spring.jsonapi.errors.logging.ErrorLogger
import de.ebf.spring.jsonapi.errors.messages.DefaultErrorMessageSource
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageSource
import de.ebf.spring.jsonapi.errors.resolvers.ExceptionResolver
import de.ebf.spring.jsonapi.errors.resolvers.ResolvedException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class JsonApiErrorsBuilderTest extends Specification {

    def createMessageSource(Map messages) {
        def source = new StaticMessageSource()
        source.addMessages(messages, LocaleContextHolder.getLocale())

        return source
    }

    def "should create a builder instance and build an http error object"() {
        given:
        def factory = new JsonApiErrorsBuilderFactory()
        def messageSource = createMessageSource([
                "default-code": "default message",
                "error-code.title": "Error title",
                "error-code.message": "Error message"
        ])
        def resolver = Mock(ExceptionResolver)
        def logger = Mock(ErrorLogger)

        def builder = factory.withErrorLogger(logger)
            .withExceptionResolver(resolver)
            .withDefaultErrorMessageCode("default-code")
            .withErrorMessageSource(new DefaultErrorMessageSource(messageSource))
            .build()

        ResponseEntity<JsonApiErrors> entity

        when:
        1 * logger.log(_ as IllegalArgumentException)
        1 * resolver.resolve(_ as IllegalArgumentException)
        entity = builder.build(new IllegalArgumentException())

        then:
        verifyAll {
            entity != null
            entity.body != null
            entity.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
            entity.body.errors[0].code == "default-code"
            entity.body.errors[0].source == [:]
            entity.body.errors[0].title == null
            entity.body.errors[0].detail == "default message"
        }

        when:
        1 * logger.log(_ as IllegalArgumentException)
        1 * resolver.resolve(_ as IllegalArgumentException) >> new ResolvedException(HttpStatus.NOT_FOUND,
            HttpHeaders.EMPTY, [new ErrorMessageResolvable("error-code")])
        entity = builder.build(new IllegalArgumentException())

        then:
        verifyAll {
            entity != null
            entity.body != null
            entity.statusCode == HttpStatus.NOT_FOUND
            entity.body.errors[0].code == "error-code"
            entity.body.errors[0].source == [:]
            entity.body.errors[0].title == "Error title"
            entity.body.errors[0].detail == "Error message"
        }
    }

    def createErrorMessageSource(Integer order) {
        def source = Mock(ErrorMessageSource)
        source.toString() >> "ErrorMessageSource($order)"
        source.getOrder() >> order
        source.get(_) >> { resolvable -> new JsonApiErrors.ErrorMessage(
                "$resolvable.code-source-$order", "title", "detail", ["order": order]
        )}
        return source
    }

    def createExceptionResolver(Integer order) {
        def resolver = Mock(ExceptionResolver)
        resolver.toString() >> "ExceptionResolver($order)"
        resolver.resolve(_) >> new ResolvedException(HttpStatus.NOT_FOUND, HttpHeaders.EMPTY, [
                new ErrorMessageResolvable("resolver-$order")
        ])
        return resolver
    }

    def "should sort message sources and resolvers by order"() {
        given:
        def factory = new JsonApiErrorsBuilderFactory()
        def firstSource = createErrorMessageSource(6)
        def secondSource = createErrorMessageSource(2)
        def thirdSource = createErrorMessageSource(3)

        def builder =factory.withDefaultErrorMessageCode("default-code")
                .withErrorMessageSource(firstSource)
                .withErrorMessageSource(secondSource)
                .withErrorMessageSource(thirdSource)
                .withExceptionResolver(createExceptionResolver(12))
                .withExceptionResolver(createExceptionResolver(13))
                .withExceptionResolver(createExceptionResolver(16))
                .build()

        when:
        def response = builder.build(new IllegalAccessException())

        then:
        verifyAll {
            response != null
            response.statusCode == HttpStatus.NOT_FOUND
            response.body.errors[0].code == "[resolver-12]-source-2"
            response.body.errors[0].title == "title"
            response.body.errors[0].detail == "detail"
            response.body.errors[0].source == ["order": 2]
        }
    }

}
