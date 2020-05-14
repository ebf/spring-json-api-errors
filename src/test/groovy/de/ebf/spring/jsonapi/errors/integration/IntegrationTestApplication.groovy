package de.ebf.spring.jsonapi.errors.integration

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder
import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilderFactory
import de.ebf.spring.jsonapi.errors.config.EnableJsonApiErrors
import de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer
import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry
import de.ebf.spring.jsonapi.errors.web.JsonApiHandlerExceptionResolver
import de.ebf.spring.jsonapi.errors.writer.JsonApiErrorsWriter
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * @author : Vladimir Spasic
 * @since : 14.05.20, Thu
 * */
@EnableWebMvc
@Import([
    JsonApiErrorsConfiguration,
    IntegrationTestController
])
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration)
class IntegrationTestApplication implements WebMvcConfigurer {

    @Autowired
    private JsonApiErrorsBuilder jsonApiErrorsBuilder;

    @Autowired
    private JsonApiErrorsWriter jsonApiErrorsWriter;

    @Override
    void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new JsonApiHandlerExceptionResolver(jsonApiErrorsBuilder, jsonApiErrorsWriter))
    }

    @EnableJsonApiErrors
    protected static class JsonApiErrorsConfiguration implements JsonApiErrorConfigurer {

        @Override
        void configure(@NotNull ErrorMappingRegistry registry) {
            registry.register(IllegalArgumentException).code("exception.illegal")
                    .status(HttpStatus.BAD_REQUEST)
        }

        @Override
        void configure(@NotNull JsonApiErrorsBuilderFactory factory) {
            factory.withDefaultErrorMessageCode("exception.default-code")
        }

    }
}
