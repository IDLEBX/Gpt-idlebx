package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CosmoFile
import kotlinx.coroutines.delay
import kotlin.math.*

// Multilingual translations helper
object CosmoTranslation {
    fun get(key: String, isEnglish: Boolean): String {
        return if (isEnglish) {
            when (key) {
                "tagline" -> "Interactive Cosmic Intelligent Workspace"
                "developer" -> "Developed by MOOHAMED (IDLEB X)"
                "title" -> "IDLEB X – AI COSMOS"
                "headline" -> "AI COSMOS ENGINE"
                "enter_button" -> "ENTER QUANTUM ORBIT"
                "welcome_toast" -> "Synchronizing cosmic telemetry channels..."
                "model_selector" -> "Active Neural Model Arrays (Select Multiple)"
                "presentation_mode" -> "Dynamic Output Projection Mode"
                "mode_text" -> "Typewriter Panel"
                "mode_chart" -> "3D Canvas Graph"
                "mode_voice" -> "Pulsating Assistant"
                "files_header" -> "Orbital Vector Databases"
                "files_helper" -> "Click on file planets to view cached text segments."
                "search_placeholder" -> "Transmit cosmic search query... (Custom RAG active)"
                "clear_history" -> "Purge Chat Log"
                "clear_files" -> "Purge Orbit Databases"
                "upload_file_title" -> "Inject Custom File Planet"
                "file_name_label" -> "File Planet Designation (e.g. quantum_matrix.txt)"
                "file_content_label" -> "Textual Vector Payload Data"
                "inject_button" -> "Launch Cosmic Orbiter"
                "fallback_voice" -> "Artificial speech resonance activated."
                "pdf_watermark" -> "Compiled via IDLEB X"
                "charts_bar" -> "Comparative Vector Weights"
                "charts_radar" -> "Model Resonance Index"
                "charts_heatmap" -> "Core Temperature Grid"
                "no_files" -> "Galaxy empty. Inject data orbits to begin search."
                "no_messages" -> "Awaiting cosmic transmission..."
                "rag_beacon" -> "RAG active: Particle light beams shooting from matching files!"
                else -> key
            }
        } else {
            // Arabic representation as requested in detailed prompt
            when (key) {
                "tagline" -> "مساحة العمل الكونية الذكية والتفاعلية"
                "developer" -> "تطوير المبدع: MOOHAMED (IDLEB X)"
                "title" -> "IDLEB X – AI COSMOS"
                "headline" -> "محرك الفضاء الذكي الكوني"
                "enter_button" -> "دخول المدار الكمومي الكوني"
                "welcome_toast" -> "جاري مزامنة قنوات التتبع الفلكية الذكية..."
                "model_selector" -> "مصفوفات النماذج العصبية النشطة (يمكن اختيار كود موازٍ)"
                "presentation_mode" -> "نمط عرض الاستجابة الرقمية الفائقة"
                "mode_text" -> "لوحة الكتابة"
                "mode_chart" -> "الرسم التفاعلي"
                "mode_voice" -> "المساعد الصوتي"
                "files_header" -> "قاعد بيانات الملفات المدارية"
                "files_helper" -> "انقر على كرات الملفات للتحقق من المدار ومعاينة النصوص"
                "search_placeholder" -> "وجه سؤالك إلى الكون الذكي لفك شفرة RAG..."
                "clear_history" -> "مسح سجل المحادثات"
                "clear_files" -> "مسح ملفات المجرة"
                "upload_file_title" -> "حقن كوكب ملفات في المجرة"
                "file_name_label" -> "اسم كوكب الملف (مثال: database_specs.pdf)"
                "file_content_label" -> "البيانات النصية المشفرة للمتجه"
                "inject_button" -> "إطلاق الكوكب المداري"
                "fallback_voice" -> "تم تفعيل رنين النطق الاصطناعي للمساعد الكوني."
                "pdf_watermark" -> "تم التصدير بواسطة IDLEB X"
                "charts_bar" -> "الأوزان النسبية للمتجهات"
                "charts_radar" -> "مؤشر الرنين للنماذج"
                "charts_heatmap" -> "مصفوفة درجة حرارة النواة"
                "no_files" -> "أرجاء الفضاء خالية. احقن كواكب ملفات لبدء البحث."
                "no_messages" -> "في انتظار بث الترددات الكونية الذكية..."
                "rag_beacon" -> "تم تفعيل الـ RAG: حزم تتبع ضوئية تنطلق الآن من كواكب ملفاتك!"
                else -> key
            }
        }
    }
}

// Reusable elegant glassmorphic card with neon glow border
@Composable
fun NeonGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF00F2FE),
    borderWidth: Dp = 1.5.dp,
    glowRadius: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NeonTransition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowIntensity"
    )

    Column(
        modifier = modifier
            .shadow(glowRadius, RoundedCornerShape(16.dp), ambientColor = borderColor, spotColor = borderColor)
            .background(Color(0x1B080327), RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            borderColor.copy(alpha = alphaAnim),
                            Color(0xFF9D4EDD).copy(alpha = alphaAnim * 0.4f),
                            borderColor.copy(alpha = alphaAnim * 0.1f)
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = borderWidth.toPx())
                )
            }
            .padding(16.dp),
        content = content
    )
}

// Component to simulate typewriter typing rendering effect
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: Float = 14f,
    onComplete: () -> Unit = {}
) {
    var printedText by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        printedText = ""
        for (i in text.indices) {
            printedText += text[i]
            delay(12L) // Fast elegant type writing effect
        }
        onComplete()
    }

    Text(
        text = printedText,
        color = textColor,
        fontSize = fontSize.sp,
        fontFamily = FontFamily.SansSerif,
        lineHeight = (fontSize * 1.4f).sp,
        modifier = modifier
    )
}

// 3D Wireframe geometric Core Star rotating in real-time
@Composable
fun RotatingCoreLogo3D(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00F2FE),
    secondaryColor: Color = Color(0xFFFF007F)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CubeTime")
    val angleRad by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val sizeL = min(size.width, size.height) * 0.4f

        // Calculate 8 coordinates of 3D hypercube
        val points3D = listOf(
            // Back face
            Offset(-sizeL, -sizeL), Offset(sizeL, -sizeL),
            Offset(sizeL, sizeL), Offset(-sizeL, sizeL),
            // Front face
            Offset(-sizeL * 0.6f, -sizeL * 0.6f), Offset(sizeL * 0.6f, -sizeL * 0.6f),
            Offset(sizeL * 0.6f, sizeL * 0.6f), Offset(-sizeL * 0.6f, sizeL * 0.6f)
        )

        // Rotate points on canvas plane based on angle
        val rotated = points3D.map { p ->
            val rx = p.x * cos(angleRad) - p.y * sin(angleRad)
            val ry = p.x * sin(angleRad) + p.y * cos(angleRad)
            Offset(rx + centerX, ry + centerY)
        }

        // Draw orbital halos
        drawCircle(
            color = primaryColor,
            radius = sizeL * 1.4f,
            center = Offset(centerX, centerY),
            alpha = 0.15f,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = secondaryColor,
            radius = sizeL * 0.8f,
            center = Offset(centerX, centerY),
            alpha = 0.25f,
            style = Stroke(width = 1.dp.toPx())
        )

        // Draw connections representing 3D structure links
        val strokeW = 1.5.dp.toPx()
        val glowPaint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
        }

        // Back Face
        drawLine(primaryColor, rotated[0], rotated[1], strokeW)
        drawLine(primaryColor, rotated[1], rotated[2], strokeW)
        drawLine(primaryColor, rotated[2], rotated[3], strokeW)
        drawLine(primaryColor, rotated[3], rotated[0], strokeW)

        // Front Face
        drawLine(secondaryColor, rotated[4], rotated[5], strokeW)
        drawLine(secondaryColor, rotated[5], rotated[6], strokeW)
        drawLine(secondaryColor, rotated[6], rotated[7], strokeW)
        drawLine(secondaryColor, rotated[7], rotated[4], strokeW)

        // Connecting lines
        for (i in 0..3) {
            drawLine(
                brush = Brush.linearGradient(listOf(primaryColor, secondaryColor)),
                start = rotated[i],
                end = rotated[i + 4],
                strokeWidth = strokeW
            )
        }

        // Pulse core anchor
        drawCircle(
            color = Color.White,
            radius = (10 + 4 * sin(angleRad * 3)).dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

// Glowing Interactive Cosmic Orbit Canvas (Physics-derived Orbit simulations)
@Composable
fun InteractiveCosmoOrbitCanvas(
    modifier: Modifier = Modifier,
    files: List<CosmoFile>,
    matchingFileIds: List<Long>,
    laserTriggerTimestamp: Long, // Triggers direct beam line emissions
    onFileClicked: (CosmoFile) -> Unit
) {
    // Continuous time ticker for coordinates calculation
    val infiniteTransition = rememberInfiniteTransition(label = "OrbitTicker")
    val timeScale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ElapsedRotationTime"
    )

    // Store computed file coordinates to calculate clicked positions
    val fileCoordinatesMap = remember { mutableStateMapOf<Long, Offset>() }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(files) {
                    detectTapGestures { clickOffset ->
                        // Calculate click distance from stored coordinates
                        files.forEach { file ->
                            val filePos = fileCoordinatesMap[file.id]
                            if (filePos != null) {
                                val dx = clickOffset.x - filePos.x
                                val dy = clickOffset.y - filePos.y
                                val distance = sqrt(dx * dx + dy * dy)
                                if (distance < 45f) { // Touch space padding
                                    CosmoAudio.playCosmicBeep(700.0, 100, 0.4f)
                                    onFileClicked(file)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Draw central sun/AI Core glow
            val radiusCore = 30.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF00F2FE), Color(0x00000000)),
                    center = Offset(centerX, centerY),
                    radius = radiusCore * 2.2f
                ),
                radius = radiusCore * 2.2f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = radiusCore,
                center = Offset(centerX, centerY)
            )

            // Draw system orbits rings
            val baseRadius1 = 120.dp
            val baseRadius2 = 180.dp
            val baseRadius3 = 240.dp
            
            listOf(baseRadius1, baseRadius2, baseRadius3).forEach { radius ->
                drawCircle(
                    color = Color(0x3B00F2FE),
                    radius = radius.toPx(),
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                )
            }

            // Draw file planets orbiting in real-time
            files.forEachIndexed { index, file ->
                // Calculate dynamic orbital trigonometry path
                val scaleFactor = when(index % 3) {
                    1 -> 1.5f
                    2 -> 0.8f
                    else -> 1.1f
                }
                
                val orbitRadiusPx = file.orbitRadius.dp.toPx()
                val speed = 0.5f / scaleFactor
                // Use file ID as a constant offset to avoid planetary stacking
                val startingOffset = (file.id * 75f) * (PI / 180f)
                val angle = (timeScale * speed * (PI / 180f) + startingOffset).toFloat()

                val fileX = centerX + orbitRadiusPx * cos(angle)
                val fileY = centerY + orbitRadiusPx * sin(angle)

                // Save calculated coordinates for click recognition
                fileCoordinatesMap[file.id] = Offset(fileX, fileY)

                val fileColor = Color(android.graphics.Color.parseColor(file.colorHex))
                val isMatched = matchingFileIds.contains(file.id)
                val beamPulse = System.currentTimeMillis() - laserTriggerTimestamp < 1500L

                // If matched, pulse outer neon halo ring larger
                if (isMatched && beamPulse) {
                    drawCircle(
                        color = fileColor,
                        radius = 28.dp.toPx(),
                        center = Offset(fileX, fileY),
                        alpha = 0.4f
                    )
                }

                // Node background glow
                drawCircle(
                    color = fileColor.copy(alpha = 0.25f),
                    radius = 20.dp.toPx(),
                    center = Offset(fileX, fileY)
                )

                // Actual planetary node
                drawCircle(
                    color = fileColor,
                    radius = 12.dp.toPx(),
                    center = Offset(fileX, fileY)
                )

                // Inside core white spark
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(fileX, fileY)
                )

                // Write node name tag labels
                val fontPaint = Paint().asFrameworkPaint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 10.dp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawContext.canvas.nativeCanvas.drawText(
                    file.name.take(16) + if (file.name.length > 16) ".." else "",
                    fileX - 16.dp.toPx(),
                    fileY + 4.dp.toPx(),
                    fontPaint
                )

                // Write type abbreviation inside sphere
                val tagPaint = Paint().asFrameworkPaint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 8.dp.toPx()
                    isAntiAlias = true
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    file.typeSymbol,
                    fileX,
                    fileY + 3.dp.toPx(),
                    tagPaint
                )

                // LASER BEAM ANIMS: Emit bright streaming particle beam if RAG match is active!
                if (isMatched && beamPulse) {
                    val progress = ((System.currentTimeMillis() - laserTriggerTimestamp) % 500) / 500f
                    val currentLaserPos = Offset(
                        fileX + (centerX - fileX) * progress,
                        fileY + (centerY - fileY) * progress
                    )

                    // Solid connection beam line
                    drawLine(
                        brush = Brush.linearGradient(listOf(fileColor, Color.White)),
                        start = Offset(fileX, fileY),
                        end = Offset(centerX, centerY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Shooting photon particle
                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = currentLaserPos
                    )
                }
            }
        }
    }
}

// Pulsating sci-fi voice audio resonance visualizer
@Composable
fun GlowingWaveVisualizer(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveTicker")
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "Phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -(2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "Phase2"
    )

    val ampScale by animateFloatAsState(
        targetValue = if (isSpeaking) 1.0f else 0.15f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "AmplitudeMultiplier"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxAmp = size.height * 0.28f * ampScale

        // Background glowing solar core represent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF9D4EDD).copy(alpha = 0.5f * ampScale), Color(0x00000000)),
                center = Offset(centerX, centerY)
            ),
            radius = (100 + ampScale * 50).dp.toPx()
        )

        // Draw sine wave 1 (Neon Cyan)
        val path1 = Path()
        val path2 = Path()
        
        path1.moveTo(0f, centerY)
        path2.moveTo(0f, centerY)

        val steps = 100
        val stepX = size.width / steps
        for (i in 0..steps) {
            val rx = i * stepX
            val theta1 = (i / steps.toFloat()) * (4 * PI) + phase1
            val ry1 = centerY + sin(theta1).toFloat() * maxAmp * sin((i / steps.toFloat()) * PI).toFloat()

            val theta2 = (i / steps.toFloat()) * (3 * PI) + phase2
            val ry2 = centerY + cos(theta2).toFloat() * (maxAmp * 0.7f) * sin((i / steps.toFloat()) * PI).toFloat()

            path1.lineTo(rx, ry1)
            path2.lineTo(rx, ry2)
        }

        drawPath(path1, Color(0xFF00F2FE), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path2, Color(0xFFFF007F), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Center visual indicator ball
        drawCircle(
            color = Color.White,
            radius = (30 + 10 * sin(phase1 * 3)).dp.toPx(),
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0xFF9D4EDD),
            radius = 26.dp.toPx(),
            center = Offset(centerX, centerY),
            alpha = 0.35f
        )
    }
}

// 3D-inspired Custom Canvas Charts (Columns, Radar, Thermal Matrix grid layers)
@Composable
fun CustomCosmoCharts(
    modifier: Modifier = Modifier,
    chartType: String, // "bar", "radar", "heatmap"
    dataJson: String?
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Simulated background mesh coordinate grid
        val gridLineColor = Color(0x1B00F2FE)
        for (gridX in 0..10) {
            val lx = (gridX / 10f) * width
            drawLine(gridLineColor, Offset(lx, 0f), Offset(lx, height), 1f)
        }
        for (gridY in 0..6) {
            val ly = (gridY / 6f) * height
            drawLine(gridLineColor, Offset(0f, ly), Offset(width, ly), 1f)
        }

        when (chartType) {
            "radar" -> {
                // Draws customized 5-sided spider system coordinates
                val centerX = width / 2f
                val centerY = height / 2f
                val maxRadius = min(width, height) * 0.40f

                val numAxes = 5
                val angles = List(numAxes) { i -> (i * 2 * PI / numAxes) - (PI / 2) }
                val axisLabels = listOf("Gemini", "GPT-4o", "Llama 3", "DeepSeek", "Nano Banana")
                val scalarValues = listOf(0.92f, 0.94f, 0.85f, 0.89f, 0.60f)

                // 1. Draw web concentric pentagons
                listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { scalar ->
                    val r = maxRadius * scalar
                    val pentPath = Path()
                    pentPath.moveTo(
                        (centerX + r * cos(angles[0])).toFloat(),
                        (centerY + r * sin(angles[0])).toFloat()
                    )
                    for (axis in 1 until numAxes) {
                        pentPath.lineTo(
                            (centerX + r * cos(angles[axis])).toFloat(),
                            (centerY + r * sin(angles[axis])).toFloat()
                        )
                    }
                    pentPath.close()
                    drawPath(pentPath, Color(0x3B6C90FF), style = Stroke(width = 1.dp.toPx()))
                }

                // 2. Draw axis lines with text indicators
                angles.forEachIndexed { idx, theta ->
                    val ax = (centerX + maxRadius * cos(theta)).toFloat()
                    val ay = (centerY + maxRadius * sin(theta)).toFloat()
                    drawLine(Color(0x3B00F2FE), Offset(centerX, centerY), Offset(ax, ay), 1.5f)

                    // Names labels
                    val padX = if (cos(theta) > 0.1) 12f else if (cos(theta) < -0.1) -75f else -25f
                    val padY = if (sin(theta) > 0.1) 15f else if (sin(theta) < -0.1) -10f else 0f
                    val paint = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 10.dp.toPx()
                        isFakeBoldText = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        axisLabels[idx],
                        ax + padX,
                        ay + padY,
                        paint
                    )
                }

                // 3. Draw active ratings polygon
                val activePoly = Path()
                activePoly.moveTo(
                    (centerX + maxRadius * scalarValues[0] * cos(angles[0])).toFloat(),
                    (centerY + maxRadius * scalarValues[0] * sin(angles[0])).toFloat()
                )
                for (axis in 1 until numAxes) {
                    activePoly.lineTo(
                        (centerX + maxRadius * scalarValues[axis] * cos(angles[axis])).toFloat(),
                        (centerY + maxRadius * scalarValues[axis] * sin(angles[axis])).toFloat()
                    )
                }
                activePoly.close()
                drawPath(activePoly, Color(0x7F00F2FE))
                drawPath(activePoly, Color(0xFF00F2FE), style = Stroke(width = 2.dp.toPx()))
            }

            "heatmap" -> {
                // Elegant thermal glowing dynamic grids
                val cols = 8
                val rows = 5
                val cw = width / cols
                val rh = height / rows

                for (c in 0 until cols) {
                    for (r in 0 until rows) {
                        // Math logic to construct beautiful pulsing color matrices
                        val pulse = (sin((c * 0.8) + (r * 0.5) + (System.currentTimeMillis() / 400.0)) + 1.0) / 2.0
                        val gridColor = Color(
                            red = (pulse * 0.2f + 0.12f).toFloat(),
                            green = (pulse * 0.8f + 0.1f).toFloat(),
                            blue = (pulse * 0.95f + 0.3f).toFloat(),
                            alpha = (pulse * 0.45f + 0.35f).toFloat()
                        )

                        drawRect(
                            color = gridColor,
                            topLeft = Offset(c * cw + 2f, r * rh + 2f),
                            size = Size(cw - 4f, rh - 4f)
                        )

                        // Light spark cores
                        drawCircle(
                            color = Color.White.copy(alpha = (pulse * 0.5).toFloat()),
                            radius = (pulse * 3.0f).dp.toPx(),
                            center = Offset(c * cw + cw / 2f, r * rh + rh / 2f)
                        )
                    }
                }
            }

            else -> {
                // "bar" chart display: Futuristic cylinder layout columns
                val items = listOf(
                    Pair("Gemini Core", 95f),
                    Pair("GPT Engine", 88f),
                    Pair("Llama Graph", 72f),
                    Pair("DeepSeek", 83f),
                    Pair("Banana", 45f)
                )

                val bottomY = height - 40f
                val availableH = height - 80f
                val colW = width / (items.size * 2 + 1)

                // Render coordinate baseline
                drawLine(Color(0xFF00F2FE), Offset(20f, bottomY), Offset(width - 20f, bottomY), 2.dp.toPx())

                items.forEachIndexed { i, data ->
                    val barX = (2 * i + 1) * colW
                    val barH = (data.second / 100f) * availableH
                    val barTop = bottomY - barH

                    // Glowing backdrop
                    drawRoundRect(
                        color = Color(0xFF9D4EDD).copy(alpha = 0.15f),
                        topLeft = Offset(barX, barTop),
                        size = Size(colW, barH),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Active neon turquoise cylinder
                    val cylGradient = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00F2FE), Color(0xFF0C074B))
                    )
                    drawRoundRect(
                        brush = cylGradient,
                        topLeft = Offset(barX, barTop),
                        size = Size(colW, barH),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Light peak line indicator
                    drawLine(
                        color = Color.White,
                        start = Offset(barX, barTop),
                        end = Offset(barX + colW, barTop),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Value label text
                    val labelPaint = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 9.dp.toPx()
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "${data.second.toInt()}%",
                        barX + colW / 2f,
                        barTop - 10f,
                        labelPaint
                    )

                    // X Axis text labels
                    drawContext.canvas.nativeCanvas.drawText(
                        data.first,
                        barX + colW / 2f,
                        bottomY + 25f,
                        labelPaint
                    )
                }
            }
        }
    }
}

// 3D holographic dial design enabling active selections of available neural models
@Composable
fun ModelRotatingSelector(
    activeModels: Set<String>,
    onToggleModel: (String) -> Unit
) {
    val modelsList = listOf("Gemini", "GPT-4o", "DeepSeek", "Llama 3", "Nano Banana")
    val colorsMap = mapOf(
        "Gemini" to Color(0xFF00F2FE),
        "GPT-4o" to Color(0xFF9D4EDD),
        "DeepSeek" to Color(0xFF39FF14),
        "Llama 3" to Color(0xFFFF8C00),
        "Nano Banana" to Color(0xFFFFFF00)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        modelsList.forEach { model ->
            val isSelected = activeModels.contains(model)
            val modelColor = colorsMap[model] ?: Color.Cyan

            // Pulsing shadow glow for active selections
            val infiniteTransition = rememberInfiniteTransition(label = "ActiveDot")
            val pBorderPulse by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.35f,
                animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "BorderPulse"
            )

            Box(
                modifier = Modifier
                    .width(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isSelected) (2 * pBorderPulse).dp else 1.dp,
                        color = if (isSelected) modelColor else Color(0x3BFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(if (isSelected) modelColor.copy(alpha = 0.15f) else Color(0x0CFFFFFF))
                    .clickable {
                        CosmoAudio.playCosmicBeep(550.0, 80, 0.5f)
                        onToggleModel(model)
                    }
                    .padding(10.dp)
                    .testTag("model_${model.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Custom aesthetic mini graphics representing the models
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(modelColor.copy(alpha = 0.2f), RoundedCornerShape(100))
                            .border(1f.dp, modelColor.copy(alpha = 0.6f), RoundedCornerShape(100)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (model) {
                            "Gemini" -> Icon(Icons.Default.Science, contentDescription = null, tint = modelColor, modifier = Modifier.size(18.dp))
                            "GPT-4o" -> Icon(Icons.Default.Language, contentDescription = null, tint = modelColor, modifier = Modifier.size(18.dp))
                            "DeepSeek" -> Icon(Icons.Default.Search, contentDescription = null, tint = modelColor, modifier = Modifier.size(18.dp))
                            "Llama 3" -> Icon(Icons.Default.Psychology, contentDescription = null, tint = modelColor, modifier = Modifier.size(18.dp))
                            "Nano Banana" -> Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = modelColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = model,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    // Activity indicator bar dot
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(if (isSelected) modelColor else Color.Transparent, RoundedCornerShape(100))
                    )
                }
            }
        }
    }
}
