package com.example.tendaysplan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tendaysplan.ui.screens.SettingsScreen
import com.example.tendaysplan.ui.screens.StatisticsScreen
import com.example.tendaysplan.ui.screens.TodayScreen
import com.example.tendaysplan.ui.screens.YearOverviewScreen
import com.example.tendaysplan.ui.screens.ProgressScreen
import com.example.tendaysplan.ui.theme.TenDaysPlanTheme
import com.example.tendaysplan.ui.viewmodel.TodayViewModel
import com.example.tendaysplan.ui.viewmodel.YearOverviewViewModel
import com.example.tendaysplan.ui.viewmodel.ProgressViewModel
import com.example.tendaysplan.ui.viewmodel.StatisticsViewModel
import com.example.tendaysplan.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 * 使用Hilt进行依赖注入
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 选中的文件URI
    private var selectedFileUri: Uri? by mutableStateOf(null)

    // 文件选择器
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查存储权限
        checkStoragePermission()

        setContent {
            TenDaysPlanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TenDaysPlanApp(
                        onFilePickerLaunch = {
                            filePickerLauncher.launch("*/*")
                        },
                        selectedFileUri = selectedFileUri,
                        onFileConsumed = { selectedFileUri = null }
                    )
                }
            }
        }
    }

    /**
     * 检查存储权限（Android 12及以下需要）
     */
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 请求权限
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "存储权限被拒绝，导出功能可能无法使用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }
}

/**
 * 应用主入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenDaysPlanApp(
    onFilePickerLaunch: () -> Unit = {},
    selectedFileUri: Uri? = null,
    onFileConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCycleNumber by remember { mutableStateOf<Int?>(null) }
    val currentYear = java.time.LocalDate.now().year

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "十日谈",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("年总览") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("yearOverview") {
                            popUpTo("yearOverview") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("执行进度") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedCycleNumber = null
                        navController.navigate("progress") {
                            popUpTo("yearOverview") { inclusive = false }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("今天") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("today") {
                            popUpTo("yearOverview") { inclusive = false }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("统计") },
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate("statistics") {
                            popUpTo("yearOverview") { inclusive = false }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("设置") },
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        navController.navigate("settings") {
                            popUpTo("yearOverview") { inclusive = false }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "yearOverview",
            modifier = Modifier.padding(padding)
        ) {
            composable("yearOverview") {
                val viewModel: YearOverviewViewModel = hiltViewModel()
                YearOverviewScreen(
                    viewModel = viewModel,
                    onCycleClick = { cycleNumber ->
                        selectedCycleNumber = cycleNumber
                        selectedTab = 1
                        navController.navigate("progress") {
                            popUpTo("yearOverview") { inclusive = false }
                        }
                    }
                )
            }
            composable("progress") {
                val viewModel: ProgressViewModel = hiltViewModel()
                ProgressScreen(
                    viewModel = viewModel,
                    initialCycleNumber = selectedCycleNumber
                )
            }
            composable("today") {
                val viewModel: TodayViewModel = hiltViewModel()
                TodayScreen(viewModel = viewModel)
            }
            composable("statistics") {
                val viewModel: StatisticsViewModel = hiltViewModel()
                StatisticsScreen(viewModel = viewModel)
            }
            composable("settings") {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onFilePickerLaunch = onFilePickerLaunch,
                    selectedFileUri = selectedFileUri,
                    onFileConsumed = onFileConsumed
                )
            }
        }
    }
}
