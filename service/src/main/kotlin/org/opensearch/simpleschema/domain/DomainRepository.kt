package org.opensearch.simpleschema.domain

import org.opensearch.simpleschema.util.logger
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap

object DomainRepository {
    private val log by logger(DomainRepository::class.java)
    var domains: ConcurrentHashMap<String, DomainResource> = ConcurrentHashMap()

    /**
     * Adds a [domain] to the repository if the name is not already present,
     * otherwise throws an IllegalArgumentException
     */
    fun createDomain(domain: DomainResource) {
        if (domains.containsKey(domain.name)) {
            throw IllegalArgumentException("Attempted to create duplicate domain `${domain.name}`")
        }
        domains[domain.name] = domain
    }

    fun getDomain(name: String): DomainResource? {
        return domains[name]
    }
}
