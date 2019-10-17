package de.ebf.spring.jsonapi.errors.logging

/**
 * Interface that is used to log caught exception that are processed by the library.
 */
@FunctionalInterface
interface ErrorLogger {

    fun log(throwable: Throwable?)

}