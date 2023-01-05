package org.opensearch.simpleschema.domain

import java.util.concurrent.ConcurrentHashMap

object DomainRepository {
    var domains: ConcurrentHashMap<String, Domain> = ConcurrentHashMap()

    /**
     * Adds a [domain] to the repository if the name is not already present,
     * otherwise return the existing domain.
     *
     * @return The domain associated with the same name if already present, otherwise null.
     */
    fun createDomain(domain: Domain): Domain? {
        return domains.putIfAbsent(domain.name, domain)
    }

    fun getDomain(name: String): Domain? {
        return domains[name]
    }
}