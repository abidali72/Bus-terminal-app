package com.busterminal.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
fun AdminDashboardScreen(
    companies: List<Company>,
    buses: List<Bus>,
    drivers: List<Driver>,
    pendingCompanies: List<Company>,
    onCompanyManagement: () -> Unit,
    onBusManagement: () -> Unit,
    onDriverManagement: () -> Unit,
    onAnnouncements: () -> Unit,
    onRouteManagement: () -> Unit = {},
    onScheduleManagement: () -> Unit = {}
) {
    val activeBuses = remember(buses) { buses.count { it.status == Constants.BUS_ACTIVE } }
    val delayedBuses = remember(buses) { buses.count { it.status == Constants.BUS_DELAYED } }
    val cancelledBuses = remember(buses) { buses.count { it.status == Constants.BUS_CANCELLED } }
    val activeRoutes = remember(buses) {
        buses.count { it.status == Constants.BUS_ACTIVE }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            GradientHeader(title = "Admin Dashboard", subtitle = "Terminal Management System")
        }

        // Pending approvals alert
        if (pendingCompanies.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusDelayed.copy(alpha = 0.1f)),
                    onClick = onCompanyManagement
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = StatusDelayed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${pendingCompanies.size} Pending Approval${if (pendingCompanies.size > 1) "s" else ""}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusDelayed)
                            Text("Tap to review companies", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        // Stats Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(title = "Companies", value = companies.size.toString(), icon = Icons.Default.Business, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    StatCard(title = "Total Buses", value = buses.size.toString(), icon = Icons.Default.DirectionsBus, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(title = "Drivers", value = drivers.size.toString(), icon = Icons.Default.People, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                    StatCard(title = "Active Routes", value = activeRoutes.toString(), icon = Icons.Default.Route, color = StatusActive, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(title = "Active", value = activeBuses.toString(), icon = Icons.Default.CheckCircle, color = StatusActive, modifier = Modifier.weight(1f))
                    StatCard(title = "Delayed", value = delayedBuses.toString(), icon = Icons.Default.Schedule, color = StatusDelayed, modifier = Modifier.weight(1f))
                    StatCard(title = "Cancelled", value = cancelledBuses.toString(), icon = Icons.Default.Cancel, color = StatusCancelled, modifier = Modifier.weight(1f))
                }
            }
        }

        // Quick actions
        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminActionCard(
                    title = "Company Management",
                    subtitle = "${companies.size} registered companies",
                    icon = Icons.Default.Business,
                    onClick = onCompanyManagement
                )
                AdminActionCard(
                    title = "Bus Management",
                    subtitle = "${buses.size} buses across all companies",
                    icon = Icons.Default.DirectionsBus,
                    onClick = onBusManagement
                )
                AdminActionCard(
                    title = "Driver Management",
                    subtitle = "${drivers.size} drivers registered",
                    icon = Icons.Default.People,
                    onClick = onDriverManagement
                )
                AdminActionCard(
                    title = "Announcements",
                    subtitle = "Post updates and announcements",
                    icon = Icons.Default.Campaign,
                    onClick = onAnnouncements
                )
                AdminActionCard(
                    title = "Route Management",
                    subtitle = "Create and manage bus routes",
                    icon = Icons.Default.Route,
                    onClick = onRouteManagement
                )
                AdminActionCard(
                    title = "Schedule Management",
                    subtitle = "Manage bus schedules and timings",
                    icon = Icons.Default.Schedule,
                    onClick = onScheduleManagement
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
