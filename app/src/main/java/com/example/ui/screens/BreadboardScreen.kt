package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simulation.breadboard.*
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*
import com.example.util.triggerHapticFeedback
import kotlin.math.sin

fun getPinOffset(comp: BBComponent, pin: BBPin, compPos: Offset): Offset {
    return when(comp.type) {
        "DC_SOURCE", "AC_SOURCE" -> if (pin.name == "POSITIVE" || pin.name == "SIGNAL") Offset(compPos.x, compPos.y - 30f) else Offset(compPos.x, compPos.y + 30f)
        "RESISTOR" -> if (pin.name == "PIN1") Offset(compPos.x - 40f, compPos.y) else Offset(compPos.x + 40f, compPos.y)
        "LED" -> if (pin.name == "ANODE") Offset(compPos.x - 25f, compPos.y) else Offset(compPos.x + 25f, compPos.y)
        else -> compPos
    }
}

@Composable
fun BreadboardScreen(onBack: () -> Unit) {
  val context = LocalContext.current
  var zoomLevel by remember { mutableStateOf(1.0f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }
  var phase by remember { mutableStateOf(0f) }
  LaunchedEffect(Unit) {
      while(true) {
          kotlinx.coroutines.delay(16)
          phase += 0.1f
      }
  }
  var selectedPin by remember { mutableStateOf<BBPin?>(null) }

  val components = remember {
    mutableStateListOf(
      BBComponent("src1", "DC_SOURCE", 5.0, listOf(BBPin("p1", "src1", "POSITIVE"), BBPin("p2", "src1", "NEGATIVE"))),
      BBComponent("res1", "RESISTOR", 220.0, listOf(BBPin("p3", "res1", "PIN1"), BBPin("p4", "res1", "PIN2"))),
      BBComponent("led1", "LED", 2.0, listOf(BBPin("p5", "led1", "ANODE"), BBPin("p6", "led1", "CATHODE")))
    )
  }

  val wires = remember {
    mutableStateListOf<BBWire>()
  }

  val compPositions = remember {
    mutableStateMapOf(
      "src1" to Offset(150f, 200f),
      "res1" to Offset(400f, 200f),
      "led1" to Offset(650f, 200f)
    )
  }

  val validator = remember { CircuitValidator() }
  val graph = remember(components, wires.toList()) { CircuitGraph(components, wires) }
  val validationResult = remember(graph) { validator.validate(graph) }
  
  // Calculate voltage dynamically if DC
  val totalRes = components.filter { it.type == "RESISTOR" }.sumOf { it.value }
  val totalV = components.filter { it.type == "DC_SOURCE" }.sumOf { it.value }
  val ohmsResult = remember(totalRes, totalV, validationResult) { 
      if (validationResult.isValid) calculateOhmsLaw(totalV, totalRes)
      else OhmsLawResult(0.0, 0.0, "Circuit Open")
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    NeoHeader(
      title = "Interactive Sandbox",
      subtitle = "Wire components directly. Tap two pins to connect."
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeoButton(text = "-", buttonType = com.example.ui.components.NeoButtonType.ZOOM_OUT, onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.6f) })
        NeoButton(text = "${(zoomLevel * 100).toInt()}%", onClick = { zoomLevel = 1.0f })
        NeoButton(text = "+", buttonType = com.example.ui.components.NeoButtonType.ADD, onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.0f) })
      }
      NeoButton(text = "CLEAR WIRES", onClick = { wires.clear(); selectedPin = null; triggerHapticFeedback(context, 40) })
    }

    NeoCard(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      backgroundColor = Color(0xFF1E293B)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val clickOffset = Offset((offset.x - panOffset.x) / zoomLevel, (offset.y - panOffset.y) / zoomLevel)
                    var tappedPin: BBPin? = null
                    // Check if tapped on a pin
                    for (comp in components) {
                        val cPos = compPositions[comp.id] ?: Offset.Zero
                        for (pin in comp.pins) {
                            val pOffset = getPinOffset(comp, pin, cPos)
                            if (Math.abs(clickOffset.x - pOffset.x) < 30f && Math.abs(clickOffset.y - pOffset.y) < 30f) {
                                tappedPin = pin
                                break
                            }
                        }
                        if (tappedPin != null) break
                    }
                    
                    if (tappedPin != null) {
                        triggerHapticFeedback(context, 30)
                        if (selectedPin == null) {
                            selectedPin = tappedPin
                        } else if (selectedPin == tappedPin) {
                            selectedPin = null // Deselect
                        } else {
                            // Connect
                            wires.add(BBWire("w_${System.currentTimeMillis()}", selectedPin!!.id, tappedPin.id))
                            selectedPin = null
                        }
                    }
                }
            )
          }
          .pointerInput(Unit) {
            var draggedComponentId: String? = null
            detectDragGestures(
              onDragStart = { offset ->
                val clickOffset = Offset((offset.x - panOffset.x) / zoomLevel, (offset.y - panOffset.y) / zoomLevel)
                draggedComponentId = components.find { comp ->
                  val pos = compPositions[comp.id] ?: Offset.Zero
                  Math.abs(clickOffset.x - pos.x) < 50f && Math.abs(clickOffset.y - pos.y) < 50f
                }?.id
              },
              onDragEnd = { draggedComponentId = null },
              onDragCancel = { draggedComponentId = null }
            ) { change, dragAmount ->
              change.consume()
              if (draggedComponentId != null) {
                val current = compPositions[draggedComponentId] ?: Offset.Zero
                compPositions[draggedComponentId!!] = Offset(current.x + dragAmount.x / zoomLevel, current.y + dragAmount.y / zoomLevel)
              } else {
                panOffset += dragAmount
              }
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val step = 30.dp.toPx() * zoomLevel
          val cols = 20
          val rows = 20
          for (i in -cols until cols) {
            for (j in -rows until rows) {
              drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                radius = 2.dp.toPx(),
                center = Offset(i * step + panOffset.x % step, j * step + panOffset.y % step)
              )
            }
          }

          // Draw Wires
          wires.forEach { wire ->
            val fromComp = components.find { c -> c.pins.any { p -> p.id == wire.fromPinId } }
            val toComp = components.find { c -> c.pins.any { p -> p.id == wire.toPinId } }
            
            if (fromComp != null && toComp != null) {
               val p1 = compPositions[fromComp.id] ?: Offset.Zero
               val p2 = compPositions[toComp.id] ?: Offset.Zero
               
               val fromPin = fromComp.pins.find { it.id == wire.fromPinId }!!
               val toPin = toComp.pins.find { it.id == wire.toPinId }!!
               
               val fromOffset = getPinOffset(fromComp, fromPin, p1)
               val toOffset = getPinOffset(toComp, toPin, p2)
               
               val scaledP1 = Offset(fromOffset.x * zoomLevel + panOffset.x, fromOffset.y * zoomLevel + panOffset.y)
               val scaledP2 = Offset(toOffset.x * zoomLevel + panOffset.x, toOffset.y * zoomLevel + panOffset.y)
               
               drawLine(
                 color = TechBlue,
                 start = scaledP1,
                 end = scaledP2,
                 strokeWidth = 4.dp.toPx() * zoomLevel
               )
            }
          }

          // Draw Components
          components.forEach { comp ->
            val basePos = compPositions[comp.id] ?: Offset(100f, 100f)
            val pos = Offset(basePos.x * zoomLevel + panOffset.x, basePos.y * zoomLevel + panOffset.y)
            
            when (comp.type) {
               "DC_SOURCE" -> {
                 drawRect(
                   color = DangerRed,
                   topLeft = Offset(pos.x - 30f * zoomLevel, pos.y - 30f * zoomLevel),
                   size = androidx.compose.ui.geometry.Size(60f * zoomLevel, 30f * zoomLevel)
                 )
                 drawRect(
                   color = Ink,
                   topLeft = Offset(pos.x - 30f * zoomLevel, pos.y),
                   size = androidx.compose.ui.geometry.Size(60f * zoomLevel, 30f * zoomLevel)
                 )
               }
               "AC_SOURCE" -> {
                   drawCircle(
                       color = TechBlue,
                       radius = 30f * zoomLevel,
                       center = pos
                   )
                   // Draw sine wave inside
                   val path = Path()
                   val width = 40f * zoomLevel
                   val dx = width / 20
                   for (i in 0..20) {
                       val x = pos.x - width/2 + i * dx
                       val y = pos.y - (sin(i / 20f * 2 * Math.PI + phase) * 15f * zoomLevel).toFloat()
                       if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                   }
                   drawPath(path, color = Color.White, style = Stroke(width = 3f * zoomLevel))
               }
               "RESISTOR" -> {
                 drawRect(
                   color = Color(0xFFCD853F),
                   topLeft = Offset(pos.x - 40f * zoomLevel, pos.y - 15f * zoomLevel),
                   size = androidx.compose.ui.geometry.Size(80f * zoomLevel, 30f * zoomLevel)
                 )
               }
               "LED" -> {
                 drawCircle(
                   color = if (validationResult.isValid) SafeGreen else Color.DarkGray,
                   radius = 25f * zoomLevel,
                   center = pos
                 )
                 if (validationResult.isValid) {
                     drawCircle(
                         color = SafeGreen.copy(alpha = 0.4f),
                         radius = 35f * zoomLevel,
                         center = pos
                     )
                 }
               }
            }
            
            // Draw Pins
            comp.pins.forEach { pin ->
                val pOff = getPinOffset(comp, pin, basePos)
                val scaledOff = Offset(pOff.x * zoomLevel + panOffset.x, pOff.y * zoomLevel + panOffset.y)
                drawCircle(
                    color = if (selectedPin == pin) AmberAccent else Color.White,
                    radius = 8f * zoomLevel,
                    center = scaledOff
                )
                drawCircle(
                    color = Ink,
                    radius = 8f * zoomLevel,
                    center = scaledOff,
                    style = Stroke(width = 2f * zoomLevel)
                )
            }
          }
        }
        
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(8.dp)
        ) {
          Text(
            text = if (validationResult.isValid) "STATUS: VALID (CLOSED LOOP)" else "STATUS: ERROR / OPEN",
            color = if (validationResult.isValid) SafeGreen else DangerRed,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
          )
          Text(
            text = ohmsResult.feedbackString,
            color = Color.White,
            fontSize = 10.sp
          )
          if (components.any { it.type == "AC_SOURCE" } && validationResult.isValid) {
              Text(text = "Signal: AC Waveform Active", color = TechBlue, fontSize = 10.sp)
          }
        }
      }
    }

    NeoCard(backgroundColor = Color.White) {
      Text(text = "TOPOLOGY VALIDATION ENGINE", fontSize = 12.sp, fontWeight = FontWeight.Black)
      Spacer(modifier = Modifier.height(4.dp))
      if (validationResult.isValid) {
        Text(text = "Circuit is correctly wired! Current is flowing.", color = SafeGreen, fontSize = 13.sp)
      } else {
        validationResult.errors.forEach { err ->
          Text(text = "• $err", color = DangerRed, fontSize = 12.sp)
        }
        if (validationResult.errors.isEmpty() && !validationResult.isValid) {
             Text(text = "• Connect the components to form a closed loop.", color = DangerRed, fontSize = 12.sp)
        }
      }
      validationResult.warnings.forEach { warn ->
        Text(text = "⚠ $warn", color = AmberAccent, fontSize = 12.sp)
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      NeoButton(
        text = "+ Resistor",
        onClick = {
          triggerHapticFeedback(context, 40)
          val id = "res_${System.currentTimeMillis()}"
          components.add(BBComponent(id, "RESISTOR", 330.0, listOf(BBPin("p1_$id", id, "PIN1"), BBPin("p2_$id", id, "PIN2"))))
          compPositions[id] = Offset(200f - panOffset.x, 200f - panOffset.y)
        },
        modifier = Modifier.weight(1f)
      )
      NeoButton(
        text = "+ LED",
        onClick = {
          triggerHapticFeedback(context, 40)
          val id = "led_${System.currentTimeMillis()}"
          components.add(BBComponent(id, "LED", 2.0, listOf(BBPin("p1_$id", id, "ANODE"), BBPin("p2_$id", id, "CATHODE"))))
          compPositions[id] = Offset(300f - panOffset.x, 200f - panOffset.y)
        },
        modifier = Modifier.weight(1f)
      )
      NeoButton(
        text = "+ Sig Gen",
        onClick = {
          triggerHapticFeedback(context, 40)
          val id = "ac_${System.currentTimeMillis()}"
          components.add(BBComponent(id, "AC_SOURCE", 12.0, listOf(BBPin("p1_$id", id, "SIGNAL"), BBPin("p2_$id", id, "GND"))))
          compPositions[id] = Offset(100f - panOffset.x, 200f - panOffset.y)
        },
        modifier = Modifier.weight(1f)
      )
    }

    NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
  }
}
