package com.example.tendaysplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendaysplan.data.model.DayRecordEntity
import com.example.tendaysplan.ui.viewmodel.TodayViewModel
import com.example.tendaysplan.utils.DateUtils

/**
 * 任务数据类
 */
data class TaskItem(
    val index: Int,
    val text: String,
    val isCompleted: Boolean,
    val name: String = "",
    val detail: String = "",
    val time: String = ""
)

/**
 * 今天页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel
) {
    val currentCycle by viewModel.currentCycle.collectAsState()
    val todayDayRecord by viewModel.todayDayRecord.collectAsState()
    val dayInCycle by viewModel.dayInCycle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "今天",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoading && currentCycle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (currentCycle != null) {
            val cycle = currentCycle!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 周期信息卡片
                CycleInfoCard(
                    cycleNumber = cycle.cycleNumber,
                    dayInCycle = dayInCycle,
                    startDate = cycle.startDate,
                    endDate = cycle.endDate,
                    goal1 = cycle.goal1,
                    goal2 = cycle.goal2,
                    goal3 = cycle.goal3,
                    onEditGoals = {
                        // TODO: 添加编辑目标功能
                    }
                )

                // 今天任务卡片（动态列表）
                TodayTasksCard(
                    dayRecord = todayDayRecord,
                    dayInCycle = dayInCycle,
                    onTaskCompletionChange = { taskIndex, isChecked ->
                        viewModel.updateTaskCompletion(
                            viewModel.selectedDate.value,
                            taskIndex,
                            isChecked
                        )
                    },
                    onTaskDetailChange = { taskIndex, displayText, name, time ->
                        viewModel.updateTaskDetails(
                            viewModel.selectedDate.value,
                            taskIndex,
                            displayText,
                            name,
                            time
                        )
                    },
                    onTaskAdd = {
                        // 添加任务由对话框处理
                    },
                    onTaskDelete = { taskIndex ->
                        viewModel.deleteTask(
                            viewModel.selectedDate.value,
                            taskIndex
                        )
                    }
                )
            }
        }
    }
}

/**
 * 周期信息卡片
 */
@Composable
fun CycleInfoCard(
    cycleNumber: Int,
    dayInCycle: Int,
    startDate: String,
    endDate: String,
    goal1: String?,
    goal2: String?,
    goal3: String?,
    onEditGoals: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 周期标题
            Text(
                "第 $cycleNumber 周期 · 第 $dayInCycle 天",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 日期范围
            Text(
                DateUtils.formatDateRange(startDate, endDate),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 周期目标
            val hasGoals = !goal1.isNullOrBlank() || !goal2.isNullOrBlank() || !goal3.isNullOrBlank()

            if (hasGoals) {
                Text(
                    "本周期的目标：",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                listOfNotNull(goal1, goal2, goal3).forEachIndexed { index, goal ->
                    Text(
                        "${index + 1}. $goal",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "本周期暂无目标",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    TextButton(onClick = onEditGoals) {
                        Text("添加目标")
                    }
                }
            }
        }
    }
}

/**
 * 今天任务卡片（动态任务列表）
 */
@Composable
fun TodayTasksCard(
    dayRecord: DayRecordEntity?,
    dayInCycle: Int,
    onTaskCompletionChange: (Int, Boolean) -> Unit,
    onTaskDetailChange: (Int, String, String, String) -> Unit,
    onTaskAdd: () -> Unit,
    onTaskDelete: (Int) -> Unit
) {
    // 将dayRecord转换为任务列表
    val tasks = remember(dayRecord) {
        listOfNotNull(
            dayRecord?.task1?.takeIf { it.isNotEmpty() }?.let {
                TaskItem(1, it, dayRecord.task1Completed,
                    dayRecord.task1Name ?: "", dayRecord.task1Detail ?: "", dayRecord.task1Time ?: "")
            },
            dayRecord?.task2?.takeIf { it.isNotEmpty() }?.let {
                TaskItem(2, it, dayRecord.task2Completed,
                    dayRecord.task2Name ?: "", dayRecord.task2Detail ?: "", dayRecord.task2Time ?: "")
            },
            dayRecord?.task3?.takeIf { it.isNotEmpty() }?.let {
                TaskItem(3, it, dayRecord.task3Completed,
                    dayRecord.task3Name ?: "", dayRecord.task3Detail ?: "", dayRecord.task3Time ?: "")
            },
            dayRecord?.task4?.takeIf { it.isNotEmpty() }?.let {
                TaskItem(4, it, dayRecord.task4Completed,
                    dayRecord.task4Name ?: "", dayRecord.task4Detail ?: "", dayRecord.task4Time ?: "")
            },
            dayRecord?.task5?.takeIf { it.isNotEmpty() }?.let {
                TaskItem(5, it, dayRecord.task5Completed,
                    dayRecord.task5Name ?: "", dayRecord.task5Detail ?: "", dayRecord.task5Time ?: "")
            },
            dayRecord?.task6?.takeIf { it.isNotEmpty() }?.let {
                TaskItem(6, it, dayRecord.task6Completed,
                    dayRecord.task6Name ?: "", dayRecord.task6Detail ?: "", dayRecord.task6Time ?: "")
            }
        )
    }

    // 编辑对话框状态
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }
    var newTaskName by remember { mutableStateOf("") }
    var newTaskDetail by remember { mutableStateOf("") }
    var newTaskTime by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题行：包含天数和添加按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "第 $dayInCycle 天",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // 添加任务按钮（最多6个任务）
                if (tasks.size < 6) {
                    IconButton(
                        onClick = {
                            newTaskName = ""
                            newTaskDetail = ""
                            newTaskTime = ""
                            editingTask = TaskItem(-1, "", false)
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加任务",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 任务列表
            if (tasks.isEmpty()) {
                Text(
                    "暂无任务，点击 + 添加任务",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                tasks.forEach { task ->
                    TaskListItem(
                        task = task,
                        onTaskClick = { isChecked ->
                            onTaskCompletionChange(task.index, isChecked)
                        },
                        onEditClick = {
                            newTaskName = task.name
                            newTaskDetail = task.detail
                            newTaskTime = task.time
                            editingTask = task
                        },
                        onDeleteClick = {
                            onTaskDelete(task.index)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // 编辑/添加任务对话框
    editingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = {
                Text(if (task.index == -1) "添加新任务" else "编辑任务")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 事件名称
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text("事件名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 事件细节
                    OutlinedTextField(
                        value = newTaskDetail,
                        onValueChange = { newTaskDetail = it },
                        label = { Text("事件细节") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 任务时间
                    OutlinedTextField(
                        value = newTaskTime,
                        onValueChange = { newTaskTime = it },
                        label = { Text("任务时间") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("例如：2小时、30分钟") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskName.isNotBlank()) {
                            // 组合显示文本：名称 + 细节
                            val displayText = if (newTaskDetail.isNotBlank()) {
                                "$newTaskName - $newTaskDetail"
                            } else {
                                newTaskName
                            }
                            val textWithTime = if (newTaskTime.isNotBlank()) {
                                "$displayText ($newTaskTime)"
                            } else {
                                displayText
                            }

                            if (task.index == -1) {
                                // 添加新任务（找到第一个空的任务槽）
                                val nextEmptyIndex = (1..6).firstOrNull { index ->
                                    when (index) {
                                        1 -> dayRecord?.task1.isNullOrBlank()
                                        2 -> dayRecord?.task2.isNullOrBlank()
                                        3 -> dayRecord?.task3.isNullOrBlank()
                                        4 -> dayRecord?.task4.isNullOrBlank()
                                        5 -> dayRecord?.task5.isNullOrBlank()
                                        6 -> dayRecord?.task6.isNullOrBlank()
                                        else -> false
                                    }
                                }
                                nextEmptyIndex?.let { onTaskDetailChange(it, textWithTime, newTaskName, newTaskTime) }
                            } else {
                                // 编辑现有任务
                                onTaskDetailChange(task.index, textWithTime, newTaskName, newTaskTime)
                            }
                        }
                        editingTask = null
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 任务列表项：显示任务文本，可点击完成、编辑或删除
 */
@Composable
fun TaskListItem(
    task: TaskItem,
    onTaskClick: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick(!task.isCompleted) },
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：复选框和任务文本
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onTaskClick,
                    modifier = Modifier.size(24.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = task.text,
                    fontSize = 16.sp,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )
            }

            // 右侧：编辑和删除按钮
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

