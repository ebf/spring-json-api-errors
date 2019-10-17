package de.ebf.spring.jsonapi.errors.messages

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.context.NoSuchMessageException
import org.springframework.core.Ordered

/**
 * Interface used to find a translation for an exception code represented by [Resolvable] interface
 *
 * @see ErrorMessageResolvable
 */
interface ErrorMessageSource: Ordered {

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