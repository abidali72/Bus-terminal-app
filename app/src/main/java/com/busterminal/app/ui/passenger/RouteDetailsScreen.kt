package com.busterminal.app.ui.passenger

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.busterminal.app.data.model.Route
import com.busterminal.app.ui.common.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsScreen(
    uiState: PassengerUiState,
    onBack: () -> Unit,
    onSortBy: (SortOption) -> Unit,
    onBusDetailClick: (ScheduleWithBus) -> Unit
) {
    val route = uiState.selectedRoute ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${route.fromCity} → ${route.toCity}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            route.routeCode.ifEmpty { "Route Details" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ─── Route Summary Header ────────────────────────
            item {
                RouteSummaryCard(route = route)
            }

            // ─── Sort Bar ────────────────────────────────────
            item {
                SortBar(
                    selectedSort = uiState.sortOption,
                    onSortBy = onSortBy,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ─── Content States ──────────────────────────────
            if (uiState.isLoadingDetails) {
                item { SkeletonLoader() }
            } else if (uiState.detailsError != null) {
                item {
                    ErrorStateScreen(
                        message = uiState.detailsError,
                        onRetry = { /* Will be handled via ViewModel */ }
                    )
                }
            } else if (uiState.groupedByCompany.isEmpty()) {
                item {
                    EmptyStateScreen(
                        icon = Icons.Default.DirectionsBus,
                        title = "No Buses Available",
                        subtitle = "No buses are currently scheduled on this route"
                    )
                }
            } else {
                // ─── Company Groups ──────────────────────────
                item {
                    Text(
                        "${uiState.groupedByCompany.sumOf { it.schedules.size }} buses from ${uiState.groupedByCompany.size} companies",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                uiState.groupedByCompany.forEach { group ->
                    item(key = "header_${group.company.id}") {
                        CompanyGroupHeader(group = group)
                    }

                    items(
                        group.schedules,
                        key = { "schedule_${it.schedule.id}" }
                    ) { scheduleWithBus ->
                        ScheduleBusCard(
                            item = scheduleWithBus,
                            onClick = { onBusDetailClick(scheduleWithBus) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ─── Route Summary Card ──────────────────────────────────────

@Composable
private fun RouteSummaryCard(route: Route) {
    val durationText = remember(route.estimatedDurationMinutes) {
        val hours = route.estimatedDurationMinutes / 60
        val mins = route.estimatedDurationMinutes % 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // From → To
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        route.fromCity,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        route.toCity,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RouteInfoChip(
                    icon = Icons.Default.AccessTime,
                    label = "Duration",
                    value = durationText
                )
                if (route.distanceKm > 0) {
                    RouteInfoChip(
                        icon = Icons.Default.Straighten,
                        label = "Distance",
                        value = "${route.distanceKm.toInt()} km"
                    )
                }
                if (route.stops.isNotEmpty()) {
                    RouteInfoChip(
                        icon = Icons.Default.LocationOn,
                        label = "Stops",
                        value = "${route.stops.size}"
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

// ─── Sort Bar ────────────────────────────────────────────────

@Composable
private fun SortBar(
    selectedSort: SortOption,
    onSortBy: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortOptions = listOf(
        SortOption.DEPARTURE to "Departure",
        SortOption.PRICE to "Price",
        SortOption.DURATION to "Duration",
        SortOption.RATING to "Rating"
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier.height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(sortOptions) { (option, label) ->
            FilterChip(
                selected = selectedSort == option,
                onClick = { onSortBy(option) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                leadingIcon = if (selectedSort == option) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ─── Company Group Header ────────────────────────────────────

@Composable
private fun CompanyGroupHeader(group: CompanyBusGroup) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Company Logo placeholder
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.company.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (group.company.rating > 0) {
                        RatingBar(rating = group.company.rating)
                    }
                }

                // Bus count badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${group.schedules.size} buses",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Company quick stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (group.company.phone.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = group.company.phone,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "From Rs. ${String.format("%.0f", group.minFare)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                SeatAvailabilityBadge(availableSeats = group.totalAvailableSeats)
            }
        }
    }
}

// ─── Schedule Bus Card ───────────────────────────────────────

@Composable
private fun ScheduleBusCard(
    item: ScheduleWithBus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = item.schedule
    val bus = item.bus

    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val departureStr = remember(schedule.departureTime) {
        if (schedule.departureTime > 0) dateFormat.format(Date(schedule.departureTime)) else "TBD"
    }
    val arrivalStr = remember(schedule.arrivalTime) {
        if (schedule.arrivalTime > 0) dateFormat.format(Date(schedule.arrivalTime)) else "TBD"
    }
    val durationMinutes = remember(schedule.departureTime, schedule.arrivalTime) {
        if (schedule.departureTime > 0 && schedule.arrivalTime > 0) {
            ((schedule.arrivalTime - schedule.departureTime) / 60000).toInt()
        } else 0
    }
    val durationStr = remember(durationMinutes) {
        val h = durationMinutes / 60
        val m = durationMinutes % 60
        if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: Bus info + type badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bus?.busNumber ?: "Bus",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Type badge
                bus?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (it.busType.lowercase()) {
                            "luxury" -> Color(0xFF6A1B9A).copy(alpha = 0.12f)
                            "ac" -> Color(0xFF1565C0).copy(alpha = 0.12f)
                            "sleeper" -> Color(0xFF2E7D32).copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = it.busType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (it.busType.lowercase()) {
                                "luxury" -> Color(0xFF6A1B9A)
                                "ac" -> Color(0xFF1565C0)
                                "sleeper" -> Color(0xFF2E7D32)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time row: Departure → Arrival
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = departureStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Depart",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = durationStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(
                        modifier = Modifier.width(50.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = arrivalStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Arrive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: Price, seats, driver, details button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Text(
                    text = "Rs. ${String.format("%.0f", schedule.basePrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                SeatAvailabilityBadge(availableSeats = schedule.availableSeats)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Driver info + Amenities
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (schedule.driverName.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.driverName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        "View Details",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Amenities
            bus?.let {
                if (it.amenities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(it.amenities) { amenity ->
                            AmenityChip(amenity = amenity)
                        }
                    }
                }
            }
        }
    }
}
