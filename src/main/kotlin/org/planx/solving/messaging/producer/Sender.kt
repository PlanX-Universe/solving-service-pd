package org.planx.solving.messaging.producer

import org.planx.solving.messaging.functions.getLoggerFor
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.planx.common.models.BaseMessageInterface
import org.planx.common.models.CallStack
import org.planx.common.models.MutableStack
import org.planx.common.models.endpoint.solving.SolvingResultsBody
import org.planx.common.models.endpoint.solving.plan.NoPlanBody
import org.planx.common.models.endpoint.solving.plan.sequential.SequentialPlan
import org.planx.common.models.endpoint.solving.plan.sequential.SequentialPlanBody
import org.planx.common.models.transforming.parsing.pddl.PddlParsingRequest
import org.planx.common.models.transforming.parsing.pddl.PddlParsingRequestBody

@Component
class Sender(var rabbitTemplate: RabbitTemplate) {
    var logger = getLoggerFor<Sender>()

    fun <T> sendPlan(
        plan: T?,
        requestId: String,
        callStack: MutableStack<CallStack>
    ) {
        val responseBody = when (plan) {
            null -> NoPlanBody(
                requestId = requestId,
                callStack = callStack
            )
            is SequentialPlan<*> -> SequentialPlanBody(
                content = plan as SequentialPlan<*>,
                requestId = requestId,
                callStack = callStack
            )
            else -> TODO("Unsupported Plan type!")
        }
        send<SolvingResultsBody<*>>(content = responseBody, addressElement = callStack.peek())
    }

    /**
     * generic send function
     */
    private fun <M> send(content: M, addressElement: CallStack) where M : BaseMessageInterface {
        try {
            logger.info("response send to ${addressElement.topic} (RequestId: ${content.requestId})")
            // is adding "__TypeId__" automatically
            rabbitTemplate.convertAndSend(addressElement.topic, addressElement.routingKey, content)
        } catch (e: AmqpException) {
            if (e.message != null) logger.error(e.message) else logger.error("Unknown AmqpException was thrown!")
        }
    }
}
