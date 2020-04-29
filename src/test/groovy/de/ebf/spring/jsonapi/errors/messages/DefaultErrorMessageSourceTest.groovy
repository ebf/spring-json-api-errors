package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification

class DefaultErrorMessageSourceTest extends Specification {

    ErrorMessageSource source

    def setup() {
        def bundle = new ResourceBundleMessageSource()
        bundle.addBasenames("de/ebf/spring/jsonapi/errors/messages",
                "de/ebf/spring/jsonapi/errors/additional-messages",
                "de/ebf/spring/jsonapi/errors/test-errors")
        bundle.setUseCodeAsDefaultMessage(false)
        bundle.setAlwaysUseMessageFormat(true)

        source = new DelegatingErrorMessageSource(bundle)
    }

    def "should resolve message with no arguments"() {
        given:
        JsonApiErrors.ErrorMessage message

        when:
        message = source.get(new ErrorMessageResolvable("errors.test-error"))

        then:
        verifyAll {
            message != null
            message.code == "errors.test-error"
            message.title == "Test error title"
            message.detail == "Test error message"
            message.source == [:]
        }
    }

    def "should resolve message with arguments and source"() {
        given:
        JsonApiErrors.ErrorMessage message

        when:
        message = source.get(new ErrorMessageResolvable("errors.test-error-arguments",
                ["first", "second", "third"].toArray(), [pointer: "/path"]))

        then:
        verifyAll {
            message != null
            message.code == "errors.test-error-arguments"
            message.title == "Test error title first"
            message.detail == "Test error message with arguments: third - first - second"
            message.source == [pointer: "/path"]
        }
    }

    def "should ignore thrown exception for missing title"() {
        given:
        JsonApiErrors.ErrorMessage message

        when:
        message = source.get(new ErrorMessageResolvable("errors.test-error-just-message"))

        then:
        verifyAll {
            message != null
            message.code == "errors.test-error-just-message"
            message.detail == "Test error message with no title"
            message.title == null
            message.source == [:]
        }
    }

    def "should use default message when code is missing"() {
        given:
        JsonApiErrors.ErrorMessage message

        when:
        message = source.get(new ErrorMessageResolvable("errors.missing-error-code", new Object[0],
                [:], "Default message"))

        then:
        verifyAll {
            message != null
            message.code == "errors.missing-error-code"
            message.detail == "Default message"
            message.title == null
            message.source == [:]
        }
    }

    def "should not ignore thrown exception for missing details"() {
        when:
        source.get(new ErrorMessageResolvable("errors.missing-error-code"))

        then:
        thrown(NoSuchMessageException.class)
    }

    def "message resolvable should have equals implemented"() {
        given:
        def first = new ErrorMessageResolvable("error-code")
        def second = new ErrorMessageResolvable("error-code", ["arg"].toArray())
        def third = new ErrorMessageResolvable("error-code", ["arg"].toArray(), [path: "path"])

        expect:
        verifyAll {
            first != second
            first != third
            second != third

            first == new ErrorMessageResolvable("error-code")
            second == new ErrorMessageResolvable("error-code", ["arg"].toArray())
            third == new ErrorMessageResolvable("error-code", ["arg"].toArray(), [path: "path"])
        }
    }
}
