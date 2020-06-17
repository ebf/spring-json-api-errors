# spring-json-api-errors
Library that transforms exceptions to JSON API error objects for Spring Boot applications

## Installation

```gradle
implementation("de.ebf:spring-json-api-errors:0.0.5")
```

## Quick Start
To enable JSON API Exception processing you would need to add the following annotation to your application

```java
@EnableJsonApiErrors
@SpringBootApplication
public class MySpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringApplication.class, args);
    }

}
```

Then in your controllers you can add an exception handler that would return the JSON API Errors response:

```java
@RestController
public class Controller {

    @Autowired
    private JsonApiErrorsBuilder builder;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiErrors> handle(Exception e) {
        return builder.build(e);
    }

}
```

As you can see the `EnableJsonApiErrors` annoation would register a few Spring Beans in your application context. Here are the two which are the most important ones:

 - JsonApiErrorsBuilder
 - JsonApiErrorsWriter
 
### JsonApiErrorsBuilder
`JsonApiErrorsBuilder` bean is used to construct the `JsonApiErrors` response entity out of a `Throwable` instance.
It uses a chain of `ExceptionResolver` implementations to resolve the caught exception that would be translated using the
configured `ErrorMessageSource`.

You can customize the `JsonApiErrorsBuilder` using the `JsonApiErrorConfigurer`. Here is a quick example on how you can use the configurer:

```java
@Slf4j
@Configiration
@EnableJsonApiErrors
public class JsonApiErrorConfiguration implements JsonApiErrorConfigurer {
    
    @Override
    public void configure(@NonNull JsonApiErrorsBuilderFactory factory) {
        factory
          .withErrorLogger(JsonApiErrorConfiguration::errorLogger) // add your custom error logger
          .withExceptionResolver(new MyExceptionResolver()) // add your custom exception resolver
          .withMessageBundles("messages/json-api-errors"); // add message bundle locations that would be used for translations
    }
    
    @Override
    public void configure(@NonNull ErrorMappingRegistry registry) {
        // you can also register your custom exception types with an error and status code
        registry.register(TenantCreationFailedException.class)
                .code("errors.inventory.tenant-creation-failed")
                .status(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private static void errorLogger(Throwable e) {
        log.error("An exception occurred while handling request", e);
    }
    
    private static MyExceptionResolver implements ExceptionResolver {
    
        public Integer getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
        
        public @Nullable ResolvedException resolve(Throwable e) {
            /// your custom exception resolver logic
            return new ResolvedException(HttpStatus.METHOD_NOT_ALLOWED, HttpHeaders.EMPTY, List.of(
              new ErrorMessageResolvable("not-found-message")
            ));
        }
    
    }
    
}
```
 
### JsonApiErrorsWriter

This service is responsible for writing the `JsonApiErrors` that are created by the `JsonApiErrorsBuilder`. This Bean is mainly used outside of Spring Controllers, like in Spring Security Exception Handlers, where the servuce needs to render the response manually.

Here is a quick implementation of Spring Security `AccessDeniedHandler` interface:

```java
class JsonApiErrorsAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private JsonApiErrorsBuilder builder;
    
    @Autowired
    private JsonApiErrorsWriter writer;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        writer.write(request, response, builder.build(e));
    }

}
```

### Resolvers

JSON API Errors comes with a list of default error resolver implementations.

#### ResponseStatusExceptionResolver

Implementation that uses Spring's `ResponseStatus` annotation to create a `ResolvedException` by reading the configured HTTP status code and a reason text from the annoation. Reason Text is used a message code for the `MessageSource` service.

```java
@ResponseStatus(code=HttpStatus.INTERNAL_SERVER_ERROR, reason="my-message-code")
class MyException extends RuntimeException {
}
```

#### ResolvableExceptionResolver

With this resolver you can use the `Resolvable` interface in your exceptions to for fine grained configuration.

```java
class MyException extends RuntimeException implements Resolvable {
...
}
```
or use the `JsonApiResolvableException` as a base class:
```java
class MyException extends JsonApiResolvableException {
...
}
```

#### MappingExceptionResolver

Resolver that reads the configuration of exception mappings that were added by the `JsonApiErrorConfigurer`.

```java
@Configiration
@EnableJsonApiErrors
public class JsonApiErrorConfiguration implements JsonApiErrorConfigurer {   
    
    @Override
    public void configure(@NonNull ErrorMappingRegistry registry) {
        registry.register(MyException.class).code("my-message-code").status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
```

#### ValidationExceptionResolver

This resolver implementation handles the following exception types:

 - MethodArgumentNotValidException
 - ConstraintViolationException
 - BindingResult

#### WebServletExceptionResolver

Handles various built in Spring and Servlet exception types:

 - HttpRequestMethodNotSupportedException
 - HttpMediaTypeNotSupportedException
 - HttpMediaTypeNotAcceptableException
 - MissingServletRequestPartException
 - MissingServletRequestParameterException
 - MethodArgumentTypeMismatchException
 - MissingRequestHeaderException
 - MissingRequestCookieException
 - MissingMatrixVariableException
 - NoHandlerFoundException

### Translations
This library depends on Spring's `MessageSource` to translate exception messages to the client. Each exception that is resolved by the `ExceptionResolver` returns a list of `Resolvable` instances. This type contains a unique exception error code that is used to find an apropriate message using the `MessageSource`. Along with the code it can contain additional arguments for message formatting, a default error message and additional error information.



