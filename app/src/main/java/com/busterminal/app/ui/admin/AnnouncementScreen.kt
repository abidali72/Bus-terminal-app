package com.busterminal.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.busterminal.app.data.model.Announcement
import com.busterminal.app.ui.common.EmptyStateScreen
import com.busterminal.app.util.toFormattedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(
    announcements: List<Announcement>,
    adminName: String,
    adminId: String,
    onPost: (String, String, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit
) {
    var showPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPostDialog = true },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Post")
            }
        }
    ) { padding ->
        if (announcements.isEmpty()) {
            EmptyStateScreen(
                icon = Icons.Default.Campaign,
                title = "No Announcements",
                subtitle = "Post updates for companies and passengers",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(announcements, key = { it.id }) { announcement ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(announcement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onDelete(announcement.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(announcement.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Posted by ${announcement.createdByName} • ${announcement.createdAt.toFormattedDateTime()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPostDialog) {
        var title by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("New Announcement") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Title") }, singleLine = true,
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        label = { Text("Message") },
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPost(title, message, adminName, adminId)
                        showPostDialog = false
                    },
                    enabled = title.isNotBlank() && message.isNotBlank()
                ) { Text("Post") }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) { Text("Cancel") }
            }
        )
    }
}
