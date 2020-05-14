package de.ebf.spring.jsonapi.errors.web

import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilder
import de.ebf.spring.jsonapi.errors.writer.JsonApiErrorsWriter
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Implementation of [HandlerExceptionResolver] that uses [JsonApiErrorsBuilder] to construct
 * the JSON API Error response and flushes it using [JsonApiErrorsWriter].
 *
 * @author : Vladimir Spasic
 * @since : 14.05.20, Thu
 **/
class JsonApiHandlerExceptionResolver(
        private val builder: JsonApiErrorsBuilder,
        private val writer: JsonApiErrorsWriter
): HandlerExceptionResolver, Ordered {

    private val logger = LoggerFactory.getLogger(JsonApiHandlerExceptionResolver::class.java)

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override fun resolveException(request: HttpServletRequest, response: HttpServletResponse, handler: Any?, ex: Exception): ModelAndView? {
        try {
            val entity = builder.build(ex) ?: return null
            writer.write(request, response, entity)
        } catch (e: IOException) {
            logger.warn("An error occurred while handling exception", e)
            return null
        }
        return ModelAndView()
    }

}