package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.exceptions.JsonApiResolvableException
import org.jetbrains.annotations.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import spock.lang.Specification

/**
 * @author <ahref="mailto:vladimir.spasic@ebf.com" > Vladimir Spasic</a>
 * @since 17.10.19, Thu
 * */
class ResolvableExceptionResolverTest extends Specification {

    def resolver = new ResolvableExceptionResolver()

    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    class MappedException extends JsonApiResolvableException {

        MappedException(@NotNull String code, @NotNull String message) {
            super(code, message)
        }
    }

    def "should resolve exception with defined status code"() {
        when:
        def resolved = resolver.resolve(new MappedException("error-code", "Default message"))

        then:
        verifyAll {
            resolved != null
            resolved.headers != null
            resolved.status == HttpStatus.BAD_REQUEST
            resolved.errors.size() == 1
            resolved.errors[0].code == "error-code"
            resolved.errors[0].source == [:]
            resolved.errors[0].arguments == new Object[0]
            resolved.errors[0].defaultMessage == "Default message"
        }

    }

    def "should resolve exception with default 500 status code"() {
        when:
        def resolved = resolver.resolve(new JsonApiResolvableException("error-code", "Default message"))

        then:
        verifyAll {
            resolved != null
            resolved.headers != null
            resolved.status == HttpStatus.INTERNAL_SERVER_ERROR
            resolved.errors.size() == 1
            resolved.errors[0].code == "error-code"
            resolved.errors[0].source == [:]
            resolved.errors[0].arguments == new Object[0]
            resolved.errors[0].defaultMessage == "Default message"
        }

    }

    def "should return null for not annotated exceptions"() {
        given:
        def resolver = new ResponseStatusExceptionResolver()
        ResolvedException resolved

        when:
        resolved = resolver.resolve(new IllegalArgumentException())

        then:
        resolved == null

    }
}
