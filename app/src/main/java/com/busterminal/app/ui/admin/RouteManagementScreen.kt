package com.busterminal.app.ui.admin

import androidx.compose.animation.AnimatedVisibility
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
import com.busterminal.app.data.model.Route
import com.busterminal.app.ui.common.StatusBadge
import com.busterminal.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteManagementScreen(
    uiState: AdminRouteScheduleUiState,
    onAddRoute: (Route) -> Unit,
    onUpdateRoute: (Route) -> Unit,
    onSoftDeleteRoute: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRoute by remember { mutableStateOf<Route?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Route?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Route")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.AddRoad, contentDescription = null) },
                text = { Text("Add Route") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.routeSearchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by city or route code...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.routeSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all" to "All", "active" to "Active", "inactive" to "Inactive").forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.routeStatusFilter == value,
                        onClick = { onStatusFilterChange(value) },
                        label = { Text(label) },
                        leadingIcon = if (uiState.routeStatusFilter == value) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Stats bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.routes.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.routes.count { it.status == "active" }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusActive)
                        Text("Active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.routes.count { it.status == "inactive" }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusCancelled)
                        Text("Inactive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Route list
            if (uiState.filteredRoutes.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No routes found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredRoutes, key = { it.id }) { route ->
                        RouteCard(
                            route = route,
                            onEdit = { editingRoute = route },
                            onDelete = { showDeleteDialog = route }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Route Dialog
    if (showAddDialog) {
        RouteFormDialog(
            title = "Add New Route",
            route = null,
            onDismiss = { showAddDialog = false },
            onSave = { route ->
                onAddRoute(route)
                showAddDialog = false
            }
        )
    }

    // Edit Route Dialog
    editingRoute?.let { route ->
        RouteFormDialog(
            title = "Edit Route",
            route = route,
            onDismiss = { editingRoute = null },
            onSave = { updated ->
                onUpdateRoute(updated)
                editingRoute = null
            }
        )
    }

    // Delete Confirmation
    showDeleteDialog?.let { route ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusCancelled) },
            title = { Text("Deactivate Route?") },
            text = { Text("This will set the route \"${route.fromCity} → ${route.toCity}\" to inactive. Active buses must be reassigned first.") },
            confirmButton = {
                Button(
                    onClick = {
                        onSoftDeleteRoute(route.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text("Deactivate")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─── Route Card ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteCard(
    route: Route,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        route.routeCode,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status = route.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Route direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(route.fromCity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Origin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(route.toCity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Destination", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${route.distanceKm} km", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${route.estimatedDurationMinutes} min", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Stops
            if (route.stops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Stops: ${route.stops.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelled)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deactivate")
                }
            }
        }
    }
}

// ─── Route Form Dialog ───────────────────────────────────────
@Composable
private fun RouteFormDialog(
    title: String,
    route: Route?,
    onDismiss: () -> Unit,
    onSave: (Route) -> Unit
) {
    var fromCity by remember { mutableStateOf(route?.fromCity ?: "") }
    var toCity by remember { mutableStateOf(route?.toCity ?: "") }
    var distance by remember { mutableStateOf(route?.distanceKm?.toString() ?: "") }
    var duration by remember { mutableStateOf(route?.estimatedDurationMinutes?.toString() ?: "") }
    var stops by remember { mutableStateOf(route?.stops?.joinToString(", ") ?: "") }
    var isActive by remember { mutableStateOf(route?.status != "inactive") }
    var fieldError by remember { mutableStateOf<String?>(null) }

    val isEditing = route != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fieldError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusCancelled.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(error, modifier = Modifier.padding(8.dp), color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
                    }
                }

                OutlinedTextField(
                    value = fromCity,
                    onValueChange = { fromCity = it },
                    label = { Text("Origin City *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isEditing,
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )
                OutlinedTextField(
                    value = toCity,
                    onValueChange = { toCity = it },
                    label = { Text("Destination City *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isEditing,
                    leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
                )
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Distance (km) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Estimated Duration (min) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                )
                OutlinedTextField(
                    value = stops,
                    onValueChange = { stops = it },
                    label = { Text("Intermediate Stops (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = null) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation
                    when {
                        fromCity.isBlank() || toCity.isBlank() -> {
                            fieldError = "Origin and destination are required"
                            return@Button
                        }
                        fromCity.trim().equals(toCity.trim(), ignoreCase = true) -> {
                            fieldError = "Origin and destination must be different"
                            return@Button
                        }
                        distance.toDoubleOrNull() == null || distance.toDouble() <= 0 -> {
                            fieldError = "Enter a valid distance"
                            return@Button
                        }
                        duration.toIntOrNull() == null || duration.toInt() <= 0 -> {
                            fieldError = "Enter a valid duration"
                            return@Button
                        }
                    }
                    val stopsList = if (stops.isBlank()) emptyList() else stops.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val newRoute = (route ?: Route()).copy(
                        fromCity = fromCity.trim(),
                        toCity = toCity.trim(),
                        distanceKm = distance.toDoubleOrNull() ?: 0.0,
                        estimatedDurationMinutes = duration.toIntOrNull() ?: 0,
                        stops = stopsList,
                        status = if (isActive) "active" else "inactive"
                    )
                    onSave(newRoute)
                }
            ) {
                Text(if (isEditing) "Update" else "Add Route")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
