package de.ebf.spring.jsonapi.errors.integration

import com.fasterxml.jackson.databind.ObjectMapper
import de.ebf.spring.jsonapi.errors.JsonApiErrors
import de.ebf.spring.jsonapi.errors.builders.DefaultJsonApiErrorsBuilder
import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import spock.lang.Specification

@SpringBootTest(classes = IntegrationTestApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest extends Specification {

    @LocalServerPort
    protected Integer port

    @Autowired
    private ObjectMapper mapper

    @Autowired
    private TestRestTemplate template

    @Autowired
    private DefaultJsonApiErrorsBuilder builder

    ResponseEntity<String> get(String path) {
        return template.getForEntity("http://localhost:${port}/${path}", String)
    }

    def setup() {
        builder.includeStackTrace = false
    }

    def "builder should be able to resolve right error message"() {
        when:
        def res = builder.build(new IllegalArgumentException())

        then:
        verifyAll {
            res != null
            res.statusCode == HttpStatus.BAD_REQUEST
            res.body.stackTrace == null
            res.body.errors[0].code == "exception.illegal"
            res.body.errors[0].title == "Error title"
            res.body.errors[0].detail == "Error message"
            res.body.errors[0].source == [:]
        }
    }

    def "builder should be able to resolve right error message and include stack trace"() {
        given:
        def exception = new IllegalArgumentException("Got an exception", new IllegalStateException("Exception cause"))
        builder.includeStackTrace = true

        def writer = new StringWriter()
        exception.printStackTrace(new PrintWriter(writer, true))

        when:
        def res = builder.build(exception)

        then:
        verifyAll {
            res != null
            res.statusCode == HttpStatus.BAD_REQUEST
            res.body.stackTrace == writer.buffer.toString()
            res.body.errors.first().code == "exception.illegal"
            res.body.errors.first().title == "Error title"
            res.body.errors.first().detail == "Error message"
            res.body.errors.first().source == [:]
        }
    }

    def "exception should be handled by the exception handler annotation"() {
        when:
        def result = get("/exception")
        def response = mapper.readValue(result.body, JsonApiErrors)

        then:

        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.FORBIDDEN
            response.stackTrace == null
            response.errors.size() == 1
            response.errors.first().code == "exception.access_denied"
            response.errors.first().title == "Access denied"
            response.errors.first().detail == "It seems you do not have enough permissions to perform this action"
            response.errors.first().source == null
        }
    }

    def "should include formatted stack trace"() {
        given:
        builder.includeStackTrace = true

        when:
        def result = get("/exception")
        def response = mapper.readValue(result.body, JsonApiErrors)

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.FORBIDDEN
            response.stackTrace.startsWith("org.springframework.security.access.AccessDeniedException: Access denied")
            response.stackTrace.contains("Caused by: java.lang.IllegalStateException: Exception cause")
            response.errors.size() == 1
            response.errors.first().code == "exception.access_denied"
            response.errors.first().title == "Access denied"
            response.errors.first().detail == "It seems you do not have enough permissions to perform this action"
            response.errors.first().source == null
        }
    }

    def "model should be validated"() {
        when:
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def result = template.postForEntity("http://localhost:${port}/model",
                new HttpEntity<>(new IntegrationTestController.Model(), headers), String)

        println(result)

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
            result.body == JsonOutput.toJson([errors: [
                    [code: "NotEmpty", detail: "must not be empty", source: [pointer: "username"]],
                    [code: "email.missing", detail: "Empty email address", source: [pointer: "inner/email"]]
             ]])
        }
    }

    def "model should be validated by javax.validation.Validator"() {
        when:
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def result = template.postForEntity("http://localhost:${port}/constraint-validator",
                new HttpEntity<>(new IntegrationTestController.Model(), headers), String)

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
            result.body == JsonOutput.toJson([errors: [
                    [code: "email.missing", detail: "Empty email address", source: [pointer: "inner/email"]],
                    [code: "javax.validation.constraints.NotEmpty.message", detail: "must not be empty", source: [pointer: "username"]],
            ]])
        }
    }

    def "exception should be writen by the error writer"() {
        when:
        def result = get("/writer")

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.SERVICE_UNAVAILABLE
            result.body == "{\"errors\":[{\"code\":\"exception.async_timeout\",\"title\":\"Service Unavailable\"," +
                    "\"detail\":\"Could not connect to server, please contact the administrator or try again later.\"}]}"
        }
    }

    def "missing parameter exception should be handled by the exception handler annotation"() {
        when:
        def result = get("/parameter")

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.BAD_REQUEST
            result.body == "{\"errors\":[{\"code\":\"exception.missing_parameter\",\"title\":\"Bad request\"," +
                    "\"detail\":\"Request parameter with name parameter is missing\"," +
                    "\"source\":{\"parameter\":\"parameter\"}}]}"
        }
    }

    def "conversion parameter exception should be handled by the exception handler annotation"() {
        when:
        def result = get("/parameter?parameter=invalidNumber")

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.BAD_REQUEST
            result.body == "{\"errors\":[{\"code\":\"exception.invalid_parameter\",\"title\":\"Bad request\"," +
                    "\"detail\":\"Invalid request parameter value for parameter parameter\"," +
                    "\"source\":{\"parameter\":\"parameter\"}}]}"
        }
    }
}