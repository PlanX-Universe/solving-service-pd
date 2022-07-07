package org.planx.solving.remote.pd

import org.planx.solving.messaging.functions.getLoggerFor
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import org.planx.common.models.endpoint.solving.plan.PlanXAction
import org.planx.common.models.endpoint.solving.plan.sequential.SequentialPlan

/**
 * Service implementation for remote API of Planning.Domains
 */
@Service
class PlanningDomainsService(
    private val pdClient: PlanningDomainsClient
) {
    private val logger = getLoggerFor<PlanningDomainsService>()

    fun createPlan(
        problem: String,
        domain: String
    ): Mono<SequentialPlan<PlanXAction>> {
        logger.info("Creating a new Plan")
        return pdClient.getSolution<SequentialPlan<PlanXAction>>(problem, domain)
            .map { plan ->
                plan.actions?.map { action ->
                    val actionCallParameters: List<String> =
                        action.name.substring(1, endIndex = action.name.lastIndex - 1).split("\\s".toRegex())
                    action.name = actionCallParameters.first()
                    action.instantiations = actionCallParameters.subList(1, actionCallParameters.lastIndex)
                    action.parameters = action.instantiations!!.mapIndexed { index, _ -> "p$index" }
                }
                plan
            }
    }
}