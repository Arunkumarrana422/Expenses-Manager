package com.example.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Expense(
    val id: String = "",
    val roomId: String = "",
    val title: String = "",
    val amountPaise: Long = 0,
    val category: String = "Other",
    val paidBy: String = "",
    val date: String = "",
    val description: String = ""
)

data class Room(val id: String = "", val name: String = "", val code: String = "", val ownerId: String = "")

class ExpenseRepository {
    private val db = FirebaseFirestore.getInstance()
    fun expenses(roomId: String, onResult: (List<Expense>) -> Unit) {
        db.collection("expenses").whereEqualTo("roomId", roomId).addSnapshotListener { snap, _ ->
            onResult(snap?.documents?.map { d ->
                Expense(
                    id = d.id, roomId = d.getString("roomId") ?: "",
                    title = d.getString("title") ?: "",
                    amountPaise = d.getLong("amountPaise") ?: 0L,
                    category = d.getString("category") ?: "Other",
                    paidBy = d.getString("paidBy") ?: "",
                    date = d.getString("date") ?: "",
                    description = d.getString("description") ?: ""
                )
            } ?: emptyList())
        }
    }
    fun add(e: Expense, onDone: (Boolean) -> Unit) {
        db.collection("expenses").add(mapOf(
            "roomId" to e.roomId, "title" to e.title, "amountPaise" to e.amountPaise,
            "category" to e.category, "paidBy" to e.paidBy, "date" to e.date,
            "description" to e.description, "createdBy" to FirebaseAuth.getInstance().currentUser?.uid
        )).addOnCompleteListener { onDone(it.isSuccessful) }
    }
}

class MainViewModel : ViewModel() {
    private val repo = ExpenseRepository()
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses = _expenses.asStateFlow()
    var selectedRoom by mutableStateOf(Room("demo", "My Room", "RM8K2P", ""))
    var signedIn by mutableStateOf(false)
    var userName by mutableStateOf("User")

    init { signedIn = FirebaseAuth.getInstance().currentUser != null }
    fun observeExpenses() = repo.expenses(selectedRoom.id) { _expenses.value = it }
    fun addExpense(e: Expense, done: (Boolean) -> Unit) = repo.add(e, done)
    fun logout() { FirebaseAuth.getInstance().signOut(); signedIn = false }
}

private fun money(paise: Long) = "₹%.2f".format(paise / 100.0)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = MaterialTheme.colorScheme.primary),
        content = content
    )
}

@Composable
fun ExpenseManagerApp(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()
    if (!vm.signedIn) {
        AuthScreen { vm.signedIn = true }
        return
    }
    LaunchedEffect(vm.selectedRoom.id) { vm.observeExpenses() }

    NavHost(nav, startDestination = "home") {
        composable("home") { HomeScreen(vm, nav) }
        composable("expenses") { ExpensesScreen(vm, nav) }
        composable("reports") { ReportsScreen(vm, nav) }
        composable("members") { MembersScreen(nav) }
        composable("profile") { ProfileScreen(vm, nav) }
        composable("add") { AddExpenseScreen(vm, nav) }
    }
}

@Composable
fun Shell(title: String, nav: NavHostController, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) }) },
        bottomBar = {
            NavigationBar {
                listOf("home" to Icons.Default.Home, "expenses" to Icons.Default.List,
                    "reports" to Icons.Default.BarChart, "members" to Icons.Default.People,
                    "profile" to Icons.Default.Person).forEach { (route, icon) ->
                    NavigationBarItem(
                        selected = false, onClick = { nav.navigate(route) { launchSingleTop = true } },
                        icon = { Icon(icon, null) }, label = { Text(route.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    ) { p -> Column(Modifier.padding(p).padding(16.dp), content = content) }
}

@Composable
fun HomeScreen(vm: MainViewModel, nav: NavHostController) {
    val expenses by vm.expenses.collectAsState()
    val total = expenses.sumOf { it.amountPaise }
    Shell("Expense Manager", nav) {
        Text("Welcome, ${vm.userName}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(vm.selectedRoom.name, style = MaterialTheme.typography.titleMedium)
                Text("Room Code: ${vm.selectedRoom.code}")
                Spacer(Modifier.height(12.dp))
                Text(money(total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Total expenses")
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Expenses", expenses.size.toString(), Modifier.weight(1f))
            StatCard("Members", "1", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { nav.navigate("add") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Expense")
        }
        Spacer(Modifier.height(16.dp))
        Text("Recent expenses", fontWeight = FontWeight.Bold)
        expenses.takeLast(5).reversed().forEach { e ->
            ListItem(headlineContent = { Text(e.title) }, supportingContent = { Text(e.category) },
                trailingContent = { Text(money(e.amountPaise), fontWeight = FontWeight.Bold) })
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) { Column(Modifier.padding(16.dp)) { Text(value, style = MaterialTheme.typography.titleLarge); Text(label) } }
}

@Composable
fun ExpensesScreen(vm: MainViewModel, nav: NavHostController) {
    val expenses by vm.expenses.collectAsState()
    Shell("Expenses", nav) {
        Button(onClick = { nav.navigate("add") }, modifier = Modifier.fillMaxWidth()) { Text("+ Add Expense") }
        Spacer(Modifier.height(8.dp))
        LazyColumn { items(expenses.reversed()) { e ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                ListItem(headlineContent = { Text(e.title, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("${e.category} • ${e.date}\nPaid by: ${e.paidBy}") },
                    trailingContent = { Text(money(e.amountPaise)) })
            }
        }}
    }
}

@Composable
fun ReportsScreen(vm: MainViewModel, nav: NavHostController) {
    val expenses by vm.expenses.collectAsState()
    val total = expenses.sumOf { it.amountPaise }
    val categories = expenses.groupBy { it.category }.mapValues { it.value.sumOf(Expense::amountPaise) }
    Shell("Reports", nav) {
        Text("Monthly report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        StatCard("Total Expense", money(total), Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text("Category spending", fontWeight = FontWeight.Bold)
        categories.forEach { (cat, value) -> ListItem(headlineContent = { Text(cat) }, trailingContent = { Text(money(value)) }) }
    }
}

@Composable
fun MembersScreen(nav: NavHostController) {
    Shell("Members", nav) {
        ListItem(leadingContent = { Icon(Icons.Default.Person, null) },
            headlineContent = { Text("You") }, supportingContent = { Text("Room owner") })
        Spacer(Modifier.height(20.dp))
        Text("Member removal and settlement tracking can be managed from the room data once Firebase is configured.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ProfileScreen(vm: MainViewModel, nav: NavHostController) {
    Shell("Profile", nav) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(90.dp))
        Spacer(Modifier.height(12.dp))
        Text(vm.userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(FirebaseAuth.getInstance().currentUser?.email ?: "Firebase account")
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
    }
}

@Composable
fun AddExpenseScreen(vm: MainViewModel, nav: NavHostController) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var date by remember { mutableStateOf("2026-09-16") }
    var paidBy by remember { mutableStateOf("You") }
    var desc by remember { mutableStateOf("") }
    val cats = listOf("Food","Grocery","Rent","Electricity","Water","Internet","Transport","Shopping","Medical","Entertainment","Other")
    Scaffold(topBar = { TopAppBar(title = { Text("Add Expense") }) }) { p ->
        Column(Modifier.padding(p).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Expense title") })
            OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text("Amount (₹)") })
            Text("Category")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cats.take(3).forEach { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) }
            }
            OutlinedTextField(date, { date = it }, Modifier.fillMaxWidth(), label = { Text("Date") })
            OutlinedTextField(paidBy, { paidBy = it }, Modifier.fillMaxWidth(), label = { Text("Paid by") })
            OutlinedTextField(desc, { desc = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val rupees = amount.toLongOrNull() ?: 0L
                if (title.isNotBlank() && rupees > 0) {
                    vm.addExpense(Expense(roomId=vm.selectedRoom.id,title=title,amountPaise=rupees*100,
                        category=category,paidBy=paidBy,date=date,description=desc)) { if (it) nav.popBackStack() }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save Expense") }
        }
    }
}

@Composable
fun AuthScreen(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signup by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Expense Manager", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Shared expenses made simple")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(email, { email=it }, Modifier.fillMaxWidth(), label={Text("Email")})
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(password, { password=it }, Modifier.fillMaxWidth(), label={Text("Password")})
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            val task = if (signup) FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            else FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            task.addOnCompleteListener { if (it.isSuccessful) onSuccess() else error = it.exception?.localizedMessage ?: "Authentication failed" }
        }, Modifier.fillMaxWidth()) { Text(if (signup) "Create account" else "Login") }
        TextButton(onClick = { signup=!signup }) { Text(if (signup) "Already have an account? Login" else "Create a new account") }
        TextButton(onClick = {
            if (email.isNotBlank()) FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        }) { Text("Forgot password?") }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
    }
}
