package com.example.simulation.breadboard

data class ValidationResult(
  val isValid: Boolean,
  val errors: List<String>,
  val warnings: List<String>
)

class CircuitValidator {
  fun validate(graph: CircuitGraph): ValidationResult {
    val errors = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    val dcSources = graph.components.filter { it.type == "DC_SOURCE" }
    val acSources = graph.components.filter { it.type == "AC_SOURCE" }
    val resistors = graph.components.filter { it.type == "RESISTOR" }
    val leds = graph.components.filter { it.type == "LED" }
    val allSources = dcSources + acSources

    if (allSources.isEmpty()) {
      errors.add("Open Circuit: No Power Source connected.")
    }

    if (leds.isNotEmpty() && resistors.isEmpty()) {
      warnings.add("Missing Resistor: Connecting an LED directly to a source without a current-limiting resistor risks burnout.")
    }

    leds.forEach { led ->
      val anodePin = led.pins.find { it.name == "ANODE" }
      val cathodePin = led.pins.find { it.name == "CATHODE" }
      val isAnodeConnected = anodePin?.let { graph.findNodeConnections(it.id).size > 1 } == true
      val isCathodeConnected = cathodePin?.let { graph.findNodeConnections(it.id).size > 1 } == true
      if (!isAnodeConnected || !isCathodeConnected) {
        errors.add("Open Circuit: LED '${led.id}' is missing connections.")
      }
    }

    allSources.forEach { source ->
      val positivePin = source.pins.find { it.name == "POSITIVE" || it.name == "SIGNAL" }
      val negativePin = source.pins.find { it.name == "NEGATIVE" || it.name == "GND" }

      val positiveConnections = positivePin?.let { graph.findNodeConnections(it.id, traverseComponents = true) } ?: emptyList()
      val negativeConnections = negativePin?.let { graph.findNodeConnections(it.id, traverseComponents = true) } ?: emptyList()

      val intersection = positiveConnections.intersect(negativeConnections.toSet())
      
      if (intersection.isEmpty()) {
         errors.add("Open Circuit: ${source.type} is not fully connected in a closed loop.")
      } else if (resistors.isEmpty() && leds.isEmpty()) {
        errors.add("Short Circuit Detected: Positive terminal connected directly to Negative terminal without load/resistance!")
      }
    }

    return ValidationResult(
      isValid = errors.isEmpty() && allSources.isNotEmpty(),
      errors = errors,
      warnings = warnings
    )
  }
}
