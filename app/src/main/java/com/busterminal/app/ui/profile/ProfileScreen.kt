package com.busterminal.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.busterminal.app.data.model.User
import com.busterminal.app.ui.auth.AuthViewModel
import com.busterminal.app.ui.company.CompanyViewModel
import com.busterminal.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    companyViewModel: CompanyViewModel,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val companyState by companyViewModel.uiState.collectAsState()
    
    val user = authState.currentUser
    
    LaunchedEffect(user) {
        if (user != null && user.role == Constants.ROLE_COMPANY) {
            companyViewModel.loadCompanyData(user.id)
        }
    }

    if (authState.isLoading || (user?.role == Constants.ROLE_COMPANY && companyState.isLoading)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
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
            // Profile Header
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (user?.role == Constants.ROLE_COMPANY) Icons.Default.Business else Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = user?.name ?: "Guest",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Account Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    ProfileItem(Icons.Default.Phone, "Phone", user?.phone ?: "Not set")
                    ProfileItem(Icons.Default.LocationCity, "City", user?.city ?: "Not set")
                    
                    if (user?.role == Constants.ROLE_PASSENGER) {
                        ProfileItem(Icons.Default.Person, "Gender", user?.gender ?: "Not set")
                        ProfileItem(Icons.Default.Cake, "Date of Birth", user?.dateOfBirth ?: "Not set")
                    }
                    
                    if (user?.role == Constants.ROLE_COMPANY) {
                        val company = companyState.company
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            "Company Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        ProfileItem(Icons.Default.Business, "Company Name", company?.name ?: "N/A")
                        ProfileItem(Icons.Default.Badge, "Registration No", company?.registrationNumber ?: "N/A")
                        ProfileItem(Icons.Default.Person, "Owner Name", company?.ownerName ?: "N/A")
                        ProfileItem(Icons.Default.LocationOn, "Address", company?.address ?: "N/A")
                        ProfileItem(
                            Icons.Default.Verified, 
                            "Status", 
                            company?.approvalStatus?.uppercase() ?: "PENDING",
                            statusColor = if (company?.approvalStatus == Constants.STATUS_APPROVED) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String,
    statusColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
