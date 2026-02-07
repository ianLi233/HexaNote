package com.notesassistant.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.notesassistant.app.R
import com.notesassistant.app.network.SemanticSearchResult
import com.notesassistant.app.ui.theme.*
import com.notesassistant.app.viewmodel.ChatMessage
import com.notesassistant.app.viewmodel.ChatMode
import com.notesassistant.app.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: NotesViewModel) {
    val state by viewModel.state.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        Header()
        
        ModeTabs(
            currentMode = state.mode, 
            onModeChange = { viewModel.setMode(it) }
        )
        
        Box(modifier = Modifier.weight(1f)) {
            if (state.mode == ChatMode.RAG) {
                MessagesList(
                    messages = state.messages, 
                    isProcessing = state.isProcessing,
                    isSpeaking = state.isSpeaking,
                    onSpeak = { viewModel.speakText(it) },
                    onStopSpeaking = { viewModel.stopSpeaking() }
                )
            } else {
                SearchResultsList(
                    results = state.searchResults, 
                    isProcessing = state.isProcessing,
                    isTranscribing = state.isTranscribing,
                    query = state.searchQuery,
                    onNoteClick = { viewModel.selectNote(it) }
                )
            }
            
            // Error Overlay
            state.errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(error, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                }
            }
        }
        
        InputArea(
            mode = state.mode,
            input = inputText,
            onInputChange = { inputText = it },
            onSend = {
                viewModel.sendQuery(inputText)
                inputText = ""
            },
            isRecording = state.isRecording,
            onRecordClick = { viewModel.toggleRecording() },
            isProcessing = state.isProcessing,
            isSpeaking = state.isSpeaking,
            onStopSpeaking = { viewModel.stopSpeaking() }
        )
    }

    // Note Detail Dialog
    state.selectedNote?.let { note ->
        NoteDetailDialog(
            note = note,
            isSpeaking = state.isSpeaking,
            onSpeak = { viewModel.speakText(note.content) },
            onStopSpeaking = { viewModel.stopSpeaking() },
            onDismiss = { 
                viewModel.stopSpeaking()
                viewModel.selectNote(null) 
            }
        )
    }
}

@Composable
fun Header() {
    Surface(
        color = Slate900.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "App Logo",
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "HexaNote Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Powered by Ollama and Weaviate", 
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun ModeTabs(currentMode: ChatMode, onModeChange: (ChatMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900.copy(alpha = 0.8f))
            .border(width = 0.5.dp, color = Slate800)
    ) {
        TabItem(
            text = "RAG Chat",
            icon = Icons.Default.Message,
            isSelected = currentMode == ChatMode.RAG,
            activeColor = Cyan400,
            onClick = { onModeChange(ChatMode.RAG) },
            modifier = Modifier.weight(1f)
        )
        TabItem(
            text = "Semantic Search",
            icon = Icons.Default.Search,
            isSelected = currentMode == ChatMode.SEMANTIC,
            activeColor = Purple400,
            onClick = { onModeChange(ChatMode.SEMANTIC) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) activeColor else Slate400
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text, 
                color = if (isSelected) activeColor else Slate400,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(40.dp)
                    .background(activeColor)
            )
        }
    }
}

@Composable
fun MessagesList(
    messages: List<ChatMessage>, 
    isProcessing: Boolean,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (messages.isEmpty() && !isProcessing) {
        EmptyState(
            icon = painterResource(id = R.drawable.icon),
            title = "Ask anything about your notes...",
            subtitle = "AI will search and synthesize answers from your notes"
        )
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    onSpeak = { onSpeak(message.content) },
                    onStopSpeaking = onStopSpeaking,
                    isSpeaking = isSpeaking
                )
            }
            if (isProcessing) {
                item {
                    TypingIndicator()
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onStopSpeaking: () -> Unit,
    isSpeaking: Boolean
) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            AppAvatar()
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Surface(
            color = if (isUser) Cyan600.copy(alpha = 0.1f) else Slate800,
            shape = RoundedCornerShape(12.dp),
            border = if (isUser) border(Cyan600.copy(alpha = 0.2f)) else border(Slate700),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = if (isUser) Cyan100 else Slate100,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (!isUser) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { if (isSpeaking) onStopSpeaking() else onSpeak() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "TTS",
                                tint = if (isSpeaking) Cyan400 else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Avatar(color = Cyan600, icon = Icons.Default.Person)
        }
    }
}

@Composable
fun AppAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Slate800),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.icon), 
            contentDescription = null, 
            modifier = Modifier.size(20.dp), 
            tint = Color.Unspecified
        )
    }
}

@Composable
fun Avatar(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
    }
}

@Composable
fun border(color: Color) = BorderStroke(1.dp, color)

@Composable
fun SearchResultsList(
    results: List<SemanticSearchResult>, 
    isProcessing: Boolean,
    isTranscribing: Boolean,
    query: String,
    onNoteClick: (SemanticSearchResult) -> Unit
) {
    if (results.isEmpty() && !isProcessing && !isTranscribing) {
        EmptyState(
            icon = painterResource(id = R.drawable.icon),
            title = "Search for notes semantically...",
            subtitle = "Find notes by meaning, not just keywords"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isTranscribing) {
                item {
                    TranscriptionIndicator()
                }
            }
            
            if (query.isNotEmpty() && !isTranscribing) {
                item {
                    Surface(
                        color = Purple900.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = border(Purple900.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "SEARCH QUERY", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Purple400,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"$query\"", 
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            if (results.isNotEmpty()) {
                item {
                    Text(
                        "${results.size} RESULTS FOUND", 
                        style = MaterialTheme.typography.labelSmall,
                        color = Purple400,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(results) { result ->
                    SearchResultItem(result, onClick = { onNoteClick(result) })
                }
            } else if (isProcessing && !isTranscribing) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Purple400)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(result: SemanticSearchResult, onClick: () -> Unit) {
    Surface(
        color = Slate800,
        shape = RoundedCornerShape(12.dp),
        border = border(Slate700),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = Purple400)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        result.title ?: "Untitled Note", 
                        color = Slate100,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    result.relevanceScore?.let { score ->
                        Surface(
                            color = Purple900.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "${(score * 100).toInt()}% match",
                                color = Purple300,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    result.content,
                    color = Slate400,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NoteDetailDialog(
    note: SemanticSearchResult, 
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onStopSpeaking: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = Slate900,
            shape = RoundedCornerShape(16.dp),
            border = border(Slate700)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        "Note Content",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    
                    // TTS Button
                    IconButton(
                        onClick = { if (isSpeaking) onStopSpeaking() else onSpeak() }
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop Speaking" else "Speak Note",
                            tint = if (isSpeaking) Cyan400 else Color.White
                        )
                    }
                }
                
                Divider(color = Slate800)
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        note.title ?: "Untitled Note",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        note.tags?.forEach { tag ->
                            Surface(
                                color = Purple900.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp),
                                border = border(Purple900.copy(alpha = 0.5f)),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    tag,
                                    color = Purple300,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        note.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate200,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (note.createdAt != null) {
                        Text(
                            "Created: ${note.createdAt}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}

val Purple900 = Color(0xFF581C87)

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.painter.Painter, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Slate800)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, color = Slate400, style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, color = Slate700, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        AppAvatar()
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = Slate800,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp).padding(4.dp),
                strokeWidth = 2.dp,
                color = Purple400
            )
        }
    }
}

@Composable
fun TranscriptionIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = Purple400
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Transcribing speech...",
            color = Slate400,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputArea(
    mode: ChatMode,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isRecording: Boolean,
    onRecordClick: () -> Unit,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    onStopSpeaking: () -> Unit
) {
    Surface(
        color = Slate900.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop Speaking Button
            if (isSpeaking) {
                IconButton(
                    onClick = onStopSpeaking,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Slate800)
                ) {
                    Icon(
                        Icons.Default.VolumeOff,
                        contentDescription = "Stop Speaking",
                        tint = Cyan400
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Microphone Button
            Box(contentAlignment = Alignment.Center) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.2f))
                    )
                }
                IconButton(
                    onClick = onRecordClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color.Red else Slate800)
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Text Input
            TextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        if (mode == ChatMode.RAG) "Ask about notes..." else "Search notes...",
                        color = Slate400,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Slate800,
                    unfocusedContainerColor = Slate800,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Cyan400,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                trailingIcon = {
                    if (input.isNotBlank()) {
                        IconButton(
                            onClick = onSend,
                            enabled = !isProcessing,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (mode == ChatMode.RAG) Cyan600 else Purple600)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send", 
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

val Slate500 = Color(0xFF64748B)
