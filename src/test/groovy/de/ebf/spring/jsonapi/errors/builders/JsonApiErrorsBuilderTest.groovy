package de.ebf.spring.jsonapi.errors.builders

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import de.ebf.spring.jsonapi.errors.exceptions.JsonApiException
import de.ebf.spring.jsonapi.errors.logging.ErrorLogger
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import de.ebf.spring.jsonapi.errors.messages.ErrorMessageSource
import de.ebf.spring.jsonapi.errors.messages.Resolvable
import de.ebf.spring.jsonapi.errors.resolvers.ExceptionResolver
import de.ebf.spring.jsonapi.errors.resolvers.ResolvedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class JsonApiErrorsBuilderTest extends Specification {

    def "should create a builder instance and build an http error object"() {
        given:
        def factory = new JsonApiErrorsBuilderFactory()
        def resolver = Mock(ExceptionResolver)
        def logger = Mock(ErrorLogger)

        def builder = factory.withErrorLogger(logger)
            .withExceptionResolver(resolver)
            .withDefaultErrorMessageCode("default-code")
            .withMessageBundles("de/ebf/spring/jsonapi/errors/additional-messages")
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
            entity.body.errors[0].title == "Default message title"
            entity.body.errors[0].detail == "Default message message"
        }

        when:
        1 * logger.log(_ as IllegalArgumentException)
        1 * resolver.resolve(_ as IllegalArgumentException) >> new ResolvedException(HttpStatus.NOT_FOUND,
            HttpHeaders.EMPTY, [new ErrorMessageResolvable("errors.test-error")])
        entity = builder.build(new IllegalArgumentException())

        then:
        verifyAll {
            entity != null
            entity.body != null
            entity.statusCode == HttpStatus.NOT_FOUND
            entity.body.errors[0].code == "errors.test-error"
            entity.body.errors[0].source == [:]
            entity.body.errors[0].title == "Test error title"
            entity.body.errors[0].detail == "Test error message"
        }
    }

    def "should use defined charset when encoding error messages"() {
        given:
        def resolver = Mock(ExceptionResolver)
        def logger = Mock(ErrorLogger)

        def builder = new JsonApiErrorsBuilderFactory().withErrorLogger(logger)
                .withExceptionResolver(resolver)
                .withDefaultErrorMessageEncoding(StandardCharsets.UTF_8.name())
                .withMessageBundles("de/ebf/spring/jsonapi/errors/encoding-test-errors")
                .build()

        ResponseEntity<JsonApiErrors> entity

        when:
        1 * logger.log(_ as IllegalArgumentException)
        1 * resolver.resolve(_ as IllegalArgumentException) >> new ResolvedException(HttpStatus.NOT_FOUND,
                HttpHeaders.EMPTY, [new ErrorMessageResolvable("errors.test-error")])

        entity = builder.build(new IllegalArgumentException())

        then:
        verifyAll {
            entity != null
            entity.body != null
            entity.statusCode == HttpStatus.NOT_FOUND
            entity.body.errors[0].code == "errors.test-error"
            entity.body.errors[0].source == [:]
            entity.body.errors[0].title == "Fehler"
            entity.body.errors[0].detail == "Ihr Token ist abgelaufen, zurückgezogen oder aus anderem Grund ungültig."
        }
    }

    def "should sort resolvers by order and use custom message source"() {
        given:
        def source = createErrorMessageSource()
        def factory = new JsonApiErrorsBuilderFactory()

        def builder = factory
                .withErrorMessageSource(source)
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
            response.body.errors[0].code == "resolver-12-source"
            response.body.errors[0].title == "title"
            response.body.errors[0].detail == "detail"
            response.body.errors[0].source == [:]
        }
    }

    def "should catch exceptions from the resolvers"() {
        given:
        def source = createErrorMessageSource()
        def resolver = Mock(ExceptionResolver)
        def factory = new JsonApiErrorsBuilderFactory()

        def builder = factory
                .withErrorMessageSource(source)
                .withExceptionResolver(resolver)
                .build()

        when:
        builder.build(new IllegalAccessException())

        then:
        1 * resolver.resolve(_ as Throwable) >> { throw new RuntimeException("Ooops") }

        and:
        def ex = thrown(JsonApiException)
        ex.cause instanceof RuntimeException
        ex.cause.message == "Ooops"
    }

    def "should catch exceptions from the message source"() {
        given:
        def source = Mock(ErrorMessageSource)
        def resolver = Mock(ExceptionResolver)
        def factory = new JsonApiErrorsBuilderFactory()

        def builder = factory
                .withErrorMessageSource(source)
                .withExceptionResolver(resolver)
                .build()

        when:
        builder.build(new IllegalAccessException())

        then:
        1 * resolver.resolve(_ as Throwable) >> new ResolvedException(HttpStatus.NOT_FOUND,
                HttpHeaders.EMPTY, [new ErrorMessageResolvable("errors.test-error")])
        1 * source.get(_ as Resolvable) >> { throw new RuntimeException("Ooops") }

        and:
        def ex = thrown(JsonApiException)
        ex.cause instanceof RuntimeException
        ex.cause.message == "Ooops"
    }

    static ErrorMessageSource createErrorMessageSource() {
        return { resolvable ->
            new JsonApiErrors.ErrorMessage("$resolvable.code-source", "title", "detail", [:])
        }
    }

    def createExceptionResolver(Integer order) {
        def resolver = Mock(ExceptionResolver)
        resolver.toString() >> "ExceptionResolver($order)"
        resolver.resolve(_) >> new ResolvedException(HttpStatus.NOT_FOUND, HttpHeaders.EMPTY, [
                new ErrorMessageResolvable("resolver-$order")
        ])
        return resolver
    }

}
