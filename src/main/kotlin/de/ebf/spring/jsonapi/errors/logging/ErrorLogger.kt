package de.ebf.spring.jsonapi.errors.logging

/**
 * Interface that is used to log caught exception
 */
@FunctionalInterface
interface ErrorLogger {

    fun log(throwable: Throwable?)

}