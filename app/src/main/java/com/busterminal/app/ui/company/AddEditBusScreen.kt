package com.busterminal.app.ui.company

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.busterminal.app.data.model.Driver
import com.busterminal.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBusScreen(
    existingBus: Bus? = null,
    companyId: String,
    drivers: List<Driver>,
    isLoading: Boolean,
    onSave: (Bus) -> Unit,
    onBack: () -> Unit
) {
    val isEdit = existingBus != null

    var busName by remember { mutableStateOf(existingBus?.busName ?: "") }
    var busNumber by remember { mutableStateOf(existingBus?.busNumber ?: "") }
    var busType by remember { mutableStateOf(existingBus?.busType ?: Constants.BUS_TYPE_AC) }
    var seatLayoutType by remember { mutableStateOf(existingBus?.seatLayoutType ?: "2x2") }
    var totalSeats by remember { mutableStateOf(existingBus?.totalSeats?.toString() ?: "40") }
    var expandedType by remember { mutableStateOf(false) }
    var expandedLayout by remember { mutableStateOf(false) }

    // Amenities
    val allAmenities = listOf("WiFi", "Charging Ports", "Entertainment", "Reclining Seats", "Washroom")
    var selectedAmenities by remember {
        mutableStateOf(existingBus?.amenities?.toSet() ?: emptySet())
    }

    val busTypes = listOf(Constants.BUS_TYPE_AC, Constants.BUS_TYPE_NON_AC, Constants.BUS_TYPE_SLEEPER, Constants.BUS_TYPE_LUXURY, "Executive")
    val layoutTypes = listOf("2x2", "2x1", "Sleeper Layout")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Bus" else "Add New Bus") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bus Name
            OutlinedTextField(
                value = busName,
                onValueChange = { busName = it },
                label = { Text("Bus Name") },
                leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Bus Number
            OutlinedTextField(
                value = busNumber,
                onValueChange = { busNumber = it },
                label = { Text("Bus Number (Unique)") },
                leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Bus Type dropdown
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it }
            ) {
                OutlinedTextField(
                    value = busType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bus Type") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    busTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { busType = type; expandedType = false }
                        )
                    }
                }
            }

            // Seat Layout Type dropdown
            ExposedDropdownMenuBox(
                expanded = expandedLayout,
                onExpandedChange = { expandedLayout = it }
            ) {
                OutlinedTextField(
                    value = seatLayoutType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seat Layout Type") },
                    leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLayout) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedLayout, onDismissRequest = { expandedLayout = false }) {
                    layoutTypes.forEach { layout ->
                        DropdownMenuItem(
                            text = { Text(layout) },
                            onClick = { seatLayoutType = layout; expandedLayout = false }
                        )
                    }
                }
            }

            // Total Seats
            OutlinedTextField(
                value = totalSeats,
                onValueChange = { totalSeats = it },
                label = { Text("Total Seats") },
                leadingIcon = { Icon(Icons.Default.EventSeat, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Amenities
            Text(
                "Amenities",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column {
                allAmenities.forEach { amenity ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedAmenities.contains(amenity),
                            onCheckedChange = { checked ->
                                selectedAmenities = if (checked) {
                                    selectedAmenities + amenity
                                } else {
                                    selectedAmenities - amenity
                                }
                            }
                        )
                        Text(amenity, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    val bus = Bus(
                        id = existingBus?.id ?: "",
                        busNumber = busNumber,
                        companyId = companyId,
                        busName = busName,
                        busType = busType,
                        seatLayoutType = seatLayoutType,
                        totalSeats = totalSeats.toIntOrNull() ?: 40,
                        amenities = selectedAmenities.toList(),
                        status = existingBus?.status ?: Constants.BUS_ACTIVE
                    )
                    onSave(bus)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = busNumber.isNotBlank() && busName.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEdit) "Update Bus" else "Add Bus", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
