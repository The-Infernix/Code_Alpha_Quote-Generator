package com.example.quotegenerator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var quoteText: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var nextQuoteButton: Button
    private lateinit var shareQuoteButton: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        quoteText = findViewById(R.id.quoteText)
        quoteAuthor = findViewById(R.id.quoteAuthor)
        nextQuoteButton = findViewById(R.id.nextQuoteButton)
        shareQuoteButton = findViewById(R.id.shareQuoteButton)

        fetchQuote() // Load first quote on start

        nextQuoteButton.setOnClickListener {
            fetchQuote()
        }

        shareQuoteButton.setOnClickListener {
            shareCurrentQuote()
        }
    }

    private fun fetchQuote() {
        val request = Request.Builder()
            .url("https://zenquotes.io/api/random")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to fetch quote", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { body ->
                    try {
                        val jsonArray = JSONArray(body)
                        val jsonObject = jsonArray.getJSONObject(0)
                        val quote = jsonObject.getString("q")
                        val author = jsonObject.getString("a")

                        runOnUiThread {
                            quoteText.text = quote
                            quoteAuthor.text = "- $author"
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Error parsing quote", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun shareCurrentQuote() {
        val quote = quoteText.text.toString()
        val author = quoteAuthor.text.toString()

        if (quote.isNotBlank()) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "\"$quote\" $author")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share quote via"))
        } else {
            Toast.makeText(this, "No quote to share", Toast.LENGTH_SHORT).show()
        }
    }
}
