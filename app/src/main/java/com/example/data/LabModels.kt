package com.example.data

data class Student(
  val id: String,
  val name: String,
  val rollNumber: String,
  val department: String = "Electronics and Communication Engineering",
  val semester: Int,
  val email: String
)

data class Teacher(
  val id: String,
  val name: String,
  val designation: String,
  val department: String = "Electronics and Communication Engineering",
  val email: String
)

data class Experiment(
  val id: String,
  val title: String,
  val description: String,
  val aim: String,
  val apparatus: List<String>,
  val theory: String,
  val formula: String,
  val category: String
)

data class ExperimentAttempt(
  val id: String,
  val studentId: String,
  val experimentId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val score: Int,
  val status: String, // "IN_PROGRESS", "COMPLETED", "VERIFIED"
  val notes: String
)

data class Circuit(
  val id: String,
  val name: String,
  val description: String,
  val components: List<Component>,
  val wires: List<Wire>
)

data class Component(
  val id: String,
  val type: String, // "RESISTOR", "CAPACITOR", "OPAMP", "GATE_AND", "LED"
  val x: Float,
  val y: Float,
  val value: String
)

data class Pin(
  val id: String,
  val componentId: String,
  val name: String,
  val type: String, // "INPUT", "OUTPUT"
  val state: Boolean
)

data class Wire(
  val id: String,
  val fromPinId: String,
  val toPinId: String,
  val color: String
)

data class Assignment(
  val id: String,
  val title: String,
  val description: String,
  val dueDate: String,
  val assignedBy: String
)
