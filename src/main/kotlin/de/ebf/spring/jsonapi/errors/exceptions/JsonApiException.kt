package de.ebf.spring.jsonapi.errors.exceptions

import java.lang.RuntimeException

/**
 * Exception thrown when something goes when constructing the [de.ebf.spring.jsonapi.errors.JsonApiErrors] object
 */
class JsonApiException: RuntimeException {

    constructor(message: String): super(message)
    constructor(message: String, cause: Throwable): super(message, cause)

}