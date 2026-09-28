package com.angela.listadoactividades

// data class que representara una actividad academica
// nos definé que informacion guarda cada elemento de la lista
data class Actividad(
    val id: Int,
    val nombre: String,
    val completada: Boolean = false
)