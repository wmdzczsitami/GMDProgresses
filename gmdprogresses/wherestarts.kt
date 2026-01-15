package com.itami.gmdprogresses

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.itami.gmdprogresses.autofuncs.whatorganizelist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.itami.gmdprogresses.manylist.ExtremeDemonEntity
import com.itami.gmdprogresses.manylist.ProgressRecord
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List

// ... 상단 import 생략 ...

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GDApplication
        val dao = app.database.whichlist()

        lifecycleScope.launch(Dispatchers.IO) {
            val count = dao.getAllDemons().first().size
            if (count == 0) {
                val organizer = whatorganizelist()
                val demonList = organizer.parseDemonsFromAssets(applicationContext)
                dao.insertAll(demonList)
            }
        }

        // MainActivity.kt의 setContent 내부
        setContent {
            val dao = (application as GDApplication).database.whichlist()
            var currentScreen by remember { mutableStateOf("list") } // "list", "detail", "stats"
            var selectedDemon by remember { mutableStateOf<ExtremeDemonEntity?>(null) }

            when (currentScreen) {
                "stats" -> {
                    StatsScreen(
                        dao = dao,
                        onBack = { currentScreen = "list" },
                        onItemClick = { demon ->
                            selectedDemon = demon
                            currentScreen = "detail"
                        }
                    )
                }
                "detail" -> {
                    val records by dao.getRecordsForDemon(selectedDemon!!.id).collectAsState(initial = emptyList())
                    DemonDetailScreen(
                        demon = selectedDemon!!,
                        records = records,
                        onBack = { currentScreen = "list" },
                        onSaveProgress = { newPercent, videoUrl ->
                            // ... 기존 저장 로직 (똑같음) ...
                        }
                    )
                }
                else -> { // "list"
                    DemonListScreen(
                        dao = dao,
                        onItemClick = { demon ->
                            selectedDemon = demon
                            currentScreen = "detail"
                        },
                        onStatsClick = { currentScreen = "stats" },
                        onAccountClick = { /* 나중에 필요하면 구현 */ }
                    )
                }
            }
        }
    }
}




@Composable
fun DemonListScreen(
    dao: whichtoupdatelist,
    onItemClick: (ExtremeDemonEntity) -> Unit,
    onStatsClick: () -> Unit, // 전체 통계 화면 이동 콜백
    onAccountClick: () -> Unit // 계정 화면 이동 콜백
) {
    val allDemons by dao.getAllDemons().collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val filteredList = allDemons
        .filter { it.name.trim().startsWith(searchText, ignoreCase = true) }
        .sortedBy { it.rank }

    LaunchedEffect(searchText) {
        if (filteredList.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    // Scaffold를 사용하면 FAB(우측 하단 버튼) 배치가 매우 쉽습니다.
    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp) // 버튼 사이 간격
            ) {
                // 1. 전체 진행도(통계) 버튼
                ExtendedFloatingActionButton(
                    onClick = onStatsClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    text = { Text("Stats") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )

                // 2. 계정 시스템 버튼
                FloatingActionButton(
                    onClick = onAccountClick,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                }
            }
        }
    ) { paddingValues ->
        // PaddingValues는 Scaffold가 FAB 자리를 비워두도록 줍니다.
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search Demon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items = filteredList, key = { it.id }) { demon ->
                    Box(modifier = Modifier.clickable { onItemClick(demon) }) {
                        DemonItem(demon)
                    }
                }
            }
        }
    }
}



@Composable
fun DemonItem(demon: ExtremeDemonEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 순위 표시
        Text(
            text = "${demon.rank}.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        // 이름과 제작자 표시
        Column {
            Text(
                text = demon.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "ID: ${demon.id}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}


@Composable
fun DemonDetailScreen(
    demon: ExtremeDemonEntity,
    records: List<ProgressRecord>,
    onBack: () -> Unit,
    onSaveProgress: (Int, String) -> Unit
) {
    // 1. 필요한 상태 변수 선언 (누락된 부분)
    var progressInput by remember { mutableStateOf("") }
    var videoLinkInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 시스템 뒤로가기 처리
    BackHandler {
        onBack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 상단 바
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            // rank와 tier 표시
            Text(
                text = "Rank #${demon.rank} (Rating: ${demon.tier})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 레벨 정보 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(demon.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text("Published by ${demon.creator}", color = Color.Gray)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                DetailInfoRow("ID", demon.id.toString())
                DetailInfoRow("Length", demon.length)
                DetailInfoRow("Objects", demon.objects)
                DetailInfoRow("Verifier", demon.verifier)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 진행도 및 유튜브 링크 입력 섹션
        OutlinedTextField(
            value = progressInput,
            onValueChange = { if (it.all { c -> c.isDigit() }) progressInput = it },
            label = { Text("Progress (%)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = videoLinkInput,
            onValueChange = { videoLinkInput = it },
            label = { Text("YouTube Video Link") },
            placeholder = { Text("https://youtu.be/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val p = progressInput.toIntOrNull() ?: 0
                val url = videoLinkInput.trim()

                // 0% 저장 방지 및 링크 유효성 체크
                if (p in 1..100 && url.isNotBlank()) {
                    onSaveProgress(p, url)
                    progressInput = ""
                    videoLinkInput = ""
                    focusManager.clearFocus()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            // 입력값이 없을 때 버튼 비활성화
            enabled = progressInput.isNotEmpty() && videoLinkInput.isNotEmpty()
        ) {
            Text("PROGRESS SAVE WITH VIDEO")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 기록 리스트
        Text("Achievement History", style = MaterialTheme.typography.titleLarge)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(records) { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 링크가 유효한지 확인 후 실행 (튕김 방지)
                            val url = record.videoUrl.trim()
                            if (url.startsWith("http")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // 튕김 방지 토스트 알림
                                    android.widget.Toast.makeText(context, "Link Error", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${record.percent}%", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(record.date, color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("Watch Video ↗", color = Color.Red, fontSize = 14.sp)
                }
                Divider(thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text(text = value, color = Color.Black)
    }
}


enum class SortType { RANK, NEWEST, OLDEST }

@Composable
fun StatsScreen(
    dao: whichtoupdatelist,
    onBack: () -> Unit,
    onItemClick: (ExtremeDemonEntity) -> Unit
) {
    // 1. 모든 진행도 기록과 데몬 정보를 가져옵니다.
    // 주의: 실제 구현 시에는 DAO에 @Query("SELECT * FROM progress_records")
    // 를 반환하는 getAllRecords() 함수를 하나 만들어두어야 합니다.
    val allRecords by dao.getAllRecords().collectAsState(initial = emptyList())
    val allDemons by dao.getAllDemons().collectAsState(initial = emptyList())

    var sortType by remember { mutableStateOf(SortType.RANK) }

    // 뒤로가기 처리
    BackHandler { onBack() }

    // 데이터 조합: 기록(Record)과 맵 정보(Demon)를 합칩니다.
    val displayList = remember(allRecords, allDemons, sortType) {
        allRecords.mapNotNull { record ->
            val demon = allDemons.find { it.id == record.demonId }
            if (demon != null) record to demon else null
        }.sortedWith { a, b ->
            when (sortType) {
                SortType.RANK -> a.second.rank.compareTo(b.second.rank) // 순위 높은 순
                SortType.NEWEST -> b.first.date.compareTo(a.first.date) // 최신 날짜 순
                SortType.OLDEST -> a.first.date.compareTo(b.first.date) // 오래된 날짜 순
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Total Stats", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 정렬 버튼 세 개
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val buttonModifier = Modifier.weight(1f)

            SortButton("Rank", sortType == SortType.RANK, buttonModifier) { sortType = SortType.RANK }
            SortButton("New", sortType == SortType.NEWEST, buttonModifier) { sortType = SortType.NEWEST }
            SortButton("Old", sortType == SortType.OLDEST, buttonModifier) { sortType = SortType.OLDEST }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 기록 리스트
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(displayList) { (record, demon) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onItemClick(demon) }, // 클릭 시 해당 데몬 페이지로
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(demon.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Date: ${record.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = "${record.percent}%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}