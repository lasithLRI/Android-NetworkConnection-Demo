package com.example.network_connection_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.text.StringBuilder


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GUI()
        }
    }
}


@Composable
fun GUI(){
    var bookInfo by remember { mutableStateOf("") }

    var keyword by remember{ mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row {

            Button(onClick = {
                scope.launch {
                    bookInfo = fetchBooks(keyword)
                }
            }) {

                Text("Fetch Books")

            }

            TextField(value = keyword, onValueChange = { keyword = it })

        }
        Text(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            text = bookInfo
        )

    }
}


suspend fun fetchBooks(keyword:String):String{
    val url_string = "https://www.googleapis.com/books/v1/volumes?q=$keyword&maxResults=25"

    val url = URL(url_string)

    val con: HttpURLConnection = url.openConnection() as HttpURLConnection

    var stb = StringBuilder()

    withContext(Dispatchers.IO){
        var bf = BufferedReader(InputStreamReader(con.inputStream))
        var line: String? = bf.readLine()

        while (line != null){
            stb.append(line + "\n")
            line = bf.readLine()
        }

    }

    val allBooks = parseJSON(stb)

    return allBooks
}

fun parseJSON(stb:StringBuilder):String{

    val json = JSONObject(stb.toString())

    var allBooks = StringBuilder()

    var jsonArray : JSONArray = json.getJSONArray("items")

    for (i in 0..jsonArray.length()-1){
        val book : JSONObject = jsonArray[i] as JSONObject

        val volInfo = book["volumeInfo"] as JSONObject
        val title = volInfo["title"] as String
        allBooks.append("${i + 1}) \"$title\" ")

        try {
            val authors = volInfo["authors"] as JSONArray
            allBooks.append("authors: ")
            for (i in 0..authors.length() - 1)
                allBooks.append(authors[i] as String + ", ")
        }catch (jen:JSONException){

        }

        allBooks.append("\n\n")
    }

    return allBooks.toString()
}

