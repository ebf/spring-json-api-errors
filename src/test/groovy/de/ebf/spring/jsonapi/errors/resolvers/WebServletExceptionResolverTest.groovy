package de.ebf.spring.jsonapi.errors.resolvers

import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingMatrixVariableException
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException
import spock.lang.Specification
import spock.lang.Unroll

class WebServletExceptionResolverTest extends Specification {

    @Unroll("#throwable = code: #code, status: #status")
    def "should resolve exceptions for #throwable = code: #code, status: #status"() {
        given:
        def resolver = new WebServletExceptionResolver()
        ResolvedException resolvedException

        when:
        resolvedException = resolver.resolve(throwable)

        then:
        verifyAll {
            resolvedException != null
            resolvedException.status == status
            resolvedException.errors[0].code == code
        }

        where:
        throwable                                                             | code                             | status
        new HttpRequestMethodNotSupportedException("POST", ["GET"])                                      | "exception.method-not-supported"       | HttpStatus.METHOD_NOT_ALLOWED
        new HttpMediaTypeNotAcceptableException([MediaType.APPLICATION_JSON])                            | "exception.content-type-not-supported" | HttpStatus.NOT_ACCEPTABLE
        new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, [MediaType.APPLICATION_JSON]) | "exception.content-type-not-supported" | HttpStatus.UNSUPPORTED_MEDIA_TYPE
        new MissingServletRequestPartException("part")                     | "exception.missing_file_parameter" | HttpStatus.BAD_REQUEST
        new MissingServletRequestParameterException("param", "String") | "exception.missing_parameter"       | HttpStatus.BAD_REQUEST
        new MissingRequestCookieException("cookie", mockParameter())         | "exception.missing_cookie"          | HttpStatus.BAD_REQUEST
        new MissingRequestHeaderException("header", mockParameter())         | "exception.missing_header"          | HttpStatus.BAD_REQUEST
        new MissingMatrixVariableException("matrix", mockParameter())        | "exception.missing_matrix_variable" | HttpStatus.BAD_REQUEST
        new NoHandlerFoundException("get", "url", HttpHeaders.EMPTY)         | "exception.not_found"               | HttpStatus.NOT_FOUND

    }

    def mockParameter() {
        def parameter = Mock(MethodParameter)
        parameter.getParameterType() >> String.class
        return parameter
    }
}
