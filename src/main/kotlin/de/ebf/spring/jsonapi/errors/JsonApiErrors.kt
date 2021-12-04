package de.ebf.spring.jsonapi.errors

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 * Data model used to structure the error response according to the JSON API specification.
 *
 * Response must contain a list of errors where each should contain an error code and
 * error details with an option title and source object.
 *
 * This class is generated using the [de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder]
 * interface. You can configure the behaviour using the [de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilderFactory]
 * and [de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer]
 */
data class JsonApiErrors @JsonCreator constructor(
    @field:JsonProperty val errors: Collection<ErrorMessage>,
    @field:JsonProperty @field:JsonInclude(NON_NULL) val stackTrace: String? = null
): Serializable {

    constructor(errors: Collection<ErrorMessage>): this(errors, null)

    @JsonInclude(NON_EMPTY)
    data class ErrorMessage @JsonCreator constructor(
        var code: String,
        var title: String?,
        var detail: String?,
        var source: Map<String, Any>? = null
    ): Serializable
}