package de.ebf.spring.jsonapi.errors.writer

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.web.HttpMediaTypeNotAcceptableException
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

class ServletJsonApiErrorsWriter constructor(
    private val converters: HttpMessageConverters
): JsonApiErrorsWriter {

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class, ServletException::class)
    override fun write(
        request: HttpServletRequest,
        response: HttpServletResponse,
        responseEntity: ResponseEntity<JsonApiErrors>
    ) {
        val converter = converters.firstOrNull { converter ->
            converter.canWrite(responseEntity.body!!::class.java, MediaType.APPLICATION_JSON)
        } ?: throw HttpMediaTypeNotAcceptableException(converters.flatMap { converters ->
            converters.supportedMediaTypes
        })

        write(converter as HttpMessageConverter<JsonApiErrors>, responseEntity, response)
    }

    @Throws(IOException::class)
    private fun write(converter: HttpMessageConverter<JsonApiErrors>,
                      entity: ResponseEntity<JsonApiErrors>,
                      response: HttpServletResponse) {

        val message = ServletServerHttpResponse(response)
        message.setStatusCode(entity.statusCode)
        message.headers.addAll(entity.headers)
        message.headers.setCacheControl(CacheControl.noCache())

        converter.write(entity.body!!, MediaType.APPLICATION_JSON, message)
        message.flush()
    }

}