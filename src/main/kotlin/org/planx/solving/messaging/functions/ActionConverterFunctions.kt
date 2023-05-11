package org.planx.solving.messaging.functions

import fr.uga.pddl4j.encoding.CodedProblem
import fr.uga.pddl4j.util.BitOp
import org.planx.common.models.endpoint.solving.plan.PlanXAction

/**
 * Converts a bit operation ([BitOp]) to an custom action ([PlanXAction]).
 *
 * @param bitOp of type [BitOp] from PDDL4J Toolkit
 * @param codedProblem of type [CodedProblem]. The encoded problem contains also domain information.
 */
fun bitOp2Action(bitOp: BitOp, codedProblem: CodedProblem, startMoment: Int): PlanXAction {
    val params: MutableList<String> = emptyList<String>().toMutableList()
    val instantiations: MutableList<String> = emptyList<String>().toMutableList()

    // Parameters:
    for (index in 0 until bitOp.arity) {
        val constantKey: Int = bitOp.getTypeOfParameters(index)
        // represents the type of the parameter
        val type: String = codedProblem.types[constantKey]
        params.add(type)
    }

    // Instantiations:
    for (index in 0 until bitOp.arity) {
        val constantKey: Int = bitOp.getValueOfParameter(index)
        val paramInstance: String = codedProblem.constants[constantKey]
        instantiations.add(paramInstance)
    }
// TODO: add other properties if necessary
//
//        // Preconditions:
//        val exp = bitOp.preconditions
//        val str = StringBuilder("(and")
//        val positive: BitSet = exp.positive
//        run {
//            var i: Int = positive.nextSetBit(0)
//            while (i >= 0) {
//                str
//                        .append(" ")
//                        .append("MAKE Something with the relevants")
////                        .append(StringEncoder.toString(relevants.get(i), constants, types, predicates, functions))
//                        .append("\n")
//                i = positive.nextSetBit(i + 1)
//            }
//        }
//        val negative: BitSet = exp.negative
//        run {
//            var i: Int = negative.nextSetBit(0)
//            while (i >= 0) {
//                str
//                        .append(" (not ")
//                        .append("MAKE Something with the relevants")
////                        .append(StringEncoder.toString(relevants.get(i), constants, types, predicates, functions))
//                        .append(")\n")
//                i = negative.nextSetBit(i + 1)
//            }
//        }
//        str.append(")")
//        logger.info(str.toString())

    // Effects:
//        val effects: MutableList<String> = emptyList<String>().toMutableList()
//        bitOp.condEffects.forEach { expression ->
//            if (expression.condition.isEmpty) {
//                // no conditions, just effects
//                // expression.effects
//            } else {
//                // add conditions "(when "
//                // expression.effects
//            }
//        }

    return PlanXAction(
        name = bitOp.name,
        cost = bitOp.cost,
        duration = bitOp.duration,
        parameters = params,
        instantiations = instantiations,
        momentInTime = startMoment,
        // TODO: add preconditions to the model
        preconditions = null,
        effects = null
    )
}