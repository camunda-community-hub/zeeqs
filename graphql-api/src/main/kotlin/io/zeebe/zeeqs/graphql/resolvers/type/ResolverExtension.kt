package io.zeebe.zeeqs.graphql.resolvers.type

import java.time.Instant
import java.time.ZoneId

object ResolverExtension {

    fun timestampToString(timestamp: Long, zoneId: String): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val zone = ZoneId.of(zoneId)
        val zonedDateTime = instant.atZone(zone)
        val offsetDateTime = zonedDateTime.toOffsetDateTime()
        return offsetDateTime.toString()
    }
}