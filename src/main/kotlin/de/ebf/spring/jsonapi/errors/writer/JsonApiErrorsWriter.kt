package de.ebf.spring.jsonapi.errors.writer

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.http.ResponseEntity
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

interface JsonApiErrorsWriter {

    @Throws(IOException::class, ServletException::class)
    fun write(request: HttpServletRequest, response: HttpServletResponse, responseEntity: ResponseEntity<JsonApiErrors>)

}