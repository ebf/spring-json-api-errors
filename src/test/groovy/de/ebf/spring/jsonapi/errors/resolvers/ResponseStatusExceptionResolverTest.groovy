package de.ebf.spring.jsonapi.errors.resolvers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import spock.lang.Specification

class ResponseStatusExceptionResolverTest extends Specification {

    @ResponseStatus(reason = "error-code", code = HttpStatus.BAD_REQUEST)
    class MappedException extends RuntimeException {}

    @ResponseStatus(reason = "error-code")
    class MappedExceptionWithDefaultStatus extends RuntimeException {}

    def "should resolve exception with defined status code"() {
        given:
        def resolver = new ResponseStatusExceptionResolver()

        ResolvedException resolved = null

        when:
        resolved = resolver.resolve(new MappedException())

        then:
        verifyAll {
            resolved != null
            resolved.headers != null
            resolved.status == HttpStatus.BAD_REQUEST
            resolved.errors.size() == 1
            resolved.errors[0].code == "error-code"
            resolved.errors[0].source == null
            resolved.errors[0].arguments == null
        }

    }

    def "should resolve exception with default status code"() {
        given:
        def resolver = new ResponseStatusExceptionResolver()

        ResolvedException resolved = null

        when:
        resolved = resolver.resolve(new MappedExceptionWithDefaultStatus())

        then:
        verifyAll {
            resolved != null
            resolved.headers != null
            resolved.status == HttpStatus.INTERNAL_SERVER_ERROR
            resolved.errors.size() == 1
            resolved.errors[0].code == "error-code"
            resolved.errors[0].source == null
            resolved.errors[0].arguments == null
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
