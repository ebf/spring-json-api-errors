package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.MessageSourceResolvable
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.StaticMessageSource
import spock.lang.Specification

class DefaultErrorMessageSourceTest extends Specification {
    def "should resolve message with no arguments"() {
        given:
        MessageSourceAccessor accessor = Mock()
        def source = new DefaultErrorMessageSource(accessor)
        JsonApiErrors.ErrorMessage message

        when:
        2 * accessor.getMessage(_ as MessageSourceResolvable) >>> ["title", "details"]
        message = source.get(new ErrorMessageResolvable("error-code"))

        then:
        verifyAll {
            message != null
            message.code == "error-code"
            message.title == "title"
            message.detail == "details"
            message.source == [:]
        }
    }

    def "should resolve message with arguments and source"() {
        given:
        MessageSourceAccessor accessor = Mock()
        def source = new DefaultErrorMessageSource(accessor)
        JsonApiErrors.ErrorMessage message

        when:
        2 * accessor.getMessage(_ as MessageSourceResolvable) >>> [null, "details"]
        message = source.get(new ErrorMessageResolvable("error-code", ["argument"].toArray(), [pointer: "/path"]))

        then:
        verifyAll {
            message != null
            message.code == "error-code"
            message.title == null
            message.detail == "details"
            message.source == [pointer: "/path"]
        }
    }

    def "should ignore thrown exception for missing title"() {
        given:
        MessageSourceAccessor accessor = Mock()
        def source = new DefaultErrorMessageSource(accessor)
        JsonApiErrors.ErrorMessage message

        when:
        2 * accessor.getMessage(_ as MessageSourceResolvable) >>> [null, "details"]
        message = source.get(new ErrorMessageResolvable("error-code"))

        then:
        verifyAll {
            message != null
            message.code == "error-code"
            message.detail == "details"
            message.title == null
            message.source == [:]
        }
    }

    def "should not ignore thrown exception for missing details"() {
        given:
        MessageSourceAccessor accessor = Mock()
        def source = new DefaultErrorMessageSource(accessor)

        when:
        2 * accessor.getMessage(_ as MessageSourceResolvable) >> { args ->
            throw new NoSuchMessageException("code")
        }
        source.get(new ErrorMessageResolvable("error-code"))

        then:
        thrown(NoSuchMessageException.class)
    }

    def "message should be formatted"() {
        given:
        def source = new StaticMessageSource()
        source.setAlwaysUseMessageFormat(true)
        source.addMessages([
                "error-code.title": "{0} Title",
                "error-code.message": "Message {2} - {0} - {1}"
        ], LocaleContextHolder.getLocale())

        def errorMessageSource = new DefaultErrorMessageSource(source)
        def resolvable = new ErrorMessageResolvable("error-code", [
                "first", "second", "third"
        ].toArray(), [path: "path"])

        when:
        def message = errorMessageSource.get(resolvable)

        then:
        verifyAll {
            message != null
            message.code == "error-code"
            message.title == "first Title"
            message.detail == "Message third - first - second"
            message.source == [path: "path"]
        }
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
