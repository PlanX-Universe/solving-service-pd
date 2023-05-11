package org.planx.solving.messaging.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.planx.solving.exceptions.SolvingError
import org.planx.solving.messaging.functions.getLoggerFor
import org.planx.solving.services.SolvingService
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.planx.common.models.endpoint.solving.UniversalSolvingBody
import org.planx.common.models.endpoint.solving.plan.sequential.SequentialPlan

@Component
@RabbitListener(queues = ["\${planx.queues.request.name}"])
class MainReceiver(
    private var solvingService: SolvingService,
    private val objectMapper: ObjectMapper
) {
    var logger = getLoggerFor<MainReceiver>()

    @RabbitHandler
    fun handleMessage(row: Any) {
        // FIXME: casting it directly leads to an error
        val rawMessage: Message = row as Message
        val body = String(rawMessage.body)
        val message: UniversalSolvingBody = objectMapper.readValue(body, UniversalSolvingBody::class.java)
        logger.info("Request received! \n (RequestID = ${message.requestId})")
        solvingService.solveProblem<SequentialPlan<*>>(
            content = message.content!!,
            requestId = message.requestId,
            callStack = message.callStack
        ).doOnError {
            throw SolvingError(message.requestId, it.message)
        }.subscribe { plan ->
            solvingService.sendPlan(
                requestId = message.requestId,
                callStack = message.callStack,
                plan = plan
            )
        }
    }
}
