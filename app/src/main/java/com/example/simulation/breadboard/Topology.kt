package com.example.simulation.breadboard

data class BBComponent(
  val id: String,
  val type: String, // "DC_SOURCE", "RESISTOR", "LED"
  val value: Double, // e.g., 5.0 Volts, 220.0 Ohms
  val pins: List<BBPin>
)

data class BBPin(
  val id: String,
  val componentId: String,
  val name: String,
  val connectedNodeId: String? = null
)

data class BBWire(
  val id: String,
  val fromPinId: String,
  val toPinId: String
)

data class CircuitGraph(
  val components: List<BBComponent>,
  val wires: List<BBWire>
) {
  fun findNodeConnections(pinId: String, traverseComponents: Boolean = false): List<String> {
    val connectedPins = mutableSetOf<String>()
    
    fun traverse(currentPinId: String) {
      if (!connectedPins.add(currentPinId)) return // Already visited
      
      wires.forEach { wire ->
        if (wire.fromPinId == currentPinId) traverse(wire.toPinId)
        if (wire.toPinId == currentPinId) traverse(wire.fromPinId)
      }
      
      if (traverseComponents) {
        val comp = components.find { c -> c.pins.any { p -> p.id == currentPinId } }
        comp?.pins?.forEach { pin ->
          if (pin.id != currentPinId) {
            traverse(pin.id)
          }
        }
      }
    }
    
    traverse(pinId)
    return connectedPins.toList()
  }
}
