package engine.parser.models

import DirectedElement

enum class GatewayMerge {
    DIVERGING,
    CONVERGING;

    companion object {
        fun fromGateway(gateway: Gateway): GatewayMerge {
                return GatewayMerge.CONVERGING
        }
    }

}


abstract class Gateway(
    id: String,
    name: String?,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?

) :
    DirectedElement(id, name, outgoing, incoming) {

    val gatewayMerge: GatewayMerge by lazy { GatewayMerge.fromGateway(gateway = this) }

}

data class ExclusiveGateway(
    override val id: String,
    override val name: String?,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
    val default: String?,
) :
    Gateway(id, name, outgoing, incoming)


data class ParallelGateway(
    override val id: String,
    override val name: String?,
    override val outgoing: MutableList<String>?,
    override val incoming: MutableList<String>?,
) :
    Gateway(id, name, outgoing, incoming)