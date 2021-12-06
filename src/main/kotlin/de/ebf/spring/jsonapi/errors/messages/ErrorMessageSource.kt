package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.NoSuchMessageException

/**
 * Interface used to find a translation for an exception code represented by [Resolvable] interface
 *
 * @see ErrorMessageResolvable
 */
@FunctionalInterface
interface ErrorMessageSource {

    /**
     * Tries to create an {@link ErrorMessage} instance based on the resolved Error
     * message code and arguments.
     *
     * @param resolvable Error Message resolvable instance
     * @return Error message for the caught exception or a default error message
     * @throws NoSuchMessageException when error message is not found for the given arguments
     */
    fun get(resolvable: Resolvable): JsonApiErrors.ErrorMessage

}