package com.busterminal.app.ui.company

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.busterminal.app.data.model.Bus
import com.busterminal.app.data.model.Company
import com.busterminal.app.data.model.Driver
import com.busterminal.app.ui.common.*
import com.busterminal.app.ui.theme.*
import com.busterminal.app.util.Constants


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDashboardScreen(
    company: Company?,
    buses: List<Bus>,
    drivers: List<Driver>,
    isLoading: Boolean,
    onAddBus: () -> Unit,
    onEditBus: (Bus) -> Unit,
    onDeleteBus: (String) -> Unit,
    onStatusChange: (String, String) -> Unit,
    onManageDrivers: () -> Unit
) {
    if (company == null) {
        LoadingScreen()
        return
    }

    // Check pending approval
    if (company.approvalStatus == Constants.STATUS_PENDING) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = StatusDelayed
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Awaiting Approval",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your company registration is under review. You'll be able to manage buses once approved by the terminal admin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    if (company.approvalStatus == Constants.STATUS_SUSPENDED) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(80.dp), tint = StatusCancelled)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Account Suspended", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Contact the terminal admin for support.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBus,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Bus")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                GradientHeader(
                    title = company.name,
                    subtitle = "Company Dashboard"
                )
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Buses",
                        value = buses.size.toString(),
                        icon = Icons.Default.DirectionsBus,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active",
                        value = buses.count { it.status == Constants.BUS_ACTIVE }.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = StatusActive,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Drivers",
                        value = drivers.size.toString(),
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Driver management button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = onManageDrivers
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Manage Drivers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${drivers.size} drivers registered", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Bus list header
            item {
                Text(
                    "Your Buses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (buses.isEmpty()) {
                item {
                    EmptyStateScreen(
                        icon = Icons.Default.DirectionsBus,
                        title = "No Buses Added Yet",
                        subtitle = "Tap + to add your first bus"
                    )
                }
            } else {
                items(buses, key = { it.id }) { bus ->
                    CompanyBusItem(
                        bus = bus,
                        onEdit = { onEditBus(bus) },
                        onDelete = { onDeleteBus(bus.id) },
                        onStatusChange = { newStatus -> onStatusChange(bus.id, newStatus) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompanyBusItem(
    bus: Bus,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Bus #${bus.busNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        bus.busName.ifEmpty { bus.busType },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = bus.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Layout: ${bus.seatLayoutType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${bus.busType} • ${bus.totalSeats} seats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Status toggle
                Box {
                    TextButton(onClick = { showStatusMenu = true }) {
                        Icon(Icons.Default.ToggleOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Status", style = MaterialTheme.typography.labelMedium)
                    }
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Active") },
                            onClick = { onStatusChange(Constants.BUS_ACTIVE); showStatusMenu = false },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusActive) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delayed") },
                            onClick = { onStatusChange(Constants.BUS_DELAYED); showStatusMenu = false },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = StatusDelayed) }
                        )
                        DropdownMenuItem(
                            text = { Text("Cancelled") },
                            onClick = { onStatusChange(Constants.BUS_CANCELLED); showStatusMenu = false },
                            leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = StatusCancelled) }
                        )
                    }
                }

                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelMedium)
                }

                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bus") },
            text = { Text("Are you sure you want to delete bus #${bus.busNumber}?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
