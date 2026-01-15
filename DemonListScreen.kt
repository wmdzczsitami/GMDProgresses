@Composable
fun DemonListScreen(dao: whichtoupdatelist, onUpdate: (ExtremeDemonEntity, Int) -> Unit) {
    // DB에서 실시간으로 데이터를 관찰 (데이터가 들어오면 자동으로 화면 갱신)
    val allDemons by dao.getAllDemons().collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    
    // 검색어에 따라 리스트 필터링 (Frost Spirit 입력 시 해당 맵만 남음)
    val filteredList = remember(searchText, allDemons) {
        allDemons.filter {
            it.name.contains(searchText, ignoreCase = true) || it.id.toString().contains(searchText)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // [상단 고정]
        Column(modifier = Modifier.padding(16.dp)) {
            Text("GMDProgresses", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            // 검색창
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Demon (e.g. Frost Spirit)") },
                singleLine = true
            )
        }

        // [중간 리스트] 
        // 데이터가 있으면 리스트를 보여주고, 없으면 로딩 메시지 표시
        if (allDemons.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator() // 로딩 중 빙글빙글
                Text("Loading 1,200 Demons...", modifier = Modifier.padding(top = 60.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().background(Color.LightGray)
            ) {
                items(items = filteredList, key = { it.id }) { demon ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 1.dp)
                            .background(Color.White)
                            .clickable { /* 수정 팝업 로직 */ }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${allDemons.indexOf(demon) + 1}. ${demon.name}", fontWeight = FontWeight.Bold)
                        Text("${demon.myProgress}%")
                    }
                }
            }
        }

        // [하단 고정]
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(16.dp)) {
            Text("My Stats", color = Color.White, modifier = Modifier.weight(1f).clickable { /* 통계 */ })
            Text("Settings", color = Color.White, modifier = Modifier.weight(1f).clickable { /* 설정 */ })
        }
    }
}