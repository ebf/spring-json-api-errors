package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.messages.ErrorMessageResolvable
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of the [ExceptionResolver] interface that uses the
 * Spring's [ResponseStatus] annotation to resolve the error code and status code
 * for the caught exception.
 */
class ResponseStatusExceptionResolver: ExceptionResolver {

    private var cache = ConcurrentHashMap<Class<in Throwable>, ResolvedException>()

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 3
    }

    override fun resolve(throwable: Throwable): ResolvedException? {
        var resolved = cache[throwable.javaClass]

        if (resolved == null) {
            resolved = fromAnnotation(throwable)
        }

        if (resolved == null) {
            return null
        }

        return resolved
    }

    private fun fromAnnotation(throwable: Throwable): ResolvedException? {
        val annotation = AnnotationUtils.findAnnotation(throwable.javaClass, ResponseStatus::class.java)

        if (annotation != null) {
            val status = AnnotationUtils.getValue(annotation) as HttpStatus
            val code = AnnotationUtils.getValue(annotation,"reason") as String
            val resolved = ResolvedException(status = status, errors = listOf(ErrorMessageResolvable(code)))

            cache[throwable.javaClass] = resolved
            return resolved
        }

        return null
    }
}