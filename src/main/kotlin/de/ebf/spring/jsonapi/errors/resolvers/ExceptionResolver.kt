package de.ebf.spring.jsonapi.errors.resolvers

import org.springframework.core.Ordered

interface ExceptionResolver: Ordered {

    fun resolve(throwable: Throwable): ResolvedException?

}