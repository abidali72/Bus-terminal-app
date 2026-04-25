package com.busterminal.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.busterminal.app.data.model.Company
import com.busterminal.app.ui.common.StatusBadge
import com.busterminal.app.ui.theme.*
import com.busterminal.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyManagementScreen(
    companies: List<Company>,
    onApprove: (String) -> Unit,
    onSuspend: (String) -> Unit,
    onBack: () -> Unit
) {
    var filterStatus by remember { mutableStateOf("all") }
    val filteredCompanies = remember(companies, filterStatus) {
        when (filterStatus) {
            "all" -> companies
            else -> companies.filter { it.approvalStatus == filterStatus }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all" to "All", Constants.STATUS_PENDING to "Pending", Constants.STATUS_APPROVED to "Approved", Constants.STATUS_SUSPENDED to "Suspended").forEach { (value, label) ->
                    FilterChip(
                        selected = filterStatus == value,
                        onClick = { filterStatus = value },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCompanies, key = { it.id }) { company ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(company.name.take(2).uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(company.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(company.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                StatusBadge(status = company.approvalStatus)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("📞 ${company.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (company.address.isNotEmpty()) {
                                Text("📍 ${company.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                when (company.approvalStatus) {
                                    Constants.STATUS_PENDING -> {
                                        Button(
                                            onClick = { onApprove(company.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusActive)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = { onSuspend(company.id) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reject")
                                        }
                                    }
                                    Constants.STATUS_APPROVED -> {
                                        OutlinedButton(
                                            onClick = { onSuspend(company.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelled)
                                        ) {
                                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Suspend")
                                        }
                                    }
                                    Constants.STATUS_SUSPENDED -> {
                                        Button(
                                            onClick = { onApprove(company.id) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reactivate")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
