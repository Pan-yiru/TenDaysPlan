package com.example.tendaysplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendaysplan.data.model.DayRecordEntity
import com.example.tendaysplan.ui.viewmodel.ProgressViewModel
import com.example.tendaysplan.utils.DateUtils
import java.time.LocalDate

/**
 * 执行进度页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    initialCycleNumber: Int? = null
) {
    // 如果有指定的初始周期，加载它
    LaunchedEffect(initialCycleNumber) {
        initialCycleNumber?.let {
            viewModel.loadCycleByIndex(DateUtils.getCurrentYear(), it)
        }
    }

    val currentCycle by viewModel.currentCycle.collectAsState()
    val dayRecords by viewModel.dayRecords.collectAsState()
    val previousDayRecord by viewModel.previousCycleDayRecord.collectAsState()
    val selectedDayInCycle by viewModel.selectedDayInCycle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showEditDialog by remember { mutableStateOf<DayRecordEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cycleNumber = currentCycle?.cycleNumber
                        IconButton(
                            onClick = { viewModel.previousCycle() },
                            enabled = cycleNumber != null && cycleNumber > 1
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "上一周期",
                                tint = if (cycleNumber != null && cycleNumber > 1)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        Column {
                            Text(
                                currentCycle?.let { "第 ${it.cycleNumber} 周期" } ?: "加载中...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            currentCycle?.let {
                                Text(
                                    "${DateUtils.formatDateRange(it.startDate, it.endDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.nextCycle() },
                            enabled = cycleNumber != null && cycleNumber < 36
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "下一周期",
                                tint = if (cycleNumber != null && cycleNumber < 36)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading && dayRecords.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "加载中...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else if (currentCycle != null) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 上一个周期同一天区域
                    PreviousCycleDaySection(
                        previousDayRecord = previousDayRecord,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )

                    // 日历选择区域（上下两排，每排5天）
                    DaysCalendarGrid(
                        dayRecords = dayRecords,
                        selectedDayInCycle = selectedDayInCycle,
                        onDaySelected = { dayInCycle ->
                            viewModel.onDaySelected(dayInCycle)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    // 分隔线 - 蓝色系
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        thickness = 2.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // 当天任务详情区域
                    DayTaskDetailSection(
                        dayRecords = dayRecords,
                        selectedDayInCycle = selectedDayInCycle,
                        onEditClick = { dayRecord ->
                            showEditDialog = dayRecord
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            } else {
                // 没有周期数据
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "暂无周期数据",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // 编辑每日任务对话框
    showEditDialog?.let { dayRecord ->
        EditDayRecordDialog(
            dayRecord = dayRecord,
            onDismiss = { showEditDialog = null },
            onSave = { task1, task2, task3, task4, task5, task6 ->
                viewModel.updateDayTasks(
                    dayRecord.date,
                    task1, task2, task3, task4, task5, task6
                )
                showEditDialog = null
            }
        )
    }
}

/**
 * 上一个周期同一天区域 (30%)
 */
@Composable
fun PreviousCycleDaySection(
    previousDayRecord: DayRecordEntity?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "上周期同一天",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (previousDayRecord == null) {
                Text(
                    "暂无记录",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        previousDayRecord?.let { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        DateUtils.formatDateWithWeekday(record.date),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val tasks = listOfNotNull(
                        record.task1, record.task2, record.task3,
                        record.task4, record.task5, record.task6
                    ).filter { it.isNotBlank() }

                    if (tasks.isEmpty()) {
                        Text(
                            "无记录",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        tasks.take(3).forEach { task ->
                            Text(
                                "• $task",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        if (tasks.size > 3) {
                            Text(
                                "... 还有 ${tasks.size - 3} 项",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "上一个周期同一天暂无记录",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * 日历网格 - 上下两排，每排5天，共10天
 */
@Composable
fun DaysCalendarGrid(
    dayRecords: List<DayRecordEntity>,
    selectedDayInCycle: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 第一排：第1-5天
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            repeat(5) { index ->
                val dayInCycle = index + 1
                val dayRecord = dayRecords.find { it.dayInCycle == dayInCycle }
                DayCalendarItem(
                    dayInCycle = dayInCycle,
                    dayRecord = dayRecord,
                    isSelected = selectedDayInCycle == dayInCycle,
                    onClick = { onDaySelected(dayInCycle) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 第二排：第6-10天
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            repeat(5) { index ->
                val dayInCycle = index + 6
                val dayRecord = dayRecords.find { it.dayInCycle == dayInCycle }
                DayCalendarItem(
                    dayInCycle = dayInCycle,
                    dayRecord = dayRecord,
                    isSelected = selectedDayInCycle == dayInCycle,
                    onClick = { onDaySelected(dayInCycle) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 单个日历方块
 * 三种状态：
 * - 白色：未发生或未来天
 * - 深蓝色：任务均已完成
 * - 浅蓝色（primaryContainer）：已发生但未完成所有任务
 */
@Composable
fun DayCalendarItem(
    dayInCycle: Int,
    dayRecord: DayRecordEntity?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 计算日期状态
    val today = LocalDate.now()
    val recordDate = dayRecord?.date?.let { LocalDate.parse(it) }

    // 判断是否是未来天
    val isFuture = recordDate != null && recordDate.isAfter(today)

    // 判断是否所有任务已完成
    val allTasksCompleted = dayRecord?.let { record ->
        val tasks = listOfNotNull(
            record.task1, record.task2, record.task3,
            record.task4, record.task5, record.task6
        ).filter { it.isNotBlank() }

        if (tasks.isEmpty()) false
        else {
            val completedCount = listOf(
                record.task1Completed, record.task2Completed, record.task3Completed,
                record.task4Completed, record.task5Completed, record.task6Completed
            ).count { it == true }
            completedCount == tasks.size
        }
    } ?: false

    // 根据状态确定颜色
    val backgroundColor = when {
        isFuture || dayRecord == null -> Color.White
        allTasksCompleted -> MaterialTheme.colorScheme.primary // 与"上周期同一天"标题相同的蓝色
        else -> MaterialTheme.colorScheme.primaryContainer // 使用主题的primaryContainer浅蓝色
    }

    val textColor = when {
        isFuture || dayRecord == null -> Color(0xFF757575)
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    // 渐变蓝色边缘（小范围渐变）
    val gradientBorder = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .drawBehind {
                // 绘制渐变边缘
                val strokeWidth = if (isSelected) 5.dp.toPx() else 3.dp.toPx()
                drawRoundRect(
                    brush = gradientBorder,
                    style = Stroke(width = strokeWidth),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayInCycle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (dayRecord != null && !isFuture) {
                Spacer(modifier = Modifier.height(4.dp))

                // 显示完成进度
                val tasks = listOfNotNull(
                    dayRecord.task1, dayRecord.task2, dayRecord.task3,
                    dayRecord.task4, dayRecord.task5, dayRecord.task6
                ).filter { it.isNotBlank() }

                if (tasks.isNotEmpty()) {
                    val completedCount = listOf(
                        dayRecord.task1Completed, dayRecord.task2Completed, dayRecord.task3Completed,
                        dayRecord.task4Completed, dayRecord.task5Completed, dayRecord.task6Completed
                    ).count { it == true }

                    Text(
                        text = "$completedCount/${tasks.size}",
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * 任务详情区域 - 显示选中天的具体任务
 */
@Composable
fun DayTaskDetailSection(
    dayRecords: List<DayRecordEntity>,
    selectedDayInCycle: Int,
    onEditClick: (DayRecordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDayRecord = dayRecords.find { it.dayInCycle == selectedDayInCycle }

    Column(
        modifier = modifier
            .background(Color.White)
            .padding(16.dp)
    ) {
        if (selectedDayRecord != null) {
            // 标题行：日期和编辑按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        DateUtils.formatDateWithWeekday(selectedDayRecord.date),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "第 ${selectedDayRecord.dayInCycle} 天",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                Button(
                    onClick = { onEditClick(selectedDayRecord) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("编辑任务", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 任务列表 - 可滚动区域
            val tasks = listOf(
                selectedDayRecord.task1 to selectedDayRecord.task1Completed,
                selectedDayRecord.task2 to selectedDayRecord.task2Completed,
                selectedDayRecord.task3 to selectedDayRecord.task3Completed,
                selectedDayRecord.task4 to selectedDayRecord.task4Completed,
                selectedDayRecord.task5 to selectedDayRecord.task5Completed,
                selectedDayRecord.task6 to selectedDayRecord.task6Completed
            )

            val hasTasks = tasks.any { it.first?.isNotBlank() == true }

            if (!hasTasks) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无任务，点击上方按钮添加",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEachIndexed { index, (task, completed) ->
                        if (!task.isNullOrBlank()) {
                            DayTaskItem(
                                taskNumber = index + 1,
                                taskText = task,
                                isCompleted = completed == true
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "暂无记录",
                    fontSize = 16.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

/**
 * 任务项组件
 */
@Composable
fun DayTaskItem(
    taskNumber: Int,
    taskText: String,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 任务编号圆圈
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$taskNumber",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = taskText,
            fontSize = 15.sp,
            color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF424242),
            modifier = Modifier.weight(1f)
        )

        if (isCompleted) {
            Text(
                text = "✓",
                fontSize = 18.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 编辑每日记录对话框
 */
@Composable
fun EditDayRecordDialog(
    dayRecord: DayRecordEntity,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?, String?, String?, String?) -> Unit
) {
    var task1 by remember { mutableStateOf(dayRecord.task1 ?: "") }
    var task2 by remember { mutableStateOf(dayRecord.task2 ?: "") }
    var task3 by remember { mutableStateOf(dayRecord.task3 ?: "") }
    var task4 by remember { mutableStateOf(dayRecord.task4 ?: "") }
    var task5 by remember { mutableStateOf(dayRecord.task5 ?: "") }
    var task6 by remember { mutableStateOf(dayRecord.task6 ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("编辑 ${DateUtils.formatDateForDisplay(dayRecord.date)}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "最多可添加6项任务",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val taskFields = listOf(
                    "任务 1" to task1,
                    "任务 2" to task2,
                    "任务 3" to task3,
                    "任务 4" to task4,
                    "任务 5" to task5,
                    "任务 6" to task6
                )

                taskFields.forEachIndexed { index, (label, initialTask) ->
                    var text by remember { mutableStateOf(initialTask) }
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            when (index) {
                                0 -> task1 = it
                                1 -> task2 = it
                                2 -> task3 = it
                                3 -> task4 = it
                                4 -> task5 = it
                                5 -> task6 = it
                            }
                        },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        task1.ifBlank { null },
                        task2.ifBlank { null },
                        task3.ifBlank { null },
                        task4.ifBlank { null },
                        task5.ifBlank { null },
                        task6.ifBlank { null }
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
