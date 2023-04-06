package io.zeebe.zeeqs.graphql.resolvers.query

import io.zeebe.zeeqs.data.entity.Signal
import io.zeebe.zeeqs.data.repository.SignalRepository
import io.zeebe.zeeqs.graphql.resolvers.connection.SignalConnection
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class SignalQueryResolver(
    val signalRepository: SignalRepository
) {

    @QueryMapping
    fun signals(
        @Argument perPage: Int,
        @Argument page: Int
    ): SignalConnection {
        return SignalConnection(
            getItems = {
                signalRepository.findAll(PageRequest.of(page, perPage)).toList()
            },
            getCount = {
                signalRepository.count()
            }
        )
    }

    @QueryMapping
    fun signal(@Argument key: Long): Signal? {
        return signalRepository.findByIdOrNull(key)
    }

}