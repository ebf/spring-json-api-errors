package de.ebf.spring.jsonapi.errors.integration

import com.fasterxml.jackson.annotation.JsonCreator
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder
import de.ebf.spring.jsonapi.errors.writer.JsonApiErrorsWriter
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.util.CollectionUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.AsyncRequestTimeoutException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

/**
 * @author : Vladimir Spasic
 * @since : 14.05.20, Thu
 * */
@RestController
class IntegrationTestController {

    @Autowired
    private JsonApiErrorsBuilder builder;

    @Autowired
    private JsonApiErrorsWriter writer;

    @GetMapping("/exception")
    void exception() {
        throw new AccessDeniedException("Access denied", new IllegalStateException("Exception cause"))
    }

    @PostMapping("/model")
    def model(@Validated @RequestBody Model model) {
        return model
    }

    @PostMapping("/constraint-validator")
    def constraintValidator(@RequestBody Model model) {
        def validator = Validation.buildDefaultValidatorFactory().usingContext().validator
        def violations = validator.validate(model)

        if (!CollectionUtils.isEmpty(violations)) {
            throw new ConstraintViolationException(violations)
        }

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

}
