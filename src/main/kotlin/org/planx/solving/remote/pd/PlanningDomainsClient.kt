package org.planx.solving.remote.pd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.planx.solving.messaging.functions.getLoggerFor
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import org.planx.common.models.endpoint.solving.plan.Plan
import org.planx.common.models.endpoint.solving.plan.PlanXAction
import org.planx.common.models.endpoint.solving.plan.sequential.SequentialPlan
import java.util.*

@Component
class PlanningDomainsClient(
    private val webClientBuilder: WebClient.Builder
) {

    var logger = getLoggerFor<PlanningDomainsClient>()
    private val webClient by lazy {
        webClientBuilder
            .baseUrl("http://solver.planning.domains/")
            .build()
    }

    fun <P> getSolution(problem: String, domain: String): Mono<SequentialPlan<PlanXAction>> {
        val body = PlanningDomainsSolvingRequestBody(
            domain = String(Base64.getDecoder().decode(domain)),
            problem = String(Base64.getDecoder().decode(problem))
        )
        logger.info("Send request to 'http://solver.planning.domains'")

        return webClient.post()
            .uri("solve-and-validate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(body))
            .retrieve()
            .bodyToMono(PlanningDomainsResponse::class.java)
            .doOnError { t ->
                // TODO: add error handling!
                logger.error("Failed for some reason", t)
            }
            .map {
                if (it.result != null) {
                    if (it.result.error) {
                        throw Error("Something went wrong while solving!")
                    }
                } else {
                    throw Error("Something went wrong while solving!")
                }
                it
            }
            .map {
                val result: PlanningDomainsResult = it.result!!
                SequentialPlan(
                    cost = result.cost,
                    actions = result.plan.mapIndexed { index, action ->
                        PlanXAction(
                            // TODO: requires parsing
                            // response of API: "(load-truck obj21 tru1 apt1)"
                            name = action.name,
                            cost = 1.0,
                            momentInTime = index
                        )
                    }
                )
            }
    }

    data class PlanningDomainsSolvingRequestBody(
        val domain: String = "",
        val problem: String = ""
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlanningDomainsResponse(
        val status: String = "",
        val result: PlanningDomainsResult? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlanningDomainsResult(
        val parseStatus: String = "",
        val length: Int = 0,
        val plan: List<PlanningDomainsResultPlan> = emptyList(),
        val val_status: String = "",
        val error: Boolean = false,
        val cost: Double = 0.0
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PlanningDomainsResultPlan(
        val action: String = "",
        val name: String = ""
    )
}
