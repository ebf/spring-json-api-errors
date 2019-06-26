package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

/**
 * Data holder that encapsulates the information needed to build [de.ebf.spring.jsonapi.errors.JsonApiErrors]
 */
data class ResolvedException (
    val status: HttpStatus,
    val headers: HttpHeaders? = HttpHeaders.EMPTY,
    val errors: Collection<ErrorMessageResolvable>
)