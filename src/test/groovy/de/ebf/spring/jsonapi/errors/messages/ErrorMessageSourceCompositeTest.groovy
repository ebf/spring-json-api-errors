package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.NoSuchMessageException
import spock.lang.Specification

class ErrorMessageSourceCompositeTest extends Specification {
    def "should return message"() {
        given:
        def source = Mock(ErrorMessageSource)
        def delegate = new ErrorMessageSourceComposite()
        delegate.addErrorMessageSource(source)

        def resolvable = new ErrorMessageResolvable("code")
        def message = new JsonApiErrors.ErrorMessage("code", "title", "message", null)
        def result

        when:
        1 * source.get(resolvable) >> message
        result = delegate.get(resolvable)

        then:
        message == result

        when:
        1 * source.get(resolvable) >> null
        delegate.get(resolvable)

        then:
        thrown(NoSuchMessageException)

        when:
        1 * source.get(resolvable) >> { r -> throw new NoSuchMessageException("code") }
        delegate.get(resolvable)

        then:
        thrown(NoSuchMessageException)
    }
}
