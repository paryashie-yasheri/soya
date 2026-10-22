package itmo.soa.grammy

import itmo.soa.grammy.api.ApiExceptionMapper
import itmo.soa.grammy.api.CorsFilter
import itmo.soa.grammy.api.GrammyResource
import jakarta.ws.rs.core.Application

class GrammyApplication : Application() {
    override fun getSingletons(): Set<Any> {
        return setOf(ApiExceptionMapper(), CorsFilter())
    }

    override fun getClasses(): Set<Class<*>> = setOf(GrammyResource::class.java)
}
