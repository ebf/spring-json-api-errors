package de.ebf.spring.jsonapi.errors

import com.fasterxml.jackson.annotation.JsonCreator
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilderFactory
import de.ebf.spring.jsonapi.errors.config.EnableJsonApiErrors
import de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer
import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry
import de.ebf.spring.jsonapi.errors.messages.DefaultErrorMessageSource
import de.ebf.spring.jsonapi.errors.writer.HttpErrorsWriter
import groovy.json.JsonOutput
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import org.springframework.core.Ordered
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

@SpringBootTest(classes = IntegrationTestApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest extends Specification {

    @EnableWebMvc
    @EnableJsonApiErrors
    @Import(IntegrationTestController)
    @EnableAutoConfiguration(exclude = SecurityAutoConfiguration)
    static class IntegrationTestApplication implements JsonApiErrorConfigurer {

        @Override
        void configure(@NotNull ErrorMappingRegistry registry) {
            registry.register(IllegalArgumentException).code("exception.illegal")
                    .status(HttpStatus.BAD_REQUEST)
        }

        @Override
        void configure(@NotNull JsonApiErrorsBuilderFactory factory) {
            def source = new StaticMessageSource()
            source.addMessages([
                    "email.missing": "Empty email address",
                    "exception.default-code": "Default message",
                    "exception.illegal.title": "Error title",
                    "exception.illegal.message": "Error message",
                    "exception.async_timeout": "Request timeout",
                    "exception.access_denied": "Denied"
            ], LocaleContextHolder.getLocale())

            factory.withErrorMessageSource(new DefaultErrorMessageSource(source, Ordered.HIGHEST_PRECEDENCE))
                .withDefaultErrorMessageCode("exception.default-code")
        }
    }

    @RestController
    static class IntegrationTestController {

        @Autowired
        private JsonApiErrorsBuilder builder

        @Autowired
        private HttpErrorsWriter writer

        @GetMapping("/exception")
        void exception() {
            throw new AccessDeniedException("Access denied")
        }

        @PostMapping("/model")
        def model(@Validated @RequestBody Model model) {
            return model
        }

        @GetMapping("/writer")
        def writer(HttpServletRequest request, HttpServletResponse response) {
            writer.write(request, response, builder.build(new AsyncRequestTimeoutException()))
        }

        @GetMapping("/parameter")
        def parameter(@RequestParam("parameter") Integer param) {
            return param
        }

        @ExceptionHandler(Throwable.class)
        def handler(Throwable throwable) {
            return builder.build(throwable)
        }

    }

    static class Model {

        @NotEmpty
        String username

        @Valid
        @NotNull
        InnerModel inner

        @JsonCreator Model() {
            username = null
            inner = new InnerModel()
        }

        class InnerModel {
            @Email
            @NotEmpty(message = "{email.missing}")
            String email

            @JsonCreator InnerModel() {
                email = null
            }
        }
    }

    @LocalServerPort
    protected Integer port

    @Autowired
    private TestRestTemplate template

    @Autowired
    private JsonApiErrorsBuilder builder

    ResponseEntity<String> get(String path) {
        return template.getForEntity("http://localhost:${port}/${path}", String)
    }

    def "builder should be able to resolve right error message"() {
        when:
        def res = builder.build(new IllegalArgumentException())

        then:
        verifyAll {
            res != null
            res.statusCode == HttpStatus.BAD_REQUEST
            res.body.errors[0].code == "exception.illegal"
            res.body.errors[0].title == "Error title"
            res.body.errors[0].detail == "Error message"
            res.body.errors[0].source == null
        }
    }

    def "exception should be handled by the exception handler annotation"() {
        when:
        def result = get("/exception")

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.FORBIDDEN
            result.body == "{\"errors\":[{\"code\":\"exception.access_denied\",\"detail\":\"Denied\"}]}"
        }
    }

    def "model should be validated"() {
        when:
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def result = template.postForEntity("http://localhost:${port}/model",
                new HttpEntity<>(new Model(), headers), String)

        println(result.body)

        then:
        verifyAll {
            result.hasBody()
            result.statusCode == HttpStatus.UNPROCESSABLE_ENTITY
            result.body == JsonOutput.toJson([errors: [
                    [code: "NotEmpty", title: "must not be empty", detail: "must not be empty", source: [pointer: "username"]],
                    [code: "email.missing", detail: "Empty email address", source: [pointer: "inner/email"]]
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
            result.body == "{\"errors\":[{\"code\":\"exception.async_timeout\",\"detail\":\"Request timeout\"}]}"
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