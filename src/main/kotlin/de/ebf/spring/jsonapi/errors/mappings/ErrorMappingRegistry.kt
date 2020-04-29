package de.ebf.spring.jsonapi.errors.mappings

/**
 * Class that contains the configuration on how should certain exception classes
 * should be resolved using the [de.ebf.spring.jsonapi.errors.resolvers.MappingExceptionResolver].
 *
 * To add a new error mapping you can use the [de.ebf.spring.jsonapi.errors.config.JsonApiErrorConfigurer]
 * interface.
 */
class ErrorMappingRegistry {

    private var mappings = HashMap<Class<out Throwable>, ErrorMappingRegistration<out Throwable>>()

    fun <T: Throwable> register(type: Class<T>): ErrorMappingRegistration<T> {
        var mapping = get(type)

        if (mapping == null) {
            mapping = ErrorMappingRegistration()
            mappings[type] = mapping
        }

        return mapping
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Throwable> get(type: Class<T>): ErrorMappingRegistration<T>? {
        return mappings[type] as ErrorMappingRegistration<T>?
    }

}