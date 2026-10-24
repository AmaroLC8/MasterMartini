package com.example.mastermartini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.mastermartini.R

// 1. MOLDE DE DATOS (Igual que cuando creabas una clase simple en tus ejercicios)
// Representa cada curso o ficha técnica del catálogo de Master Martini
data class CursoMasterMartini(
    val titulo: String,
    val insumo: String,
    val chef: String,
    val duracion: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    // Recibimos exactamente los 5 datos que nos manda el MainActivity
    nombreUsuario: String,
    rubroUsuario: String,
    rutUsuario: String,
    correoUsuario: String,
    onCerrarSesion: () -> Unit
) {
    // Colores estilo bandera italiana de Master Martini
    val verdeItaliano = Color(0xFF008C45)
    val rojoItaliano = Color(0xFFCD212A)

    // 2. LISTA INMUTABLE CON listOf() (Idéntico a lo que usaste en tus ejercicios de Kotlin)
    val catalogoCursos = listOf(
        CursoMasterMartini(
            titulo = "Curva de Templado en Chocolatería",
            insumo = "Cobertura Caravella Dark 54%",
            chef = "Chef Roberto Valenzuela",
            duracion = "15 min"
        ),
        CursoMasterMartini(
            titulo = "Estabilización y Batido de Cremas",
            insumo = "Crema Vegetal Decor Up",
            chef = "Chef Elena Rossi",
            duracion = "20 min"
        ),
        CursoMasterMartini(
            titulo = "Laminado de Masa de Hoja Artesanal",
            insumo = "Margarina Master Martini Placa",
            chef = "Maestro Marco Zanetti",
            duracion = "35 min"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catálogo Master Martini B2B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        // 3. LazyColumn: Es una columna con scroll automático ideal para listas de elementos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item{
                Text(
                    text = "MasterClass en video (Offline):",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                ReproductorVideoLocal()
            }
            // Bloque 1: Franja tricolor italiana y Tarjeta con los datos guardados en el celular
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_master_martini),
                        contentDescription = "Logo Master Martini",
                        modifier = Modifier
                            .width(200.dp)
                            .height(65.dp)
                            .padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .width(120.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(verdeItaliano))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(rojoItaliano))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tarjeta que demuestra al profesor que los datos se leyeron desde AlmacenamientoLocal
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¡Bienvenido, $nombreUsuario!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Rubro del Taller: $rubroUsuario", fontWeight = FontWeight.SemiBold, color = verdeItaliano)
                        Text(text = "RUT Registrado: $rutUsuario", fontSize = 14.sp)
                        Text(text = "Correo: $correoUsuario", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Datos recuperados del almacenamiento local",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Bloque 2: Título de la sección de cursos
            item {
                Text(
                    text = "Masterclasses y Recetas Técnicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bloque 3: Equivalente al ciclo 'for (curso in catalogoCursos)'
            // Por cada curso en la lista, dibuja una Card en la pantalla
            items(catalogoCursos) { curso ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = curso.titulo,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Insumo principal: ${curso.insumo}", fontSize = 14.sp)
                        Text(text = "Instructor: ${curso.chef}", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⏱️ Duración: ${curso.duracion}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = rojoItaliano
                        )
                    }
                }
            }

            // Bloque 4: Botón para regresar al formulario (Navegación funcional)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Editar Datos / Volver al Registro", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}