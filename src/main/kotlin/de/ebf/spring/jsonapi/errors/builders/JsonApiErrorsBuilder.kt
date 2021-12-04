package de.ebf.spring.jsonapi.errors.builders

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.http.ResponseEntity

/**
 * Interface used to construct a [ResponseEntity] to be sent to the client
 * when an exception occurs.
 *
 * Response entity contains a list of [JsonApiErrors] in accordance to
 * JSON API Specification.
 */
interface JsonApiErrorsBuilder {

    /**
     * Create an [ResponseEntity] for the given Exception
     *
     * @param throwable Caught exception
     * @return Response entity with an appropriate status code, headers and error messages
     * @throws de.ebf.spring.jsonapi.errors.exceptions.JsonApiException when an error occurs
     *      during the build process
     */
    fun build(throwable: Throwable): ResponseEntity<JsonApiErrors>

}