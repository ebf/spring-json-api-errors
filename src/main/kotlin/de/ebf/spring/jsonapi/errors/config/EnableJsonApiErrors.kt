package de.ebf.spring.jsonapi.errors.config

import org.springframework.context.annotation.Import

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Import(JsonApiErrorAutoConfiguration::class)
annotation class EnableJsonApiErrors
