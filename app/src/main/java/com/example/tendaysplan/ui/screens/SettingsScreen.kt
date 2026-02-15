package com.example.tendaysplan.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tendaysplan.ui.viewmodel.SettingsViewModel

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onFilePickerLaunch: () -> Unit,
    selectedFileUri: Uri?,
    onFileConsumed: () -> Unit
) {
    val exportResult by viewModel.exportResult.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // 当有文件被选中时，自动导入
    LaunchedEffect(selectedFileUri) {
        selectedFileUri?.let { uri ->
            viewModel.importData(uri)
            onFileConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 数据管理部分
                item {
                    Text(
                        "数据管理",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 导出按钮
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "导出数据",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "将所有数据导出为JSON文件，支持Base64编码",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.exportData() },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("导出中...")
                                } else {
                                    Text("导出数据")
                                }
                            }
                        }
                    }
                }

                // 导入按钮
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "导入数据",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "从JSON文件导入数据，将覆盖现有数据",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onFilePickerLaunch,
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("选择文件导入")
                            }
                        }
                    }
                }

                // 清除数据按钮
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "清除所有数据",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "警告：此操作不可恢复",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showClearConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("清除所有数据")
                            }
                        }
                    }
                }

                // 应用信息部分
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "关于",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "十日谈",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "版本 1.0.0",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "10天周期目标设定和进度跟踪应用",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    // 导出结果对话框
    exportResult?.let { result ->
        when (result) {
            is SettingsViewModel.ExportResult.Success -> {
                AlertDialog(
                    onDismissRequest = { viewModel.resetResults() },
                    title = { Text("导出成功") },
                    text = {
                        Text("已导出 ${result.recordCount} 条记录\n文件路径: ${result.uri}")
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.resetResults() }) {
                            Text("确定")
                        }
                    }
                )
            }
            is SettingsViewModel.ExportResult.Error -> {
                AlertDialog(
                    onDismissRequest = { viewModel.resetResults() },
                    title = { Text("导出失败") },
                    text = { Text(result.message) },
                    confirmButton = {
                        Button(onClick = { viewModel.resetResults() }) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }

    // 导入结果对话框
    importResult?.let { result ->
        when (result) {
            is SettingsViewModel.ImportResult.Success -> {
                AlertDialog(
                    onDismissRequest = { viewModel.resetResults() },
                    title = { Text("导入成功") },
                    text = {
                        Text("已导入 ${result.cycleCount} 个周期和 ${result.recordCount} 条记录")
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.resetResults() }) {
                            Text("确定")
                        }
                    }
                )
            }
            is SettingsViewModel.ImportResult.Error -> {
                AlertDialog(
                    onDismissRequest = { viewModel.resetResults() },
                    title = { Text("导入失败") },
                    text = { Text(result.message) },
                    confirmButton = {
                        Button(onClick = { viewModel.resetResults() }) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }

    // 清除数据确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("确认清除") },
            text = {
                Text("确定要清除所有数据吗？此操作不可恢复！")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确定清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
