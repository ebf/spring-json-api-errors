package de.ebf.spring.jsonapi.errors.config

import de.ebf.spring.jsonapi.errors.builders.JsonApiErrorsBuilderFactory
import de.ebf.spring.jsonapi.errors.mappings.ErrorMappingRegistry

/**
 * Interface that can be used to configure the JSON API Errors library.
 */
interface JsonApiErrorConfigurer {

    /**
     * Contribute Simple Error mappings to [ErrorMappingRegistry]
     */
    @JvmDefault fun configure(registry: ErrorMappingRegistry) {}

    /**
     * Configure the [JsonApiErrorsBuilderFactory]
     */
    @JvmDefault fun configure(factory: JsonApiErrorsBuilderFactory) {}

}