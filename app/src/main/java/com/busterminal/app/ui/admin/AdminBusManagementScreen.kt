package com.busterminal.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.busterminal.app.data.model.Bus
import com.busterminal.app.data.model.Company
import com.busterminal.app.data.model.Route
import com.busterminal.app.ui.common.StatusBadge
import com.busterminal.app.ui.theme.*
import com.busterminal.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBusManagementScreen(
    uiState: AdminRouteScheduleUiState,
    onAddBus: (Bus) -> Unit,
    onUpdateBus: (Bus) -> Unit,
    onSoftDeleteBus: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    getCompanyName: (String) -> String,
    getRouteName: (String) -> String,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBus by remember { mutableStateOf<Bus?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Bus?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bus Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Bus")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) },
                text = { Text("Add Bus") },
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
                value = uiState.busSearchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by bus number or name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.busSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Status filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusFilters = listOf("all" to "All", "active" to "Active", "maintenance" to "Maintenance", "disabled" to "Disabled")
                items(statusFilters) { (value, label) ->
                    FilterChip(
                        selected = uiState.busStatusFilter == value,
                        onClick = { onStatusFilterChange(value) },
                        label = { Text(label) },
                        leadingIcon = if (uiState.busStatusFilter == value) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Type filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val typeFilters = listOf("all" to "All Types", Constants.BUS_TYPE_AC to "AC", Constants.BUS_TYPE_NON_AC to "Non-AC", Constants.BUS_TYPE_SLEEPER to "Sleeper", Constants.BUS_TYPE_LUXURY to "Luxury")
                items(typeFilters) { (value, label) ->
                    FilterChip(
                        selected = uiState.busTypeFilter == value,
                        onClick = { onTypeFilterChange(value) },
                        label = { Text(label) }
                    )
                }
            }

            // Bus list
            if (uiState.filteredBuses.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No buses found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredBuses, key = { it.id }) { bus ->
                        BusManagementCard(
                            bus = bus,
                            companyName = getCompanyName(bus.companyId),
                            routeName = if (bus.routeId.isNotBlank()) getRouteName(bus.routeId) else "Not assigned",
                            onEdit = { editingBus = bus },
                            onDelete = { showDeleteDialog = bus }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Bus Dialog
    if (showAddDialog) {
        BusFormDialog(
            title = "Add New Bus",
            bus = null,
            companies = uiState.companies,
            routes = uiState.routes.filter { it.status == "active" },
            onDismiss = { showAddDialog = false },
            onSave = { bus ->
                onAddBus(bus)
                showAddDialog = false
            }
        )
    }

    // Edit Bus Dialog
    editingBus?.let { bus ->
        BusFormDialog(
            title = "Edit Bus",
            bus = bus,
            companies = uiState.companies,
            routes = uiState.routes.filter { it.status == "active" },
            onDismiss = { editingBus = null },
            onSave = { updated ->
                onUpdateBus(updated)
                editingBus = null
            }
        )
    }

    // Delete Confirmation
    showDeleteDialog?.let { bus ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusCancelled) },
            title = { Text("Disable Bus?") },
            text = { Text("This will disable bus \"${bus.busNumber}\". It will no longer be available for scheduling. Active schedules must be cancelled first.") },
            confirmButton = {
                Button(
                    onClick = {
                        onSoftDeleteBus(bus.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text("Disable")
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

// ─── Bus Management Card ─────────────────────────────────────
@Composable
private fun BusManagementCard(
    bus: Bus,
    companyName: String,
    routeName: String,
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
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(bus.busNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                StatusBadge(status = bus.status)
            }

            if (bus.busName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(bus.busName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Company", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(companyName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Route", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(routeName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(bus.busType, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Seats", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${bus.totalSeats}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fare", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rs. ${bus.baseFare}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }

            // Amenities
            if (bus.amenities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(bus.amenities) { amenity ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(amenity, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
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
                    Text("Disable")
                }
            }
        }
    }
}

// ─── Bus Form Dialog ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BusFormDialog(
    title: String,
    bus: Bus?,
    companies: List<Company>,
    routes: List<Route>,
    onDismiss: () -> Unit,
    onSave: (Bus) -> Unit
) {
    val isEditing = bus != null
    var busNumber by remember { mutableStateOf(bus?.busNumber ?: "") }
    var busName by remember { mutableStateOf(bus?.busName ?: "") }
    var selectedCompanyId by remember { mutableStateOf(bus?.companyId ?: "") }
    var selectedRouteId by remember { mutableStateOf(bus?.routeId ?: "") }
    var busType by remember { mutableStateOf(bus?.busType ?: Constants.BUS_TYPE_AC) }
    var totalSeats by remember { mutableStateOf(bus?.totalSeats?.toString() ?: "40") }
    var baseFare by remember { mutableStateOf(bus?.baseFare?.toString() ?: "") }
    var isActive by remember { mutableStateOf(bus?.status == Constants.BUS_ACTIVE) }
    var fieldError by remember { mutableStateOf<String?>(null) }

    val availableAmenities = listOf("WiFi", "Charging", "AC", "Water Bottle", "Blanket", "TV", "Reclining Seats", "Reading Light")
    var selectedAmenities by remember { mutableStateOf(bus?.amenities?.toSet() ?: emptySet()) }

    // Dropdown states
    var companyExpanded by remember { mutableStateOf(false) }
    var routeExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fieldError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusCancelled.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(error, modifier = Modifier.padding(8.dp), color = StatusCancelled, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Company dropdown
                ExposedDropdownMenuBox(
                    expanded = companyExpanded,
                    onExpandedChange = { companyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = companies.find { it.id == selectedCompanyId }?.name ?: "Select Company",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Company *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = companyExpanded, onDismissRequest = { companyExpanded = false }) {
                        companies.forEach { company ->
                            DropdownMenuItem(
                                text = { Text(company.name) },
                                onClick = {
                                    selectedCompanyId = company.id
                                    companyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Route dropdown
                ExposedDropdownMenuBox(
                    expanded = routeExpanded,
                    onExpandedChange = { routeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = routes.find { it.id == selectedRouteId }?.let { "${it.fromCity} → ${it.toCity}" } ?: "Select Route",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Route *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = routeExpanded, onDismissRequest = { routeExpanded = false }) {
                        routes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text("${route.fromCity} → ${route.toCity}") },
                                onClick = {
                                    selectedRouteId = route.id
                                    routeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = busNumber,
                    onValueChange = { busNumber = it },
                    label = { Text("Bus Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isEditing
                )

                OutlinedTextField(
                    value = busName,
                    onValueChange = { busName = it },
                    label = { Text("Bus Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Bus Type dropdown
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = busType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bus Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        listOf(Constants.BUS_TYPE_AC, Constants.BUS_TYPE_NON_AC, Constants.BUS_TYPE_SLEEPER, Constants.BUS_TYPE_LUXURY).forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = { busType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalSeats,
                        onValueChange = { totalSeats = it },
                        label = { Text("Seats *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = baseFare,
                        onValueChange = { baseFare = it },
                        label = { Text("Base Fare *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Amenities
                Text("Amenities", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableAmenities.forEach { amenity ->
                        FilterChip(
                            selected = amenity in selectedAmenities,
                            onClick = {
                                selectedAmenities = if (amenity in selectedAmenities) {
                                    selectedAmenities - amenity
                                } else {
                                    selectedAmenities + amenity
                                }
                            },
                            label = { Text(amenity, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

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
                    when {
                        selectedCompanyId.isBlank() -> { fieldError = "Select a company"; return@Button }
                        selectedRouteId.isBlank() -> { fieldError = "Select a route"; return@Button }
                        busNumber.isBlank() -> { fieldError = "Bus number is required"; return@Button }
                        totalSeats.toIntOrNull() == null || totalSeats.toInt() <= 0 -> { fieldError = "Enter valid seat count"; return@Button }
                        baseFare.toDoubleOrNull() == null || baseFare.toDouble() < 0 -> { fieldError = "Enter valid fare"; return@Button }
                    }
                    val newBus = (bus ?: Bus()).copy(
                        busNumber = busNumber.trim(),
                        busName = busName.trim(),
                        companyId = selectedCompanyId,
                        routeId = selectedRouteId,
                        busType = busType,
                        totalSeats = totalSeats.toIntOrNull() ?: 40,
                        baseFare = baseFare.toDoubleOrNull() ?: 0.0,
                        amenities = selectedAmenities.toList(),
                        status = if (isActive) Constants.BUS_ACTIVE else Constants.BUS_DISABLED
                    )
                    onSave(newBus)
                }
            ) {
                Text(if (isEditing) "Update" else "Add Bus")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
