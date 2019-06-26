package de.ebf.spring.jsonapi.errors.mappings

/**
 * Class that contains the configuration on how should certain exception classes
 * should be resolved using the [de.ebf.spring.jsonapi.errors.resolvers.MappingExceptionResolver].
 *
 * To add a new error mapping you can use the [de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer]
 * interface.
 */
class ErrorMappingRegistry {

    private var mappings = HashMap<Class<out Throwable>, ErrorMappingRegistration>()

    fun register(type: Class<out Throwable>): ErrorMappingRegistration {
        var mapping = mappings[type]

        if (mapping == null) {
            mapping = ErrorMappingRegistration()
            mappings[type] = mapping
        }

        return mapping
    }

    fun get(type: Class<out Throwable>): ErrorMappingRegistration? {
        return mappings[type]
    }

}