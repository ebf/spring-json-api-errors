package de.ebf.spring.jsonapi.errors.resolvers

import de.ebf.spring.jsonapi.errors.messages.Resolvable
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author <a href="mailto:vladimir.spasic@ebf.com">Vladimir Spasic</a>
 * @since 17.10.19, Thu
 **/
class ResolvableExceptionResolver: ExceptionResolver {

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 5
    }

    override fun resolve(throwable: Throwable): ResolvedException? {
        return if (throwable is Resolvable) resolve(throwable as Resolvable) else null
    }

    private fun resolve(resolvable: Resolvable): ResolvedException {
        val annotation = AnnotationUtils.findAnnotation(resolvable.javaClass, ResponseStatus::class.java)

        val status = if (annotation != null) {
            AnnotationUtils.getValue(annotation) as HttpStatus
        } else HttpStatus.INTERNAL_SERVER_ERROR

        return ResolvedException(status = status, errors = listOf(resolvable))
    }
}