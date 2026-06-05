package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.*
import com.example.data.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainCosmosWorkspace()
            }
        }
    }
}

@Composable
fun MainCosmosWorkspace() {
    val context = LocalContext.current
    val viewModel: CosmoViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    
    val files by viewModel.filesList.collectAsState(initial = emptyList())
    val messages by viewModel.messagesList.collectAsState(initial = emptyList())

    var searchInputText by remember { mutableStateOf("") }
    var showAddFileDialog by remember { mutableStateOf(false) }

    // Dynamic state trackers for Add File inputs
    var newFileName by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }
    var newFileType by remember { mutableStateOf("PDF") }

    // Trigger toast feedbacks
    LaunchedEffect(state.systemMessage) {
        state.systemMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearSystemMessage()
        }
    }

    // Outer space dark gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030310), // Deep Indigo
                        Color(0xFF0B001F)  // Dark Cosmic purple
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        // --- SPLASH SCREEN WITH INTERACTIVE INTRO ---
        AnimatedVisibility(
            visible = state.showSplash,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.fillMaxSize().zIndex(99f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF03030D)),
                contentAlignment = Alignment.Center
            ) {
                // Background star grid dots decorative
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rnd = kotlin.random.Random(42)
                    for (i in 0..60) {
                        drawCircle(
                            color = Color.White.copy(alpha = rnd.nextFloat() * 0.4f),
                            radius = (1..3).random(rnd).toFloat(),
                            center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Pulsating rotating 3D star logo
                    RotatingCoreLogo3D(
                        modifier = Modifier
                            .size(175.dp)
                            .testTag("splash_rotating_logo"),
                        primaryColor = Color(0xFF00F2FE),
                        secondaryColor = Color(0xFFFF007F)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title with typing neon effect
                    Text(
                        text = "IDLEB X – AI COSMOS",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Developer Tagline using automatic writing effect
                    TypewriterText(
                        text = "MOOHAMED - IDLEB X",
                        textColor = Color(0xFFFF007F),
                        fontSize = 15f,
                        modifier = Modifier.testTag("splash_developer_tag"),
                        onComplete = {
                            // Synthesize nice opening chirp upon load
                            CosmoAudio.playChimeChord()
                        }
                    )
                    
                    Text(
                        text = CosmoTranslation.get("tagline", state.isEnglish),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Enter button
                    Button(
                        onClick = {
                            CosmoAudio.playCosmicBeep(880.0, 150, 0.6f)
                            viewModel.dismissSplash()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .border(1.5.dp, Color(0xFF00F2FE), RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F2FE).copy(alpha = 0.2f), Color(0xFF9D4EDD).copy(alpha = 0.2f))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                            .testTag("splash_enter_button")
                    ) {
                        Text(
                            text = CosmoTranslation.get("enter_button", state.isEnglish),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick bilingual language selector on splash
                    Row(
                        modifier = Modifier
                            .clickable {
                                CosmoAudio.playCosmicBeep(600.0, 80, 0.3f)
                                viewModel.toggleLanguage()
                            }
                            .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(12.dp))
                            .background(Color(0x0CFFFFFF))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFF00F2FE),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isEnglish) "Switch to العربية" else "الانتقال إلى English",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }


        // --- MAIN APPLICATION WORKSPACE ---
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            
            // --- TOP APP NAVIGATION & ACTION HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x2B050212))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "IDLEB X – AI COSMOS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = CosmoTranslation.get("developer", state.isEnglish),
                        color = Color(0xFFFF007F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear Chat logs
                    IconButton(
                        onClick = {
                            CosmoAudio.playCollapseEffect()
                            viewModel.clearChat()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0x0CFFFFFF), RoundedCornerShape(100))
                            .testTag("action_clear_chat")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = CosmoTranslation.get("clear_history", state.isEnglish),
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset and Seed Files
                    IconButton(
                        onClick = {
                            CosmoAudio.playCollapseEffect()
                            viewModel.clearAllFiles()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0x0CFFFFFF), RoundedCornerShape(100))
                            .testTag("action_clear_files")
                    ) {
                        Icon(
                            Icons.Default.FolderDelete,
                            contentDescription = CosmoTranslation.get("clear_files", state.isEnglish),
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Language Toggle
                    IconButton(
                        onClick = {
                            CosmoAudio.playCosmicBeep(650.0, 90, 0.4f)
                            viewModel.toggleLanguage()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0x0CFFFFFF), RoundedCornerShape(100))
                            .testTag("action_toggle_language")
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "Language",
                            tint = Color(0xFF00F2FE),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Outer Scrollable Layout wrapping file orbits and terminal interface
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                // --- 1. DYNAMIC ORBIT CANVAS INTERACTIVE SECTION ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x1300F2FE), Color.Transparent),
                                radius = 400f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (files.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CosmoTranslation.get("no_files", state.isEnglish),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Continuous rotation files orbit graph
                        InteractiveCosmoOrbitCanvas(
                            modifier = Modifier.fillMaxSize(),
                            files = files,
                            matchingFileIds = state.matchingFileIds,
                            laserTriggerTimestamp = state.laserTriggerTimestamp,
                            onFileClicked = { clickedPlanet ->
                                viewModel.selectFileForPreview(clickedPlanet)
                            }
                        )
                    }

                    // Bottom info label inside orbits
                    Text(
                        text = CosmoTranslation.get("files_helper", state.isEnglish),
                        color = Color.White.copy(0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }

                // --- 2. MULTI-MODEL DIAL PANEL ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = CosmoTranslation.get("model_selector", state.isEnglish),
                        color = Color(0xFF00F2FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    ModelRotatingSelector(
                        activeModels = state.activeModels,
                        onToggleModel = { modelName ->
                            viewModel.toggleModelSelection(modelName)
                        }
                    )
                }

                // --- 3. PRESENTATION PROJECTION MODE CARDS ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = CosmoTranslation.get("presentation_mode", state.isEnglish),
                        color = Color(0xFFFF007F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Three tab buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Text", Icons.Default.ChatBubbleOutline, "mode_text"),
                            Triple("Chart", Icons.Default.BarChart, "mode_chart"),
                            Triple("Voice", Icons.Default.VolumeUp, "mode_voice")
                        ).forEach { (modeID, asset, translationKey) ->
                            val isActive = state.activePresentationMode == modeID
                            Button(
                                onClick = {
                                    CosmoAudio.playCosmicBeep(700.0, 80, 0.4f)
                                    viewModel.setPresentationMode(modeID)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) Color(0xFF9D4EDD) else Color(0x13FFFFFF)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) Color(0xFF00F2FE) else Color.White.copy(0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        asset,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = CosmoTranslation.get(translationKey, state.isEnglish),
                                        color = if (isActive) Color.White else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 4. DYNAMIC SCREEN RESULTS OUTPUT ACCORDING TO PRES MODE ---
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    when (state.activePresentationMode) {
                        "Voice" -> {
                            // Pulsing glowing orb wave visualizer
                            NeonGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                borderColor = Color(0xFF9D4EDD)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlowingWaveVisualizer(
                                        modifier = Modifier.fillMaxSize(),
                                        isSpeaking = state.isSpeaking
                                    )

                                    if (state.isSpeaking) {
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "アシスタントが話しています...",
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                text = state.currentSpokenText.take(65) + "...",
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Awaiting voice trigger...",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        "Chart" -> {
                            // Immersive dynamic metrics diagrams (Toggle charts)
                            var activeSubChart by remember { mutableStateOf("bar") }

                            Column {
                                // Chart choices chips
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        Pair("bar", "charts_bar"),
                                        Pair("radar", "charts_radar"),
                                        Pair("heatmap", "charts_heatmap")
                                    ).forEach { (cID, trans) ->
                                        val isCurrent = activeSubChart == cID
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isCurrent) Color(0x3B00F2FE) else Color(0x0CFFFFFF))
                                                .border(1.dp, if (isCurrent) Color(0xFF00F2FE) else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    CosmoAudio.playCosmicBeep(650.0, 70, 0.4f)
                                                    activeSubChart = cID
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = CosmoTranslation.get(trans, state.isEnglish),
                                                color = if (isCurrent) Color.White else Color.LightGray,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                NeonGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    borderColor = Color(0xFF00F2FE)
                                ) {
                                    CustomCosmoCharts(
                                        modifier = Modifier.fillMaxSize(),
                                        chartType = activeSubChart,
                                        dataJson = null
                                    )
                                }
                            }
                        }

                        else -> {
                            // "Text" Mode: LazyColumn terminal chat bubbles
                            NeonGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                borderColor = Color(0xFF00F2FE)
                            ) {
                                if (messages.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = CosmoTranslation.get("no_messages", state.isEnglish),
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(messages) { msg ->
                                            val isUser = msg.sender == "User"
                                            val isLocalBanana = msg.sender == "Nano Banana"

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        if (isUser) Icons.Default.Person else Icons.Default.RocketLaunch,
                                                        contentDescription = null,
                                                        tint = if (isUser) Color(0xFF00F2FE) else if (isLocalBanana) Color.Yellow else Color(0xFF9D4EDD),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = if (isUser) "MOOHAMED (User)" else "${msg.sender} Engine",
                                                        color = if (isUser) Color(0xFF00F2FE) else if (isLocalBanana) Color.Yellow else Color(0xFFD0BCFF),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .clip(
                                                            RoundedCornerShape(
                                                                topStart = 12.dp,
                                                                topEnd = 12.dp,
                                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                                            )
                                                        )
                                                        .background(
                                                            if (isUser) Color(0x3B00F2FE) else Color(0x1F9D4EDD)
                                                        )
                                                        .border(
                                                            width = 0.5.dp,
                                                            color = if (isUser) Color(0x3BFFFFFF) else Color(0x409D4EDD),
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(10.dp)
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = msg.messageText,
                                                            color = Color.White,
                                                            fontSize = 12.sp,
                                                            lineHeight = 16.sp
                                                        )

                                                        // Quick share export utilities directly next to each answer!
                                                        if (!isUser) {
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Divider(color = Color.White.copy(0.12f))
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                            ) {
                                                                // Share Answer as Graphics image Card
                                                                Text(
                                                                    text = "📷 Share Infocard",
                                                                    color = Color(0xFF00F2FE),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier
                                                                        .pointerInput(msg) {
                                                                            detectTapGestures {
                                                                                CosmoAudio.playChimeChord()
                                                                                viewModel.shareAsImage(context, msg.sender, msg.messageText)
                                                                            }
                                                                        }
                                                                )

                                                                // Export All Journal to signed PDF document!
                                                                Text(
                                                                    text = "📄 Export Signed PDF",
                                                                    color = Color(0xFFFF007F),
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier
                                                                        .pointerInput(messages) {
                                                                            detectTapGestures {
                                                                                CosmoAudio.playChimeChord()
                                                                                viewModel.exportToPdf(context, messages)
                                                                            }
                                                                        }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // --- 5. COMMAND BAR BOTTOM INTERFACE ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070519))
                    .border(width = 1.dp, color = Color(0x3B00F2FE), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Custom file node button
                IconButton(
                    onClick = {
                        CosmoAudio.playCosmicBeep(750.0, 100, 0.4f)
                        showAddFileDialog = true
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x0CFFFFFF), RoundedCornerShape(12.dp))
                        .testTag("action_show_inject_panel")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = CosmoTranslation.get("upload_file_title", state.isEnglish),
                        tint = Color(0xFF00F2FE),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Commanding key field in modern glowing styling
                OutlinedTextField(
                    value = searchInputText,
                    onValueChange = { searchInputText = it },
                    placeholder = {
                        Text(
                            text = CosmoTranslation.get("search_placeholder", state.isEnglish),
                            fontSize = 11.sp,
                            color = Color.LightGray.copy(0.6f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF00F2FE),
                        unfocusedBorderColor = Color.White.copy(0.12f),
                        unfocusedContainerColor = Color(0x05FFFFFF),
                        focusedContainerColor = Color(0x14000000)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("command_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Transmit query projectile
                IconButton(
                    onClick = {
                        if (searchInputText.isNotBlank()) {
                            CosmoAudio.playCosmicBeep(920.0, 120, 0.6f)
                            viewModel.submitMessage(searchInputText)
                            searchInputText = ""
                        }
                    },
                    enabled = !state.isRecordingOrProcessing,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF00F2FE), Color(0xFF9D4EDD))
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("action_transmit_query")
                ) {
                    if (state.isRecordingOrProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Transmit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }


        // --- CUSTOM SUB-OVERLAYS: GLASSMORPHIC PREVIEW DIALOG ---
        AnimatedVisibility(
            visible = state.selectedFileForPreview != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.fillMaxSize().zIndex(98f)
        ) {
            state.selectedFileForPreview?.let { file ->
                val fColor = Color(android.graphics.Color.parseColor(file.colorHex))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.85f))
                        .clickable { viewModel.selectFileForPreview(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .border(1.5.dp, fColor, RoundedCornerShape(20.dp))
                            .background(Color(0xFF070417), RoundedCornerShape(20.dp))
                            .clickable(enabled = false) {}
                            .padding(20.dp)
                            .testTag("file_preview_overlay")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(fColor.copy(0.15f), RoundedCornerShape(100))
                                        .border(1.dp, fColor, RoundedCornerShape(100)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = file.typeSymbol,
                                        color = fColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = file.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Weight: ${file.sizeLabel} | Orbit: ${file.orbitRadius.toInt()}dp",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    CosmoAudio.playCosmicBeep(500.0, 70, 0.4f)
                                    viewModel.selectFileForPreview(null)
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(0.12f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = file.content,
                            color = Color.White.copy(0.9f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                CosmoAudio.playCosmicBeep(500.0, 70, 0.4f)
                                viewModel.selectFileForPreview(null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = fColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Collapse Orbit Preview", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }


        // --- CUSTOM SUB-OVERLAYS: INJECT ORBITAL FILE PLANE PANEL drawer ---
        AnimatedVisibility(
            visible = showAddFileDialog,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize().zIndex(97f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.85f))
                    .clickable { showAddFileDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, Color(0xFF00F2FE), RoundedCornerShape(20.dp))
                        .background(Color(0xFF050314), RoundedCornerShape(20.dp))
                        .clickable(enabled = false) {}
                        .padding(20.dp)
                        .testTag("file_injector_overlay")
                ) {
                    Text(
                        text = CosmoTranslation.get("upload_file_title", state.isEnglish),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input File Title
                    Text(CosmoTranslation.get("file_name_label", state.isEnglish), color = Color(0xFF00F2FE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        modifier = Modifier.fillMaxWidth().testTag("add_file_name_field"),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F2FE),
                            unfocusedBorderColor = Color.White.copy(0.12f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Select Format segment row
                    Text("Select Telemetry format signature:", color = Color.LightGray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PDF", "TXT", "MD", "Image").forEach { format ->
                            val isChosen = newFileType == format
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) Color(0xFF9D4EDD) else Color(0x13FFFFFF))
                                    .border(1.dp, if (isChosen) Color(0xFF00F2FE) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        CosmoAudio.playCosmicBeep(600.0, 60, 0.4f)
                                        newFileType = format
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(format, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input File Payload Content
                    Text(CosmoTranslation.get("file_content_label", state.isEnglish), color = Color(0xFF00F2FE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newFileContent,
                        onValueChange = { newFileContent = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_file_content_field"),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F2FE),
                            unfocusedBorderColor = Color.White.copy(0.12f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                CosmoAudio.playCosmicBeep(450.0, 70, 0.4f)
                                showAddFileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (newFileName.isNotBlank() && newFileContent.isNotBlank()) {
                                    CosmoAudio.playChimeChord()
                                    viewModel.uploadFile(newFileName, newFileContent, newFileType)
                                    
                                    // Flush states
                                    newFileName = ""
                                    newFileContent = ""
                                    showAddFileDialog = false
                                } else {
                                    CosmoAudio.playCollapseEffect()
                                    Toast.makeText(context, "Please complete fields!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                            modifier = Modifier.weight(1.5f).testTag("submit_inject_button")
                        ) {
                            Text(
                                CosmoTranslation.get("inject_button", state.isEnglish),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
