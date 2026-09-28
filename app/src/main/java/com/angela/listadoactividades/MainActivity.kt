package com.angela.listadoactividades

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.angela.listadoactividades.ui.theme.ListadoActividadesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // en vez de setContentView, usamos setContent=puerta de entrada del compose
        setContent {
            ListadoActividadesTheme {
                //surface que ocupa toda la pantalla
                Surface(modifier = Modifier.fillMaxSize()) {
                    // se llama la logica de la app
                    PantallaListadoActividades()
                }
            }
        }
    }
}

// declaracion de estados

@Composable
fun PantallaListadoActividades() {

    // lista de actividades vacia
    var actividades by remember { mutableStateOf(listOf<Actividad>()) }
    var siguienteId by remember { mutableStateOf(1) }
    var textoNuevaActividad by remember { mutableStateOf("") }
    var filtroSeleccionado by remember { mutableStateOf("Todas") }

    //dato derivado, se calcula a partir de lista y el filtro
    val actividadesFiltradas = when (filtroSeleccionado) {
        "Pendientes" -> actividades.filter { !it.completada }
        "Completadas" -> actividades.filter { it.completada }
        else -> actividades
    }

    // interfaz
    // estructura visual principal
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Actividades académicas",
            style = MaterialTheme.typography.headlineSmall, //estilo definido en el tema
            fontWeight = FontWeight.Bold
        )

        //separar cada elemento
        Spacer(modifier = Modifier.height(12.dp))

        // formulario, agregar acividad
        // aqui se hace state hosting (var textoNueva Actividad)
        FormularioActividad(
            texto = textoNuevaActividad,
            //callback que el formulario ejecuta cada vez que el usuario escribe
            onTextoChange = { textoNuevaActividad = it },
            //calback que se ejecuta para agregar
            onAgregar = {
                //verificacion de campo vacio
                if (textoNuevaActividad.isNotBlank()) {
                    actividades = actividades + Actividad(id = siguienteId, nombre = textoNuevaActividad)
                    siguienteId++
                    textoNuevaActividad = ""
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // state hosting, recibe un estado, para avisar por medio de un callback que el usuario elige otro
        FiltrosLazyRow(
            filtroActual = filtroSeleccionado,
            onFiltroSeleccionado = { filtroSeleccionado = it }
        )

        Spacer(modifier = Modifier.height(12.dp))


        // condicion que decide mostrar segun hayan actividades o no
            if (actividadesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay actividades para mostrar")
                }
            } else {

                //lista principal, mostrar  la lista de actividades y que el usuario pueda interactuar
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // recorre cada actividad filtrada; el key ayuda a Compose a identificar cada elemento aunque la lista cambie de orden.
                    items(items = actividadesFiltradas, key = { it.id }) { actividad ->
                        ActividadCard(
                            actividad = actividad,
                            onCambiarEstado = {
                                // se usa .map() para recorrer toda la lista
                                actividades = actividades.map {
                                    // si el id coincide con la actividad tocada, se crea una copia (.copy()) con completada invertido
                                    if (it.id == actividad.id) it.copy(completada = !it.completada) else it
                                }
                            },
                            //se usa .filter() para quedarse con todas las actividades excepto la que tiene ese id.
                            onEliminar = {
                                actividades = actividades.filter { it.id != actividad.id }
                            }
                        )
                    }
                }
            }
    }
}

@Composable

// dibuja el formulario para agregar actividades
fun FormularioActividad(
    //parametros
    texto: String,
    onTextoChange: (String) -> Unit,
    onAgregar: () -> Unit
) {
    //contenido
    //coloca los elementos en horizontal y los centra verticalmente
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = texto,
            //se llama esta funcion con el texto nuevo. el padre lo guarda y el compose se recompoe
            onValueChange = onTextoChange,
            label = { Text("Nueva actividad") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onAgregar) {
            Text("Agregar")
        }
    }
}

//filtro lazyRow, agrega una fila horizontal con 3 opciones
@Composable
fun FiltrosLazyRow(
    filtroActual: String,
    onFiltroSeleccionado: (String) -> Unit
) {
    val opciones = listOf("Todas", "Pendientes", "Completadas")

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opciones) { opcion ->
            FilterChip(
                selected = filtroActual == opcion,
                onClick = { onFiltroSeleccionado(opcion) },
                label = { Text(opcion) }
            )
        }
    }
}


//tarjeta individual de actividad (checkbox, nombre la actividad y un boton para eliminarla)
@Composable
fun ActividadCard(
    actividad: Actividad,
    //se ejecuta cuando el usuario toca el checkbox
    onCambiarEstado: () -> Unit,
    // se ejecuta cuando el usuario toca la papelera
    onEliminar: () -> Unit
) {
    // card nos presentara visualmente cada actividad de forma agrupada
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = actividad.completada,
                onCheckedChange = { onCambiarEstado() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = actividad.nombre,
                modifier = Modifier.weight(1f),
                // Si la actividad está completada, el texto aparece tachado (LineThrough)
                textDecoration = if (actividad.completada) TextDecoration.LineThrough else TextDecoration.None,
                //en un color más apagado (outline). Si no, se ve normal con el color estándar sobre superficies (onSurface).
                color = if (actividad.completada) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )

            //boton circular para iconos
            IconButton(onClick = onEliminar) {
                //icono de papelera que viene de la libreria de iconos de Material
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar actividad")
            }
        }
    }
}