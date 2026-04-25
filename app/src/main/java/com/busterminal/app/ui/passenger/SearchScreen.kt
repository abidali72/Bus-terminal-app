package com.busterminal.app.ui.passenger

import androidx.compose.foundation.clickable
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
import com.busterminal.app.data.model.Route
import com.busterminal.app.ui.common.EmptyStateScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: PassengerUiState,
    onSearch: (from: String, to: String) -> Unit,
    onRouteClick: (Route) -> Unit,
    onClearSearch: () -> Unit
) {
    var fromCity by remember { mutableStateOf(uiState.searchFrom) }
    var toCity by remember { mutableStateOf(uiState.searchTo) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ─── Search Header ──────────────────────────────────
        Text(
            "Search Routes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Find the best route for your journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Search Card ────────────────────────────────────
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // From Field
                OutlinedTextField(
                    value = fromCity,
                    onValueChange = { fromCity = it },
                    label = { Text("From City") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.TripOrigin,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Swap button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val temp = fromCity
                            fromCity = toCity
                            toCity = temp
                        }
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            contentDescription = "Swap cities",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // To Field
                OutlinedTextField(
                    value = toCity,
                    onValueChange = { toCity = it },
                    label = { Text("To City") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Button
                Button(
                    onClick = { onSearch(fromCity.trim(), toCity.trim()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = fromCity.isNotBlank() || toCity.isNotBlank()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Routes", style = MaterialTheme.typography.titleSmall)
                }

                // Clear button
                if (fromCity.isNotBlank() || toCity.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            fromCity = ""
                            toCity = ""
                            onClearSearch()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Search")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Results ────────────────────────────────────────
        if (uiState.searchFrom.isNotBlank() || uiState.searchTo.isNotBlank()) {
            Text(
                "${uiState.filteredRoutes.size} routes found",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (uiState.searchFrom.isNotBlank() || uiState.searchTo.isNotBlank()) {
                if (uiState.filteredRoutes.isEmpty()) {
                    item {
                        EmptyStateScreen(
                            icon = Icons.Default.SearchOff,
                            title = "No Routes Found",
                            subtitle = "Try a different city name"
                        )
                    }
                } else {
                    items(uiState.filteredRoutes, key = { it.id }) { route ->
                        SearchRouteResultCard(
                            route = route,
                            onClick = { onRouteClick(route) }
                        )
                    }
                }
            } else {
                // Show all routes as suggestions
                item {
                    Text(
                        "Popular Routes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(uiState.routes.take(5), key = { it.id }) { route ->
                    SearchRouteResultCard(
                        route = route,
                        onClick = { onRouteClick(route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchRouteResultCard(
    route: Route,
    onClick: () -> Unit
) {
    val durationText = remember(route.estimatedDurationMinutes) {
        val hours = route.estimatedDurationMinutes / 60
        val mins = route.estimatedDurationMinutes % 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = route.fromCity,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = route.toCity,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (durationText.isNotEmpty() && durationText != "0m") {
                        Text(
                            text = "⏱ $durationText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (route.distanceKm > 0) {
                        Text(
                            text = "📏 ${route.distanceKm.toInt()} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
