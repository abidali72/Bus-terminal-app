package com.busterminal.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.busterminal.app.ui.auth.AuthViewModel
import com.busterminal.app.ui.company.CompanyViewModel
import com.busterminal.app.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    companyViewModel: CompanyViewModel,
    onBack: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val companyState by companyViewModel.uiState.collectAsState()
    
    val user = authState.currentUser
    val company = companyState.company
    
    var name by remember { mutableStateOf(user?.name ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var city by remember { mutableStateOf(user?.city ?: "") }
    
    // Passenger fields
    var gender by remember { mutableStateOf(user?.gender ?: "") }
    var dob by remember { mutableStateOf(user?.dateOfBirth ?: "") }
    
    // Company fields
    var companyName by remember { mutableStateOf(company?.name ?: "") }
    var companyAddress by remember { mutableStateOf(company?.address ?: "") }
    var ownerName by remember { mutableStateOf(company?.ownerName ?: "") }
    var registrationNumber by remember { mutableStateOf(company?.registrationNumber ?: "") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe success messages
    LaunchedEffect(authState.actionSuccess) {
        authState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    LaunchedEffect(companyState.actionSuccess) {
        companyState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            if (user?.role == Constants.ROLE_PASSENGER) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            if (user?.role == Constants.ROLE_COMPANY && company != null) {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Company Details", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it },
                    label = { Text("Registration Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = companyAddress,
                    onValueChange = { companyAddress = it },
                    label = { Text("Company Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    user?.let { currentUser ->
                        val updatedUser = currentUser.copy(
                            name = name,
                            phone = phone,
                            city = city,
                            gender = gender,
                            dateOfBirth = dob
                        )
                        authViewModel.updateUserProfile(updatedUser)
                        
                        if (currentUser.role == Constants.ROLE_COMPANY && company != null) {
                            val updatedCompany = company.copy(
                                name = companyName,
                                address = companyAddress,
                                ownerName = ownerName,
                                registrationNumber = registrationNumber,
                                city = city // Sync city?
                            )
                            companyViewModel.updateCompanyProfile(updatedCompany)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !authState.isLoading && !companyState.isLoading
            ) {
                 if (authState.isLoading || companyState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}
