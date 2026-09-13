package com.example.simulation.breadboard

data class OhmsLawResult(
  val currentAmps: Double,
  val currentMa: Double,
  val feedbackString: String
)

fun calculateOhmsLaw(voltageVolts: Double, resistanceOhms: Double): OhmsLawResult {
  if (resistanceOhms <= 0.0) {
    return OhmsLawResult(
      currentAmps = Double.POSITIVE_INFINITY,
      currentMa = Double.POSITIVE_INFINITY,
      feedbackString = "I = $voltageVolts / 0 → Infinite Current (Short Circuit Warning!)"
    )
  }

  val currentAmps = voltageVolts / resistanceOhms
  val currentMa = currentAmps * 1000.0
  val formattedMa = String.format("%.1f", currentMa)

  val feedback = "I = $voltageVolts V / $resistanceOhms Ω ≈ $formattedMa mA"

  return OhmsLawResult(
    currentAmps = currentAmps,
    currentMa = currentMa,
    feedbackString = feedback
  )
}
