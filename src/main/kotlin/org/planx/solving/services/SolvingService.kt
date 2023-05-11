package org.planx.solving.services

import org.planx.solving.messaging.functions.getLoggerFor
import org.planx.solving.messaging.producer.Sender
import org.planx.solving.remote.pd.PlanningDomainsService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import org.planx.common.models.CallStack
import org.planx.common.models.FunctionalityType
import org.planx.common.models.MutableStack
import org.planx.common.models.endpoint.solving.Language
import org.planx.common.models.endpoint.solving.SolvingRequest
import org.planx.common.models.endpoint.solving.plan.Plan
import org.planx.common.models.endpoint.solving.plan.PlanXAction
import org.planx.common.models.endpoint.solving.plan.sequential.SequentialPlan
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingResponseBody

@Service
class SolvingService(
    private val planningDomainsService: PlanningDomainsService,
    private val sender: Sender
) {
    private val logger = getLoggerFor<SolvingService>()

    fun <P> solveProblem(
        content: SolvingRequest,
        requestId: String,
        callStack: MutableStack<CallStack>
    ): Mono<P> where P : Plan<*> {
        logger.info("Start Planning for $requestId")

        if (!callStack.isEmpty()) {
            // remove current step
            callStack.pop()
        }

        return solve(
            content.problem!!,
            content.domain!!,
            lang = content.language
        )
    }

    /**
     * @param problem is a base64 encoded [String]
     * @param domain is a base64 encoded [String]
     * @param lang is the language of the Problem and the Domain as [Language].
     *        The default value for lang is Language.PDDL
     */
    private fun <P> solve(
        problem: String,
        domain: String,
        lang: Language? = Language.PDDL
    ): Mono<P> where P : Plan<*> {
        logger.info("Request parsing functionality")
        return when (lang) {
            Language.PDDL -> planningDomainsService.createPlan(problem, domain).map {
                it as P
            }
            null -> throw Exception("Illegal language exception!")
            else -> TODO("Not implemented on the prototype")
        }
    }

    /**
     * sends response of planning
     */
    fun <T> sendPlan(plan: T?, callStack: MutableStack<CallStack>, requestId: String) where T : Plan<*> {
        sender.sendPlan(plan, requestId, callStack)
    }
}
