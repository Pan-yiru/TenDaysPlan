package com.example.tendaysplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendaysplan.data.model.CycleEntity
import com.example.tendaysplan.ui.viewmodel.YearOverviewViewModel
import com.example.tendaysplan.utils.DateUtils
import kotlinx.coroutines.launch

/**
 * 年总览页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearOverviewScreen(
    viewModel: YearOverviewViewModel,
    onCycleClick: (Int) -> Unit  // 改为传递周期序号
) {
    val selectedYear by viewModel.selectedYear.collectAsState()
    val cycles by viewModel.cycles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf<CycleEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { viewModel.previousYear() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "上一年"
                            )
                        }
                        Text(
                            "$selectedYear 年",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.nextYear() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "下一年"
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
            if (isLoading && cycles.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cycles) { cycle ->
                        CycleCard(
                            cycle = cycle,
                            onClick = {
                                onCycleClick(cycle.cycleNumber)
                            },
                            onEditGoals = {
                                showEditDialog = cycle
                            }
                        )
                    }
                }
            }
        }
    }

    // 编辑目标对话框
    showEditDialog?.let { cycle ->
        EditGoalsDialog(
            cycle = cycle,
            onDismiss = { showEditDialog = null },
            onSave = { goal1, goal2, goal3 ->
                viewModel.updateCycleGoals(cycle.cycleId, goal1, goal2, goal3)
                showEditDialog = null
            }
        )
    }
}

/**
 * 周期卡片
 */
@Composable
fun CycleCard(
    cycle: CycleEntity,
    onClick: () -> Unit,
    onEditGoals: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "第 ${cycle.cycleNumber} 周期",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    DateUtils.formatDateRange(cycle.startDate, cycle.endDate),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val hasGoals = !cycle.goal1.isNullOrBlank() ||
                    !cycle.goal2.isNullOrBlank() ||
                    !cycle.goal3.isNullOrBlank()

            if (!hasGoals) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "点击编辑目标...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onEditGoals) {
                        Text("编辑")
                    }
                }
            } else {
                // 三个目标横排显示，每个用浅灰色小方框背景
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 目标1
                    GoalBox(
                        goal = cycle.goal1,
                        modifier = Modifier.weight(1f)
                    )

                    // 目标2
                    GoalBox(
                        goal = cycle.goal2,
                        modifier = Modifier.weight(1f)
                    )

                    // 目标3
                    GoalBox(
                        goal = cycle.goal3,
                        modifier = Modifier.weight(1f)
                    )
                }

                TextButton(
                    onClick = onEditGoals,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("编辑目标")
                }
            }
        }
    }
}

/**
 * 目标小方框组件
 */
@Composable
fun GoalBox(
    goal: String?,
    modifier: Modifier = Modifier
) {
    if (!goal.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer) // 使用主题的primaryContainer浅蓝色
                .padding(8.dp),
            contentAlignment = Alignment.Center // 居中对齐
        ) {
            Text(
                text = goal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, // 文字居中
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        // 空目标显示占位框
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 编辑目标对话框
 */
@Composable
fun EditGoalsDialog(
    cycle: CycleEntity,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?) -> Unit
) {
    var goal1 by remember { mutableStateOf(cycle.goal1 ?: "") }
    var goal2 by remember { mutableStateOf(cycle.goal2 ?: "") }
    var goal3 by remember { mutableStateOf(cycle.goal3 ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("编辑第 ${cycle.cycleNumber} 周期目标")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = goal1,
                    onValueChange = { goal1 = it },
                    label = { Text("目标 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = goal2,
                    onValueChange = { goal2 = it },
                    label = { Text("目标 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = goal3,
                    onValueChange = { goal3 = it },
                    label = { Text("目标 3") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        goal1.ifBlank { null },
                        goal2.ifBlank { null },
                        goal3.ifBlank { null }
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
