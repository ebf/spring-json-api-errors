package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry
import org.springframework.http.HttpStatus
import spock.lang.Specification

class MappingExceptionResolverTest extends Specification {

    def "should resolve exception from mappings"() {
        given:
        def registry = new ErrorMappingRegistry()
        def resolver = new MappingExceptionResolver(registry)

        registry.register(IllegalArgumentException.class)
                .code("illegal").status(HttpStatus.BAD_REQUEST)

        ResolvedException resolved = null

        when:
        resolved = resolver.resolve(new IllegalArgumentException())

        then:
        verifyAll {
            resolved != null
            resolved.headers != null
            resolved.status == HttpStatus.BAD_REQUEST
            resolved.errors.size() == 1
            resolved.errors[0].code == "illegal"
            resolved.errors[0].source == null
            resolved.errors[0].arguments == null
        }

    }

    def "should return null for missing error mapping"() {
        given:
        def registry = new ErrorMappingRegistry()
        def resolver = new MappingExceptionResolver(registry)
        ResolvedException resolved

        when:
        resolved = resolver.resolve(new IllegalArgumentException())

        then:
        resolved == null

    }
}
