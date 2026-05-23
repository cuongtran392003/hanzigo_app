package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Enumeration for easy, compiler-safe tab navigation
enum class Screen(val title: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    Dashboard("Tổng quan", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    Flashcard("Thẻ Nhớ", Icons.Filled.Collections, Icons.Outlined.Collections),
    AILessons("Bài học AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    Assessment("Thi Thử", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    Community("Cộng Đồng", Icons.Filled.Forum, Icons.Outlined.Forum),
    Settings("Thiết Lập", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChineseAppNavigation(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val progress by viewModel.progressStats.collectAsStateWithLifecycle()
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hoa Ngữ Smartech",
                                fontWeight = FontWeight.Black,
                                fontSize = 21.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        // Small eye-friendly indicator toggle
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Đổi Chế Độ Học",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    Screen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.iconSelected else screen.iconUnselected,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardTab(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    Screen.Flashcard -> FlashcardsTab(viewModel = viewModel)
                    Screen.AILessons -> AILessonsTab(viewModel = viewModel)
                    Screen.Assessment -> AssessmentsTab(viewModel = viewModel)
                    Screen.Community -> CommunityTab(viewModel = viewModel)
                    Screen.Settings -> SettingsTab(viewModel = viewModel)
                }
            }
        }
    }
}

// Helper Components for Elegant Dark Theme Dashboard
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .height(115.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeklyProgressPreviewCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ tuần này",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Chi tiết",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Bar graphs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.55f, 0.7f, 0.2f)
                heights.forEachIndexed { idx, h ->
                    val color = if (idx == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(h)
                            .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "“Hôm qua là lịch sử, ngày mai là bí ẩn, còn hôm nay là món quà.”",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = TextGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== DASHBOARD TAB ====================
@Composable
fun DashboardTab(viewModel: StudyViewModel, onNavigate: (Screen) -> Unit) {
    val progress by viewModel.progressStats.collectAsStateWithLifecycle()
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()
    val allCards by viewModel.allFlashcards.collectAsStateWithLifecycle()

    val percent = (progress.currentXp.toFloat() / progress.nextLevelXp.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. App Header Row matching HTML specification
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Styled Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = progress.userName.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Chào buổi tối,",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Text(
                            text = progress.userName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Flame streak pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF2B2930), CircleShape)
                        .border(1.dp, Color(0xFF49454F), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "🔥", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress.streak} ngày",
                        color = Color(0xFFFFB4AB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Daily Goal Purple Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mục tiêu hàng ngày",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${progress.wordsMastered} / ${allCards.size.coerceAtLeast(1)} Từ mới",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = percent,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .width(140.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }

                    // Rounded square box with visual details
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏮", fontSize = 28.sp)
                    }
                }
            }
        }

        // 3. Quick Actions Grid styled EXACTLY per Design HTML with explicit icons, pill backgrounds, and click binds
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Thẻ ghi nhớ",
                        subtitle = "SRS Algorithm",
                        icon = Icons.Filled.Collections,
                        iconBgColor = MaterialTheme.colorScheme.primary, // #D0BCFF
                        iconTint = MaterialTheme.colorScheme.onPrimary, // #381E72
                        onClick = { onNavigate(Screen.Flashcard) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Luyện phát âm",
                        subtitle = "Voice AI",
                        icon = Icons.Filled.Mic,
                        iconBgColor = MaterialTheme.colorScheme.secondary, // #BBC3FF
                        iconTint = MaterialTheme.colorScheme.onSecondary, // #1D2B5B
                        onClick = { onNavigate(Screen.Flashcard) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Bài kiểm tra",
                        subtitle = "Weekly Review",
                        icon = Icons.Filled.Quiz,
                        iconBgColor = MaterialTheme.colorScheme.tertiary, // #F2B8B5
                        iconTint = MaterialTheme.colorScheme.onTertiary, // #601410
                        onClick = { onNavigate(Screen.Assessment) },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Cộng đồng",
                        subtitle = "Rank: Cao thủ",
                        icon = Icons.Filled.People,
                        iconBgColor = CorrectGreen, // #C2EAD0
                        iconTint = OnCorrectGreen, // #0A3818
                        onClick = { onNavigate(Screen.Community) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Progress bar chart widget styled perfectly matching HTML
        item {
            WeeklyProgressPreviewCard()
        }

        // 5. Mini quick review stat boxes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${dueCards.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Từ Ôn Tập",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = CorrectGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${progress.wordsMastered}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = CorrectGreen
                        )
                        Text(
                            text = "Đã Thuộc Lòng",
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 6. Custom Canvas Memory Distribution Ring
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // Calculate percentage
                            val total = allCards.size.coerceAtLeast(1)
                            val mastered = progress.wordsMastered
                            val learning = (total - mastered).coerceAtLeast(0)

                            val masteredAngle = (mastered.toFloat() / total.toFloat()) * 360f
                            val learningAngle = 360f - masteredAngle

                            drawArc(
                                color = CorrectGreen,
                                startAngle = -90f,
                                sweepAngle = masteredAngle,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )

                            if (learningAngle > 0f) {
                                drawArc(
                                    color = SecondaryLight.copy(alpha = 0.5f),
                                    startAngle = -90f + masteredAngle,
                                    sweepAngle = learningAngle,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val percentMastered = if (allCards.isEmpty()) 0 else (progress.wordsMastered * 100 / allCards.size)
                            Text(
                                text = "$percentMastered%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(text = "Đã thông thạo", fontSize = 9.sp, color = TextGray)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "Biểu Đồ Trí Nhớ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(CorrectGreen, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Thẻ nhớ đã thuộc: ${progress.wordsMastered}", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(SecondaryLight.copy(alpha = 0.5f), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Từ vựng đang nhớ: ${allCards.size - progress.wordsMastered}", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 4. Daily Gamification Quests (Nhiệm vụ Thử Thách Hàng Ngày)
        item {
            Column {
                Text(
                    text = "Thử Thách Hôm Nay 🎯",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        QuestItem(title = "Ôn tập 5 từ vựng qua Thẻ Nhớ", checked = dueCards.isEmpty() && allCards.isNotEmpty())
                        QuestItem(title = "Thực hành phát âm chuẩn cùng AI", checked = progress.pronunciationScore > 75)
                        QuestItem(title = "Đăng bài giao lưu giải đáp thắc mắc", checked = false)
                    }
                }
            }
        }

        // 5. Weekly placement summary and badge achievement
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = "Huy hiệu",
                        tint = GoldStar,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Danh Hiệu Hiện Tại: Hồng Hạc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Đã hoàn thành ${progress.wordsMastered / 5} bộ từ vựng cơ bản. Hãy tiếp tục ôn tập để lên cấp danh rực rỡ!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun QuestItem(title: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (checked) CorrectGreen else TextGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = if (checked) TextGray else MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .background(
                    if (checked) CorrectGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (checked) "+10 XP" else "10 XP",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (checked) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ==================== FLASHCARDS TAB ====================
@Composable
fun FlashcardsTab(viewModel: StudyViewModel) {
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()
    val allCards by viewModel.allFlashcards.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("Tất cả") }
    val filteredCards = remember(allCards, selectedCategory) {
        if (selectedCategory == "Tất cả") allCards else allCards.filter { it.category == selectedCategory }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    // Speech simulation text state
    val speechText by viewModel.speechText.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isEvaluating by viewModel.isEvaluating.collectAsStateWithLifecycle()
    val feedback by viewModel.pronunciationFeedback.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(filteredCards) {
        currentIndex = 0
        isFlipped = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top categories & Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Thẻ Nhớ Từ Vựng 🗂️",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm từ", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Thêm Từ", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Categories quick list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "Tất cả",
                    onClick = { selectedCategory = "Tất cả" },
                    label = { Text("Tất cả (${allCards.size})") }
                )
            }
            val cats = categories.ifEmpty { listOf("HSK 1", "Du Lịch", "Giao Tiếp") }
            items(cats) { cat ->
                val size = allCards.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("$cat ($size)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.School, contentDescription = null, tint = TextGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Đã ôn hết từ vựng chuyên mục này!", color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.simulateFastForwardTime() }) {
                        Text("Tua nhanh thời gian để ôn lại")
                    }
                }
            }
        } else {
            val card = filteredCards.getOrNull(currentIndex)
            if (card != null) {
                // Main Flashcard container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { isFlipped = !isFlipped }
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isFlipped) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(
                            targetState = isFlipped,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            }
                        ) { flipped ->
                            if (!flipped) {
                                // Front Side
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = card.character,
                                        fontSize = 68.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Nhấn để lật thẻ 🔄",
                                        fontSize = 12.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                // Back Side (Meaning & Extras)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = card.pinyin,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TertiaryDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = card.meaning,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Example details
                                    if (card.exampleCn.isNotEmpty()) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = card.exampleCn,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = card.exampleVi,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Strength badge indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(
                                color = if (card.strength >= 80) CorrectGreen.copy(alpha = 0.2f) else SecondaryLight.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Trí nhớ: ${card.strength}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (card.strength >= 80) CorrectGreen else SecondaryLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pronunciation Check Sub-panel Inside Flashcards Tab
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Phát Âm Chữ Này 🎙️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isListening) viewModel.stopListening() else viewModel.startListening()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isListening) MistakeRed else MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                                    contentDescription = "Thu giọng nói",
                                    tint = Color.White
                                )
                            }

                            // Quick manual simulation buttons to bypass micro limitations
                            Button(
                                onClick = { viewModel.skipOrSimulateSpeechInput(card.pinyin) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Mô phỏng khớp", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = { viewModel.evaluatePronunciation(card) },
                                enabled = speechText.isNotEmpty() && !isEvaluating,
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryLight)
                            ) {
                                if (isEvaluating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("AI Phân Tích", fontSize = 11.sp)
                                }
                            }
                        }

                        if (speechText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nói được: \"$speechText\"",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        feedback?.let { fb ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (fb.isCorrect) CorrectGreen.copy(alpha = 0.1f) else MistakeRed.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (fb.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    contentDescription = null,
                                    tint = if (fb.isCorrect) CorrectGreen else MistakeRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Điểm: ${fb.score}/100",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (fb.isCorrect) CorrectGreen else MistakeRed
                                    )
                                    Text(text = fb.comment, fontSize = 11.sp)
                                    if (fb.correctionTips.isNotEmpty()) {
                                        Text(text = "Mẹo: ${fb.correctionTips}", fontSize = 11.sp, color = TextGray)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom SuperMemo grading bar (only shown if Flipped)
                if (isFlipped) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Độ dễ nhớ với bạn thế nào?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SM2Button(text = "Rất khó", weight = 1) {
                                viewModel.submitFlashcardScore(card, 1)
                                isFlipped = false
                                if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                            }
                            SM2Button(text = "Cần ôn", weight = 2) {
                                viewModel.submitFlashcardScore(card, 2)
                                isFlipped = false
                                if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                            }
                            SM2Button(text = "Khá nhớ", weight = 3) {
                                viewModel.submitFlashcardScore(card, 3)
                                isFlipped = false
                                if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                            }
                            SM2Button(text = "Nhớ tốt", weight = 4) {
                                viewModel.submitFlashcardScore(card, 4)
                                isFlipped = false
                                if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                            }
                            SM2Button(text = "Thuộc lòng", weight = 5) {
                                viewModel.submitFlashcardScore(card, 5)
                                isFlipped = false
                                if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                            }
                        }
                    }
                } else {
                    // Quick next controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentIndex > 0) currentIndex-- else currentIndex = filteredCards.size - 1
                                isFlipped = false
                            },
                            modifier = Modifier.testTag("prev_card_btn")
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Trở lại")
                        }

                        Text(
                            text = "Thẻ ${currentIndex + 1} / ${filteredCards.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray
                        )

                        IconButton(
                            onClick = {
                                if (currentIndex < filteredCards.size - 1) currentIndex++ else currentIndex = 0
                                isFlipped = false
                            },
                            modifier = Modifier.testTag("next_card_btn")
                        ) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Tiếp theo")
                        }
                    }
                }
            }
        }
    }

    // Modal to write custom new words
    if (showAddDialog) {
        var charInput by remember { mutableStateOf("") }
        var pinyinInput by remember { mutableStateOf("") }
        var meaningInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("HSK 1") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Thêm Từ Vựng Mới 🌸", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = charInput,
                        onValueChange = { charInput = it },
                        label = { Text("Chữ Hán (Giản thể / Phồn thể)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pinyinInput,
                        onValueChange = { pinyinInput = it },
                        label = { Text("Phiên âm Pinyin") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = meaningInput,
                        onValueChange = { meaningInput = it },
                        label = { Text("Nghĩa Việt") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Chuyên mục (HSK 1, Du Lịch, Giao Tiếp...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Bỏ qua")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.addCustomFlashcard(charInput, pinyinInput, meaningInput, categoryInput)
                            showAddDialog = false
                        }) {
                            Text("Thêm Thẻ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SM2Button(text: String, weight: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when (weight) {
                1 -> MistakeRed
                2 -> SecondaryLight
                3 -> MaterialTheme.colorScheme.outline
                4 -> MaterialTheme.colorScheme.secondary
                else -> CorrectGreen
            }
        ),
        modifier = Modifier
            .weight(1f)
            .height(34.dp)
    ) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}


// ==================== LESSONS TAB (AI TUTOR) ====================
@Composable
fun AILessonsTab(viewModel: StudyViewModel) {
    val aiResult by viewModel.aiLessonContent.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingLesson.collectAsStateWithLifecycle()

    var customPrompt by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Bài Học Cá Nhân Hóa AI 🤖",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sử dụng trí tuệ nhân tạo để tự tạo thêm thẻ nhớ hoặc giải thích văn phong ngữ pháp chuẩn xác.",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        // Prompt inputs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Tôi muốn Biên Soạn Bài Học:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val samples = listOf("Giao tiếp ở hiệu thuốc", "Chủ đề Đồ ăn Trung Hoa", "Hàng không & Sân bay", "Mặc cả mua sắm")
                        items(samples) { sample ->
                            AssistChip(
                                onClick = { customPrompt = sample },
                                label = { Text(sample) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customPrompt,
                        onValueChange = { customPrompt = it },
                        placeholder = { Text("Nhập chủ đề muốn học (e.g. Hỏi đường, Giao tiếp nhà hàng...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.generateAILesson(customPrompt) },
                        enabled = customPrompt.isNotEmpty() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Biên Soạn Bằng Gemini AI")
                        }
                    }
                }
            }
        }

        // Live resulting box
        item {
            if (aiResult.isNotEmpty() || isGenerating) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sách Giáo Khoa Của Bạn:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom scrollable readable text panel
                        SelectionContainer {
                            Text(
                                text = aiResult,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Help, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chọn từ mẫu hoặc nhập chủ đề tự soạn ở trên!",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}


// ==================== ASSESSMENTS TAB (TESTING) ====================
@Composable
fun AssessmentsTab(viewModel: StudyViewModel) {
    val quizQuestions by viewModel.currentQuizQuestions.collectAsStateWithLifecycle()
    val currentIndex by viewModel.quizCurrentIndex.collectAsStateWithLifecycle()
    val userSelectedIdx by viewModel.quizSelectedAnswer.collectAsStateWithLifecycle()
    val totalScore by viewModel.quizScore.collectAsStateWithLifecycle()
    val quizCompleted by viewModel.quizCompleted.collectAsStateWithLifecycle()

    val assessmentsHistory by viewModel.weeklyAssessments.collectAsStateWithLifecycle()

    var modeQuizStarted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!modeQuizStarted) {
            // Dashboard style overview
            Text(
                text = "Kiểm Tra Trình Độ Hàng Tuần 📝",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Task, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Bài kiểm tra đánh giá tự động", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Khảo sát ngẫu nhiên 5 từ vựng bất kỳ bạn đang học. Đạt điểm cao (>80) để nhận Huy hiệu danh dự!",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.startWeeklyQuiz()
                            modeQuizStarted = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Phát Động Thi Tuần")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Lịch Sử Thi Thử ⏳", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (assessmentsHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bạn chưa thực hiện bài kiểm tra tuần này.", color = TextGray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(assessmentsHistory) { report ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(report.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "${report.score} / 100 điểm",
                                        color = if (report.score >= 80) CorrectGreen else SecondaryLight,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(report.dateCompleted)),
                                    fontSize = 10.sp,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Hệ thống: ${report.feedback}", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        } else {
            // Live Quiz Screen
            if (quizCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (totalScore >= 80) Icons.Filled.Stars else Icons.Filled.MilitaryTech,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hoàn Thành Thử Thách 🏆", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Kết quả: $totalScore / 100 Điểm",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (totalScore >= 80) "Quá giỏi! Trí tuệ của bạn thật rực rỡ, được tặng thêm rất nhiều EXP!" else "Có lỗi nhịp lỡ hẹn chút, cố lên nhé học viên!",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { modeQuizStarted = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Trở về Tổng Quan")
                    }
                }
            } else {
                val currentQuestion = quizQuestions.getOrNull(currentIndex)
                if (currentQuestion != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current progress index
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Câu hỏi ${currentIndex + 1} / ${quizQuestions.size}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "Điểm hiện tại: $totalScore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Progress line indicator
                        LinearProgressIndicator(
                            progress = (currentIndex.toFloat() / quizQuestions.size.toFloat()),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentQuestion.questionText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(20.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Choices list
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentQuestion.options.forEachIndexed { i, choice ->
                                val isSelected = userSelectedIdx == i
                                val isCorrectChoice = i == currentQuestion.correctAnswerIndex
                                val isWrongSelection = isSelected && !isCorrectChoice

                                val btnColor = when {
                                    userSelectedIdx != null && isCorrectChoice -> CorrectGreen
                                    userSelectedIdx != null && isWrongSelection -> MistakeRed
                                    isSelected -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val textColor = if (userSelectedIdx != null && (isCorrectChoice || isWrongSelection)) Color.White else MaterialTheme.colorScheme.onSurface

                                Button(
                                    onClick = { viewModel.selectQuizAnswer(i) },
                                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = choice, fontWeight = FontWeight.Bold, color = textColor)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls
                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            enabled = userSelectedIdx != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = if (currentIndex == quizQuestions.size - 1) "Kết thúc đánh giá" else "Tiếp tục"
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==================== COMMUNITY TAB ====================
@Composable
fun CommunityTab(viewModel: StudyViewModel) {
    val posts by viewModel.communityPosts.collectAsStateWithLifecycle()
    var showCreatePost by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diễn Đàn Giao Lưu 🎓",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Button(onClick = { showCreatePost = true }) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Đăng tin", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Cộng đồng tạm thời chưa có bài viết. Hãy mở phong trào tự viết bài thảo luận nhé!", color = TextGray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(posts) { post ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom visual stylized avatar
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = post.author.firstOrNull()?.toString()?.uppercase() ?: "K",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(post.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(post.timestamp)),
                                            fontSize = 9.sp,
                                            color = TextGray
                                        )
                                    }
                                }

                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(post.category, fontSize = 9.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = post.content, fontSize = 13.sp, lineHeight = 18.sp)

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.likeCommunityPost(post.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Favorite,
                                            contentDescription = "Thích",
                                            tint = MistakeRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${post.likes}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreatePost) {
        var postTitle by remember { mutableStateOf("") }
        var postCategory by remember { mutableStateOf("Thảo luận") }
        var postContent by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreatePost = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Đăng bài trao đổi kiến thức 💡", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Tiêu đề bài viết") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postCategory,
                        onValueChange = { postCategory = it },
                        label = { Text("Chuyên mục (Thảo luận, Thành ngữ, Kinh nghiệm, Hỏi đáp)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("Nội dung hay muốn chia sẻ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreatePost = false }) {
                            Text("Hủy")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.submitPost(postTitle, postContent, postCategory)
                            showCreatePost = false
                        }) {
                            Text("Đăng Bài")
                        }
                    }
                }
            }
        }
    }
}


// ==================== SETTINGS / PROFILE TAB ====================
@Composable
fun SettingsTab(viewModel: StudyViewModel) {
    val progress by viewModel.progressStats.collectAsStateWithLifecycle()
    val rTime by viewModel.reminderTime.collectAsStateWithLifecycle()
    val rEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()

    var editingName by remember { mutableStateOf(progress.userName) }
    var inputReminderTime by remember { mutableStateOf(rTime) }
    var inputReminderEnabled by remember { mutableStateOf(rEnabled) }

    LaunchedEffect(progress.userName) {
        editingName = progress.userName
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Hồ Sơ & Cài Đặt ⚙️",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Section: Edit profile name
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Thông Tin Học Sinh", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        label = { Text("Đổi biệt hiệu / Học danh") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.updateUserName(editingName) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cập nhật tên")
                    }
                }
            }
        }

        // Section: Personalized Smart Daily Reminders
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nhắc Nhở Học Tập Thông Minh ⏰", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = inputReminderEnabled,
                            onCheckedChange = { inputReminderEnabled = it }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (inputReminderEnabled) {
                        OutlinedTextField(
                            value = inputReminderTime,
                            onValueChange = { inputReminderTime = it },
                            label = { Text("Giờ nhắc nhở hàng ngày (HH:mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.saveReminderSettings(inputReminderEnabled, inputReminderTime) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Lưu cấu hình nhắc")
                    }
                }
            }
        }

        // Space debugging helpers (Pro tools)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Công cụ Hỗ trợ Học tập nhanh", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vòng lặp ngắt quãng được cấu hình tính theo ngày. Bạn có thể tua nhanh thời gian học để bắt buộc các chữ Hán đến lịch ôn tập ngay lập tức phục vụ nghiên cứu & thi thử.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.simulateFastForwardTime() },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tua nhanh lịch để Ôn Từ", color = Color.White)
                    }
                }
            }
        }

        item {
            Text(
                text = "Học Tiếng Trung - Hoa Ngữ v1.0.0. Hoạt động offline hoàn toàn.",
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
