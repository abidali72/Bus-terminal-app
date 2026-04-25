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
import com.busterminal.app.data.model.Schedule
import com.busterminal.app.ui.common.StatusBadge
import com.busterminal.app.ui.theme.*
import com.busterminal.app.util.Constants
import java.text.SimpleDateFormat
import java.util.*

private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
private val dateTimeFormatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementScreen(
    uiState: AdminRouteScheduleUiState,
    onAddSchedule: (Schedule) -> Unit,
    onUpdateSchedule: (Schedule) -> Unit,
    onCancelSchedule: (String) -> Unit,
    onRouteFilterChange: (String) -> Unit,
    onCompanyFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    getBusNumber: (String) -> String,
    getRouteName: (String) -> String,
    getCompanyName: (String) -> String,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showCancelDialog by remember { mutableStateOf<Schedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Schedule")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                text = { Text("Add Schedule") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statusFilters = listOf(
                    "all" to "All",
                    Constants.SCHEDULE_SCHEDULED to "Scheduled",
                    Constants.SCHEDULE_LIVE to "Live",
                    Constants.SCHEDULE_COMPLETED to "Completed",
                    Constants.SCHEDULE_CANCELLED to "Cancelled"
                )
                items(statusFilters) { (value, label) ->
                    FilterChip(
                        selected = uiState.scheduleStatusFilter == value,
                        onClick = { onStatusFilterChange(value) },
                        label = { Text(label) },
                        leadingIcon = if (uiState.scheduleStatusFilter == value) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Route filter
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.scheduleRouteFilter == "all",
                        onClick = { onRouteFilterChange("all") },
                        label = { Text("All Routes") }
                    )
                }
                items(uiState.routes.filter { it.status == "active" }) { route ->
                    FilterChip(
                        selected = uiState.scheduleRouteFilter == route.id,
                        onClick = { onRouteFilterChange(route.id) },
                        label = { Text("${route.fromCity} → ${route.toCity}", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Stats
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
                        Text("${uiState.schedules.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.schedules.count { it.status == Constants.SCHEDULE_SCHEDULED }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusActive)
                        Text("Scheduled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.schedules.count { it.status == Constants.SCHEDULE_LIVE }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusDelayed)
                        Text("Live", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${uiState.schedules.count { it.status == Constants.SCHEDULE_CANCELLED }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusCancelled)
                        Text("Cancelled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Schedule list
            if (uiState.filteredSchedules.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No schedules found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredSchedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            busNumber = getBusNumber(schedule.busId),
                            routeName = getRouteName(schedule.routeId),
                            companyName = getCompanyName(schedule.companyId),
                            onEdit = {
                                if (schedule.status != Constants.SCHEDULE_CANCELLED) {
                                    editingSchedule = schedule
                                }
                            },
                            onCancel = {
                                if (schedule.status != Constants.SCHEDULE_CANCELLED) {
                                    showCancelDialog = schedule
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add Schedule Dialog
    if (showAddDialog) {
        ScheduleFormDialog(
            title = "Add Schedule",
            schedule = null,
            buses = uiState.buses.filter { it.status == Constants.BUS_ACTIVE },
            routes = uiState.routes.filter { it.status == "active" },
            onDismiss = { showAddDialog = false },
            onSave = { schedule ->
                onAddSchedule(schedule)
                showAddDialog = false
            }
        )
    }

    // Edit Schedule Dialog
    editingSchedule?.let { schedule ->
        ScheduleFormDialog(
            title = "Edit Schedule",
            schedule = schedule,
            buses = uiState.buses.filter { it.status == Constants.BUS_ACTIVE },
            routes = uiState.routes.filter { it.status == "active" },
            onDismiss = { editingSchedule = null },
            onSave = { updated ->
                onUpdateSchedule(updated)
                editingSchedule = null
            }
        )
    }

    // Cancel Confirmation
    showCancelDialog?.let { schedule ->
        AlertDialog(
            onDismissRequest = { showCancelDialog = null },
            icon = { Icon(Icons.Default.EventBusy, contentDescription = null, tint = StatusCancelled) },
            title = { Text("Cancel Schedule?") },
            text = { Text("This schedule for bus ${getBusNumber(schedule.busId)} will be cancelled. New bookings will be blocked and this will reflect immediately in the passenger app.") },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelSchedule(schedule.id)
                        showCancelDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled)
                ) {
                    Text("Cancel Schedule")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = null }) {
                    Text("Keep")
                }
            }
        )
    }
}

// ─── Schedule Card ───────────────────────────────────────────
@Composable
private fun ScheduleCard(
    schedule: Schedule,
    busNumber: String,
    routeName: String,
    companyName: String,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    val isCancelled = schedule.status == Constants.SCHEDULE_CANCELLED
    val cardAlpha = if (isCancelled) 0.6f else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(busNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                StatusBadge(status = schedule.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(routeName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Text(companyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))

            // Time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Departure", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (schedule.departureTime > 0) dateTimeFormatter.format(Date(schedule.departureTime)) else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Arrival", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (schedule.arrivalTime > 0) dateTimeFormatter.format(Date(schedule.arrivalTime)) else "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${schedule.availableSeats} seats", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rs. ${schedule.basePrice}", style = MaterialTheme.typography.bodySmall)
                }
                if (schedule.driverName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(schedule.driverName, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            }

            if (!isCancelled) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

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
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelled)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

// ─── Schedule Form Dialog ────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleFormDialog(
    title: String,
    schedule: Schedule?,
    buses: List<Bus>,
    routes: List<Route>,
    onDismiss: () -> Unit,
    onSave: (Schedule) -> Unit
) {
    val isEditing = schedule != null
    var selectedBusId by remember { mutableStateOf(schedule?.busId ?: "") }
    var departureHour by remember { mutableIntStateOf(if (schedule != null && schedule.departureTime > 0) {
        Calendar.getInstance().apply { timeInMillis = schedule.departureTime }.get(Calendar.HOUR_OF_DAY)
    } else 8) }
    var departureMinute by remember { mutableIntStateOf(if (schedule != null && schedule.departureTime > 0) {
        Calendar.getInstance().apply { timeInMillis = schedule.departureTime }.get(Calendar.MINUTE)
    } else 0) }
    var arrivalHour by remember { mutableIntStateOf(if (schedule != null && schedule.arrivalTime > 0) {
        Calendar.getInstance().apply { timeInMillis = schedule.arrivalTime }.get(Calendar.HOUR_OF_DAY)
    } else 12) }
    var arrivalMinute by remember { mutableIntStateOf(if (schedule != null && schedule.arrivalTime > 0) {
        Calendar.getInstance().apply { timeInMillis = schedule.arrivalTime }.get(Calendar.MINUTE)
    } else 0) }
    var fareOverride by remember { mutableStateOf(schedule?.basePrice?.toString() ?: "") }
    var status by remember { mutableStateOf(schedule?.status ?: Constants.SCHEDULE_SCHEDULED) }
    var fieldError by remember { mutableStateOf<String?>(null) }

    var busExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
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

                // Bus dropdown (not editable for existing schedules)
                ExposedDropdownMenuBox(
                    expanded = busExpanded,
                    onExpandedChange = { if (!isEditing) busExpanded = it }
                ) {
                    OutlinedTextField(
                        value = buses.find { it.id == selectedBusId }?.let { "${it.busNumber} - ${it.busName}" } ?: "Select Bus",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isEditing,
                        label = { Text("Bus *") },
                        trailingIcon = { if (!isEditing) ExposedDropdownMenuDefaults.TrailingIcon(expanded = busExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    if (!isEditing) {
                        ExposedDropdownMenu(expanded = busExpanded, onDismissRequest = { busExpanded = false }) {
                            buses.forEach { bus ->
                                DropdownMenuItem(
                                    text = { Text("${bus.busNumber} - ${bus.busName}") },
                                    onClick = {
                                        selectedBusId = bus.id
                                        busExpanded = false
                                        // Auto-set fare from bus
                                        if (fareOverride.isBlank()) {
                                            fareOverride = bus.baseFare.toString()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Departure time
                Text("Departure Time", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = departureHour.toString().padStart(2, '0'),
                        onValueChange = { departureHour = it.toIntOrNull()?.coerceIn(0, 23) ?: departureHour },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = departureMinute.toString().padStart(2, '0'),
                        onValueChange = { departureMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: departureMinute },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Arrival time
                Text("Arrival Time", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = arrivalHour.toString().padStart(2, '0'),
                        onValueChange = { arrivalHour = it.toIntOrNull()?.coerceIn(0, 23) ?: arrivalHour },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = arrivalMinute.toString().padStart(2, '0'),
                        onValueChange = { arrivalMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: arrivalMinute },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Fare override
                OutlinedTextField(
                    value = fareOverride,
                    onValueChange = { fareOverride = it },
                    label = { Text("Fare (Rs.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
                )

                // Status dropdown (only for editing)
                if (isEditing) {
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = status.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            listOf(Constants.SCHEDULE_SCHEDULED, Constants.SCHEDULE_LIVE, Constants.SCHEDULE_COMPLETED, Constants.SCHEDULE_DELAYED).forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.replaceFirstChar { it.uppercase() }) },
                                    onClick = { status = s; statusExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedBusId.isBlank()) {
                        fieldError = "Select a bus"
                        return@Button
                    }

                    // Build timestamps from hour/minute (using today's date as base)
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, departureHour)
                    cal.set(Calendar.MINUTE, departureMinute)
                    cal.set(Calendar.SECOND, 0)
                    val depTime = cal.timeInMillis

                    cal.set(Calendar.HOUR_OF_DAY, arrivalHour)
                    cal.set(Calendar.MINUTE, arrivalMinute)
                    var arrTime = cal.timeInMillis
                    // Handle next-day arrival
                    if (arrTime <= depTime) {
                        arrTime += 24 * 60 * 60 * 1000
                    }

                    val fare = fareOverride.toDoubleOrNull() ?: 0.0

                    val selectedBus = buses.find { it.id == selectedBusId }
                    val newSchedule = (schedule ?: Schedule()).copy(
                        busId = selectedBusId,
                        routeId = selectedBus?.routeId ?: "",
                        companyId = selectedBus?.companyId ?: "",
                        departureTime = depTime,
                        arrivalTime = arrTime,
                        basePrice = fare,
                        status = status
                    )
                    onSave(newSchedule)
                }
            ) {
                Text(if (isEditing) "Update" else "Create Schedule")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
