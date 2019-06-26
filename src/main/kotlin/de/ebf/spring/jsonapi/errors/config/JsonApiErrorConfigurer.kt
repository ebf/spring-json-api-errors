package de.ebf.spring.jsonapi.errors.config

import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilderFactory
import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry

interface JsonApiErrorConfigurer {

    fun configure(registry: ErrorMappingRegistry) {}

    fun configure(factory: JsonApiErrorsBuilderFactory) {}

}