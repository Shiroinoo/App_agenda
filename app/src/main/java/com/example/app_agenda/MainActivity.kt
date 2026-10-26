package com.example.app_agenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// ============================================================
// COLORES (aproximados del Figma: reemplaza por los hex exactos)
// ============================================================
val Teal = Color(0xFF9CC5C5)
val TealClaro = Color(0xFFC8DEDE)
val Crema = Color(0xFFFBF3EC)
val Cafe = Color(0xFF8A6A60)
val TextoOscuro = Color(0xFF1F2A2A)

// ============================================================
// MODELO
// ============================================================
data class Tarea(
    val id: Int,
    val titulo: String,
    val categoria: String,
    val detalle: String,
    val cuando: String,
    val completada: Boolean = false,
    val importante: Boolean = false
)

enum class Filtro(val etiqueta: String) {
    TODAS("Tareas"),
    PENDIENTES("Pendientes"),
    IMPORTANTES("Importantes")
}

val tareasIniciales = listOf(
    Tarea(5, "Tarea 05", "Salud", "Cita con Doc.", "Martes, 10:00 a.m.", importante = true),
    Tarea(4, "Tarea 04", "Hogar", "Lavar la ropa", "Sábado"),
    Tarea(3, "Tarea 03", "Escuela", "Entregar proyecto de química", "Lunes, 08:00 a.m.", importante = true),
    Tarea(2, "Tarea 02", "Hogar", "Ir al supermercado", "Sábado, 04:00 p.m.", completada = true),
    Tarea(1, "Tarea 01", "Escuela", "Comprar materiales para el proyecto de química", "Jueves", completada = true)
)

// ============================================================
// LÓGICA (funciones con retorno + lambdas de orden superior)
// ============================================================
fun filtrarTareas(tareas: List<Tarea>, filtro: Filtro, busqueda: String): List<Tarea> =
    tareas.filter { t ->
        val coincideBusqueda = t.titulo.contains(busqueda, ignoreCase = true) ||
                t.detalle.contains(busqueda, ignoreCase = true) ||
                t.categoria.contains(busqueda, ignoreCase = true)
        val coincideFiltro = when (filtro) {
            Filtro.TODAS -> true
            Filtro.PENDIENTES -> !t.completada
            Filtro.IMPORTANTES -> t.importante
        }
        coincideBusqueda && coincideFiltro
    }

fun alternarCompletada(tareas: List<Tarea>, id: Int): List<Tarea> =
    tareas.map { if (it.id == id) it.copy(completada = !it.completada) else it }

// ============================================================
// NAVEGACIÓN
// ============================================================
object Rutas {
    const val TAREAS = "tareas"
    const val CONSTRUCCION = "construccion"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { AppNav() }
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Rutas.TAREAS) {
        composable(Rutas.TAREAS) {
            PantallaTareas(onEnConstruccion = { navController.navigate(Rutas.CONSTRUCCION) })
        }
        composable(Rutas.CONSTRUCCION) {
            PantallaEnConstruccion(onVolver = { navController.popBackStack() })
        }
    }
}

// ============================================================
// PANTALLA PRINCIPAL
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(onEnConstruccion: () -> Unit) {
    var tareas by remember { mutableStateOf(tareasIniciales) }
    var filtro by remember { mutableStateOf(Filtro.TODAS) }
    var busqueda by remember { mutableStateOf("") }

    val visibles = filtrarTareas(tareas, filtro, busqueda)
    val pendientes = visibles.filter { !it.completada }
    val completadas = visibles.filter { it.completada }

    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onEnConstruccion,
                containerColor = Cafe,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar nueva tarea")
            }
        },
        bottomBar = { BarraInferior(onEnConstruccion) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // ---- Buscador ----
            TextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar tareas") },
                singleLine = true,
                leadingIcon = {
                    IconButton(onClick = onEnConstruccion) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Teal,
                    unfocusedContainerColor = Teal,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextoOscuro,
                    unfocusedTextColor = TextoOscuro
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ---- Filtros ----
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Filtro.entries.forEach { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = { Text(f.etiqueta, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = TealClaro,
                            selectedContainerColor = Teal,
                            labelColor = TextoOscuro,
                            selectedLabelColor = TextoOscuro
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Lista ----
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(pendientes, key = { it.id }) { tarea ->
                    TarjetaTarea(tarea) { tareas = alternarCompletada(tareas, tarea.id) }
                }

                if (completadas.isNotEmpty()) {
                    item {
                        Column(Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(color = Color.LightGray)
                            Text(
                                "Tareas completadas",
                                fontSize = 13.sp,
                                color = TextoOscuro,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                    items(completadas, key = { it.id }) { tarea ->
                        TarjetaTarea(tarea) { tareas = alternarCompletada(tareas, tarea.id) }
                    }
                }

                if (visibles.isEmpty()) {
                    item {
                        Text(
                            "No hay tareas que coincidan.",
                            color = TextoOscuro,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaTarea(tarea: Tarea, onAlternar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Crema),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${tarea.titulo} ● ${tarea.categoria}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextoOscuro
                )
                Text("•  ${tarea.detalle}", fontSize = 13.sp, color = TextoOscuro)
                Text("•  ${tarea.cuando}", fontSize = 13.sp, color = TextoOscuro)
            }
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { onAlternar() },
                colors = CheckboxDefaults.colors(checkedColor = Cafe, uncheckedColor = TextoOscuro),
                modifier = Modifier.semantics {
                    contentDescription = "Marcar ${tarea.titulo} como completada"
                }
            )
        }
    }
}

// ============================================================
// BARRA INFERIOR
// ============================================================
@Composable
fun BarraInferior(onEnConstruccion: () -> Unit) {
    Surface(
        color = TealClaro,
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ItemNav("Tareas", Icons.Default.Check, seleccionado = true, onClick = {})
            ItemNav("Calendario", Icons.Default.DateRange, seleccionado = false, onClick = onEnConstruccion)
            ItemNav("Usuario", Icons.Default.Person, seleccionado = false, onClick = onEnConstruccion)
        }
    }
}

@Composable
fun ItemNav(texto: String, icono: ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
            .clickable(role = Role.Tab, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .background(if (seleccionado) Teal else Color.Transparent, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Icon(icono, contentDescription = null, tint = TextoOscuro)
        }
        Text(texto, fontSize = 12.sp, color = TextoOscuro)
    }
}

// ============================================================
// PANTALLA "EN CONSTRUCCIÓN"
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEnConstruccion(onVolver: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = Cafe,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Estamos trabajando en ello",
                style = MaterialTheme.typography.headlineMedium,
                color = TextoOscuro
            )
            Spacer(Modifier.height(8.dp))
            Text("Esta función estará disponible pronto.", color = TextoOscuro)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onVolver,
                colors = ButtonDefaults.buttonColors(containerColor = Cafe),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Volver")
            }
        }
    }
}