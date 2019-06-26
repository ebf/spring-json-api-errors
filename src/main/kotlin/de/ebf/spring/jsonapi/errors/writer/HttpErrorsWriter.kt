package de.ebf.spring.jsonapi.errors.writer

import de.ebf.spring.jsonapi.errors.JsonApiErrors
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface HttpErrorsWriter {

    fun write(request: HttpServletRequest, response: HttpServletResponse, responseEntity: ResponseEntity<JsonApiErrors>)

}