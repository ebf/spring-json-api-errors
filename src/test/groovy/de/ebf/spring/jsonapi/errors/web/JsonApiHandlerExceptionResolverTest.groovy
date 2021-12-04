package de.ebf.spring.jsonapi.errors.web

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder
import de.ebf.spring.jsonapi.errors.exceptions.JsonApiException
import de.ebf.spring.jsonapi.errors.writer.JsonApiErrorsWriter
import org.springframework.core.Ordered
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author : vladimir.spasic@ebf.com
 * @since : 04.12.21, Sat
 * */
class JsonApiHandlerExceptionResolverTest extends Specification {

    def builder = Mock(JsonApiErrorsBuilder)
    def writer = Mock(JsonApiErrorsWriter)
    def entity = ResponseEntity.status(500).body(new JsonApiErrors([]))

    def resolver

    def setup() {
        resolver = new JsonApiHandlerExceptionResolver(builder, writer)
    }

    def "should always have highest order"() {
        expect:
        Ordered.HIGHEST_PRECEDENCE == resolver.order
    }

    def "should build and write json api errors"() {
        given:
        def request = Mock(HttpServletRequest)
        def response = Mock(HttpServletResponse)
        def ex = new IllegalAccessException("Illegal access")

        when:
        def mav = resolver.resolveException(request, response, null, ex)

        then:
        1 * builder.build(ex) >> entity
        1 * writer.write(request, response, entity)

        and:
        mav != null

        and:
        noExceptionThrown()
    }

    def "should catch IO and Servlet Exceptions"() {
        given:
        def request = Mock(HttpServletRequest)
        def response = Mock(HttpServletResponse)
        def ex = new IllegalAccessException("Illegal access")

        when:
        def mav = resolver.resolveException(request, response, null, ex)

        then:
        1 * builder.build(ex) >> entity
        1 * writer.write(request, response, entity) >> { throw new IOException("Failed to write response") }

        and:
        mav == null

        and:
        noExceptionThrown()

        when:
        mav = resolver.resolveException(request, response, null, ex)

        then:
        1 * builder.build(ex) >> entity
        1 * writer.write(request, response, entity) >> { throw new ServletException("Failed to write response") }

        and:
        mav == null

        and:
        noExceptionThrown()
    }

    def "should not catch runtime exceptions"() {
        given:
        def request = Mock(HttpServletRequest)
        def response = Mock(HttpServletResponse)
        def ex = new IllegalAccessException("Illegal access")

        when:
        resolver.resolveException(request, response, null, ex)

        then:
        1 * builder.build(ex) >> { throw new JsonApiException("Ooops") }
        0 * writer.write(request, response, _ as ResponseEntity)

        and:
        thrown(JsonApiException)

        when:
        resolver.resolveException(request, response, null, ex)

        then:
        1 * builder.build(ex) >> entity
        1 * writer.write(request, response, entity) >> { throw new RuntimeException("Ooops") }

        and:
        thrown(RuntimeException)
    }

}
