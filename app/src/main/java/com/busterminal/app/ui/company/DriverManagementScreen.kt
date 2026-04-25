package com.busterminal.app.ui.company

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
import com.busterminal.app.data.model.Driver
import com.busterminal.app.ui.common.EmptyStateScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverManagementScreen(
    drivers: List<Driver>,
    companyId: String,
    onAddDriver: (Driver) -> Unit,
    onUpdateDriver: (Driver) -> Unit,
    onDeleteDriver: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<Driver?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Driver")
            }
        }
    ) { padding ->
        if (drivers.isEmpty()) {
            EmptyStateScreen(
                icon = Icons.Default.People,
                title = "No Drivers Yet",
                subtitle = "Add drivers to assign them to buses",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(drivers, key = { it.id }) { driver ->
                    DriverItem(
                        driver = driver,
                        onEdit = { editingDriver = driver; showAddDialog = true },
                        onDelete = { onDeleteDriver(driver.id) }
                    )
                }
            }
        }
    }

    // Add/Edit dialog
    if (showAddDialog) {
        DriverDialog(
            driver = editingDriver,
            companyId = companyId,
            onSave = { driver ->
                if (editingDriver != null) onUpdateDriver(driver) else onAddDriver(driver)
                showAddDialog = false
                editingDriver = null
            },
            onDismiss = { showAddDialog = false; editingDriver = null }
        )
    }
}

@Composable
private fun DriverItem(
    driver: Driver,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("📞 ${driver.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("🪪 License: ${driver.licenseNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${driver.experienceYears} years experience", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Driver") },
            text = { Text("Remove ${driver.name}?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverDialog(
    driver: Driver?,
    companyId: String,
    onSave: (Driver) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(driver?.name ?: "") }
    var phone by remember { mutableStateOf(driver?.phone ?: "") }
    var license by remember { mutableStateOf(driver?.licenseNumber ?: "") }
    var experienceYears by remember { mutableStateOf(driver?.experienceYears?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (driver != null) "Edit Driver" else "Add Driver") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = license, onValueChange = { license = it },
                    label = { Text("License Number") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = experienceYears, onValueChange = { experienceYears = it },
                    label = { Text("Experience (years)") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Driver(
                            id = driver?.id ?: "",
                            name = name,
                            phone = phone,
                            licenseNumber = license,
                            experienceYears = experienceYears.toIntOrNull() ?: 0,
                            companyId = companyId
                        )
                    )
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
