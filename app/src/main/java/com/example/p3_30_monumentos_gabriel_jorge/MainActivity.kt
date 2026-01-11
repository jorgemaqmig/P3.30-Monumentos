package com.example.p3_30_monumentos_gabriel_jorge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.p3_30_monumentos_gabriel_jorge.model.Monumento
import com.example.p3_30_monumentos_gabriel_jorge.ui.theme.P3_30_Monumentos_Gabriel_JorgeTheme
import com.example.p3_30_monumentos_gabriel_jorge.ui.theme.SoraFontFamily
import com.example.p3_30_monumentos_gabriel_jorge.viewmodel.MonumentosViewModel
import com.example.p3_30_monumentos_gabriel_jorge.viewmodel.UiState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MonumentosApp() }
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonumentosApp(viewModel: MonumentosViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiStringsMap by viewModel.uiStrings.collectAsState()

    val listaOriginal =
            when (val state = uiState) {
                is UiState.Success -> state.monumentos
                else -> emptyList()
            }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estados
    var ordenActual by rememberSaveable { mutableStateOf("ASC") }
    var currentThemeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
    var currentLanguageMode by rememberSaveable { mutableStateOf("SYSTEM") }

    // Lógica para ordenar
    val listaParaMostrar =
            remember(ordenActual, listaOriginal) {
                when (ordenActual) {
                    "ASC" -> listaOriginal.sortedBy { it.titulo }
                    "DESC" -> listaOriginal.sortedByDescending { it.titulo }
                    "PAIS" -> listaOriginal.sortedBy { it.pais }
                    else -> listaOriginal
                }
            }

    // Lógica para tema
    val systemIsDark = isSystemInDarkTheme()
    val useDarkTheme =
            remember(currentThemeMode, systemIsDark) {
                when (currentThemeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> systemIsDark
                }
            }

    P3_30_Monumentos_Gabriel_JorgeTheme(darkTheme = useDarkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

            // Modal de ajustes
            ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(
                                LocalLayoutDirection provides LayoutDirection.Ltr
                        ) {
                            ModalDrawerSheet {
                                Column(
                                        modifier =
                                                Modifier.padding(horizontal = 16.dp)
                                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Spacer(Modifier.height(16.dp))
                                    Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                                dynamicString(R.string.settings, uiStringsMap),
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                                onClick = { scope.launch { drawerState.close() } }
                                        ) { Icon(Icons.Filled.Close, dynamicString(R.string.close, uiStringsMap)) }
                                    }
                                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                                    Text(
                                            dynamicString(R.string.appearance, uiStringsMap),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                    )
                                    ThemeOptionRow(
                                            dynamicString(R.string.system, uiStringsMap),
                                            currentThemeMode == ThemeMode.SYSTEM
                                    ) { currentThemeMode = ThemeMode.SYSTEM }
                                    ThemeOptionRow(
                                            dynamicString(R.string.light, uiStringsMap),
                                            currentThemeMode == ThemeMode.LIGHT
                                    ) { currentThemeMode = ThemeMode.LIGHT }
                                    ThemeOptionRow(
                                            dynamicString(R.string.dark, uiStringsMap),
                                            currentThemeMode == ThemeMode.DARK
                                    ) { currentThemeMode = ThemeMode.DARK }

                                    Text(
                                            dynamicString(R.string.sort_list, uiStringsMap),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                    )
                                    SortOptionRow(dynamicString(R.string.sort_az, uiStringsMap), "ASC", ordenActual) {
                                        ordenActual = it
                                    }
                                    SortOptionRow(dynamicString(R.string.sort_za, uiStringsMap), "DESC", ordenActual) {
                                        ordenActual = it
                                    }
                                    SortOptionRow(dynamicString(R.string.sort_country, uiStringsMap), "PAIS", ordenActual) {
                                        ordenActual = it
                                    }

                                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                                    Text(
                                            dynamicString(R.string.monument_lang_title, uiStringsMap),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    LanguageDropdown(currentLanguageMode, uiStringsMap) {
                                        currentLanguageMode = it
                                        viewModel.updateLanguage(it)
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(
                            topBar = {
                                TopAppBar(
                                        title = {
                                            Text(
                                                    text = dynamicString(R.string.app_name, uiStringsMap).uppercase(),
                                                    fontFamily = SoraFontFamily,
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    fontWeight = FontWeight.Bold
                                            )
                                        },
                                        actions = {
                                            IconButton(
                                                    onClick = {
                                                        scope.launch { drawerState.open() }
                                                    }
                                            ) {
                                                Icon(
                                                        imageVector = Icons.Filled.Menu,
                                                        contentDescription = dynamicString(R.string.menu_open_desc, uiStringsMap)
                                                )
                                            }
                                        }
                                )
                            }
                    ) { innerPadding ->
                        when (uiState) {

                            // Estados de carga
                            is UiState.Loading -> {
                                Box(
                                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                                dynamicString(R.string.translating, uiStringsMap),
                                                modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        androidx.compose.material3.LinearProgressIndicator(
                                                progress = (uiState as UiState.Loading).progress,
                                                modifier = Modifier.width(200.dp)
                                        )
                                    }
                                }
                            }

                            // Estados de error
                            is UiState.Error -> {
                                Box(
                                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                                        contentAlignment = Alignment.Center
                                ) { Text(dynamicString(R.string.error_loading, uiStringsMap)) }
                            }

                            // Estados de éxito
                            is UiState.Success -> {
                                MonumentosList(
                                        monumentoList = listaParaMostrar,
                                        uiStringsMap = uiStringsMap,
                                        modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función para mostrar las opciones de tema
@Composable
fun ThemeOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
            Modifier.fillMaxWidth()
                    .height(48.dp)
                    .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
            verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// Función para mostrar el menú desplegable de idiomas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
        currentLanguage: String,
        uiStringsMap: Map<Int, String>,
        onLanguageSelected: (String) -> Unit
) {

    val languages =
            listOf(
                    "SYSTEM" to dynamicString(R.string.language_system, uiStringsMap),
                    "es" to dynamicString(R.string.language_es, uiStringsMap),
                    "en" to dynamicString(R.string.language_en, uiStringsMap),
                    "pt" to dynamicString(R.string.language_pt, uiStringsMap),
                    "fr" to dynamicString(R.string.language_fr, uiStringsMap),
                    "it" to dynamicString(R.string.language_it, uiStringsMap)
            )
    var expanded by remember { mutableStateOf(false) }
    val selectedText =
            languages.find { it.first == currentLanguage }?.second ?: dynamicString(R.string.language_system, uiStringsMap)

    ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languages.forEach { (code, label) ->
                DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onLanguageSelected(code)
                            expanded = false
                        }
                )
            }
        }
    }
}

// Función para mostrar las opciones de ordenamiento
@Composable
fun SortOptionRow(label: String, value: String, currentValue: String, onSelect: (String) -> Unit) {
    Row(
            Modifier.fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                            selected = (currentValue == value),
                            onClick = { onSelect(value) },
                            role = Role.RadioButton
                    ),
            verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = (currentValue == value), onClick = null)
        Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// Función para mostrar monumentos
@Composable
fun MonumentoCard(
        monumento: Monumento,
        uiStringsMap: Map<Int, String>,
        modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageResId =
            context.resources.getIdentifier(monumento.ruta_imagen, "drawable", context.packageName)

    var expanded by remember { mutableStateOf(false) }

    // Animaciones
    val imageAlpha by
            animateFloatAsState(targetValue = if (expanded) 1f else 0.3f, label = "ImgAlpha")
    val textAlpha by animateFloatAsState(targetValue = if (expanded) 0f else 1f, label = "TxtAlpha")

    Card(
            modifier =
                    modifier
                            .clickable { expanded = !expanded }
                            .animateContentSize(
                                    animationSpec =
                                            spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                            )
                            )
    ) {
        Column {
            if (imageResId != 0) {
                Box(modifier = Modifier) {
                    AsyncImage(
                            model =
                                    ImageRequest.Builder(context)
                                            .data(imageResId)
                                            .crossfade(true)
                                            .build(),
                            contentDescription = monumento.titulo,
                            modifier = Modifier.fillMaxWidth().height(194.dp),
                            contentScale = ContentScale.Crop,
                            alpha = imageAlpha
                    )

                    Text(
                            text = monumento.titulo,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp,
                            modifier = Modifier.align(Alignment.Center).alpha(textAlpha)
                    )
                }
            } else {
                Text(text = dynamicString(R.string.image_not_found, uiStringsMap), modifier = Modifier.padding(8.dp))
            }

            if (expanded) {
                Text(
                        text = monumento.titulo + " (" + monumento.pais + ")",
                        fontFamily = SoraFontFamily,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        style = MaterialTheme.typography.headlineSmall
                )

                Text(
                        text = monumento.descripcion,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Función para mostrar la lista de monumentos
@Composable
fun MonumentosList(
        monumentoList: List<Monumento>,
        uiStringsMap: Map<Int, String>,
        modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(monumentoList) { monumento ->
            MonumentoCard(
                    monumento = monumento,
                    uiStringsMap = uiStringsMap,
                    modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun dynamicString(id: Int, uiStringsMap: Map<Int, String>): String {
    return uiStringsMap[id] ?: stringResource(id)
}
