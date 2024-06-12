package com.example.fetchexercise

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fetchexercise.ui.theme.FetchExerciseTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import retrofit2.Retrofit
import androidx.compose.ui.unit.dp
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


class MainActivity : AppCompatActivity() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fetch-hiring.s3.amazonaws.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchData()
    }

    private fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getData()
                withContext(Dispatchers.Main) {
                    updateUI(response)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    private fun updateUI(items: List<Item>) {
        setContent {
            FetchExerciseTheme {
                Scaffold(
                    content = {
                        DisplayItems(items)
                    }
                )
            }
        }
    }

    @Composable
    fun DisplayItems(items: List<Item>) {
        // Group items by listId
        val groupedItems = items.groupBy { it.listId }

        // Sort grouped items by listId and then by name
        val sortedItems = groupedItems.entries.sortedBy { it.key }.flatMap { entry ->
            entry.value.filter {
                it.name?.isNotEmpty() ?: false
            } // Filter out items with blank or null name
                .sortedBy { it.name }
        }

//        // Display grouped and sorted items
//        sortedItems.forEach { item ->
//            Text(
//                text = "List ID: ${item.listId}, Item ID: ${item.id}, Name: ${item.name ?: "No Name"}",
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }

        // Display grouped and sorted items in a LazyColumn
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(sortedItems) { item ->
                Text(
                    text = "List ID: ${item.listId}, Item ID: ${item.id}, Name: ${item.name ?: "No Name"}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }

    }
}

data class Item(
    val id: Int,
    val listId: Int,
    val name: String?
)

interface ApiService {
    @GET("hiring.json")
    suspend fun getData(): List<Item>
}
