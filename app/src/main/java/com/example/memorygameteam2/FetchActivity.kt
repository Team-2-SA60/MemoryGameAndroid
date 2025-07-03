package com.example.memorygameteam2

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.PlayActivity
import com.example.memorygameteam2.R
import com.example.memorygameteam2.fetch.FetchCard
import com.example.memorygameteam2.fetch.FetchCardAdapter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class FetchActivity : AppCompatActivity() {

    private var fetchJob: Job? = null
    private var numberSelected: Int = 0
    private var selectedImages: MutableList<Int> = mutableListOf()
    private lateinit var fetchImages: MutableList<FetchCard>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fetch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initButtons()
    }

    private fun initButtons() {
        val fetchButton = findViewById<Button>(R.id.fetch_button)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.progress_text)
        val playButton = findViewById<Button>(R.id.play_button)

        fetchButton.setOnClickListener {
            val fetchLink = findViewById<EditText>(R.id.fetch_link).text.toString()

            // Cancel previous job if it's running
            fetchJob?.cancel()

            fetchJob = lifecycleScope.launch {
                try {
                    fetchImages(fetchLink, progressBar, progressText)
                } catch (e: CancellationException) {
                    // Job was cancelled - normal flow
                    Log.d("FetchActivity", "Fetching cancelled")
                } catch (e: Exception) {
                    handleFetchError(e)
                }
            }
        }

        playButton.setOnClickListener {
            val imagePosArray = selectedImages.toCollection(ArrayList())
            val intent = Intent(this, PlayActivity::class.java)
            intent.putIntegerArrayListExtra("imageList", imagePosArray)
            startActivity(intent)
        }
    }

    private suspend fun fetchImages(
        fetchLink: String,
        progressBar: ProgressBar,
        progressText: TextView
    ) {
        withContext(Dispatchers.IO) {
            val html = getHtmlContent(fetchLink)
            val imageUrls = extractImageUrls(html.toString())

            fetchImages = mutableListOf()

            for ((index, imageUrl) in imageUrls.withIndex()) {
                if (fetchImages.size >= 20) break

                val file = makeFile("image_$index.jpg")
                val success = downloadToFile(imageUrl, file)

                if (success) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        fetchImages.add(FetchCard(bitmap))
                    } else {
                        file.delete()
                    }
                }

                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    if (fetchImages.size >= 20) {
                        progressBar.progress = 100
                        progressText.text = "Select 6 images plz!"
                    } else {
                        progressBar.progress = ((index + 1) * 100 / 20)
                        progressText.text = "${index + 1} / 20 images loaded..."
                    }
                }
            }

            withContext(Dispatchers.Main) {
                val rv = findViewById<RecyclerView>(R.id.fetch_rv)
                rv.layoutManager = GridLayoutManager(this@FetchActivity, 4)
                rv.adapter = FetchCardAdapter(fetchImages) { pos ->
                    onFetchCardClicked(pos, rv.adapter as FetchCardAdapter)
                }
            }
        }
    }

    private fun handleFetchError(e: Exception) {
        when (e) {
            is FileNotFoundException -> {
                clearAll()
                showToast("Please enter a valid URL")
            }
            is IOException -> {
                clearAll()
                showToast("Network error, please try again")
            }
            else -> {
                clearAll()
                showToast("Error: ${e.message}")
                Log.e("FetchActivity", "Fetch error", e)
            }
        }
    }

    private fun showToast(message: String) {
        lifecycleScope.launchWhenResumed {
            Toast.makeText(this@FetchActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onFetchCardClicked(pos: Int, adapter: FetchCardAdapter) {
        val selectedImage = fetchImages[pos]
        val playButton = findViewById<Button>(R.id.play_button)

        if (!selectedImage.isSelected) {
            if (selectedImages.size < 6) {
                selectedImage.isSelected = true
                selectedImages.add(pos)
                adapter.notifyItemChanged(pos)

                playButton.visibility = View.VISIBLE
                playButton.text = if (selectedImages.size == 6) {
                    playButton.isEnabled = true
                    "Play!"
                } else {
                    playButton.isEnabled = false
                    "${selectedImages.size} / 6 images selected"
                }
            }
        } else {
            selectedImage.isSelected = false
            selectedImages.remove(pos)
            adapter.notifyItemChanged(pos)
            playButton.text = "${selectedImages.size} / 6 images selected"
            playButton.isEnabled = false
        }
    }

    // Network and file operations remain the same but add suspend modifier
    @Throws(IOException::class)
    private suspend fun getHtmlContent(url: String): String {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla")
            connection.inputStream.use { input ->
                input.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        }
    }

    private fun extractImageUrls(html: String): List<String> {
        val doc: Document = Jsoup.parse(html)
        val imgElements: Elements = doc.select("img[src]")
        return imgElements
            .map { it.attr("src") }
            .filter { src ->
                src.startsWith("http") && (src.contains(".jpg") || src.contains(".jpeg"))
            }
    }

    private fun makeFile(filename: String): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, filename)
    }

    @Throws(IOException::class)
    private suspend fun downloadToFile(imageUrl: String, file: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                URL(imageUrl).openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } catch (e: IOException) {
                file.delete()
                false
            }
        }
    }

    private fun clearAll() {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        dir?.listFiles()?.forEach { it.delete() }

        fetchImages.clear()
        val rv = findViewById<RecyclerView>(R.id.fetch_rv)
        rv.adapter?.notifyDataSetChanged()

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.progress_text)
        progressBar.progress = 0
        progressText.text = "0 / 20 images loaded..."

        val playButton = findViewById<Button>(R.id.play_button)
        playButton.visibility = View.INVISIBLE
        playButton.text = ""
        playButton.isEnabled = false

        selectedImages.clear()
    }

    override fun onDestroy() {
        fetchJob?.cancel()
        super.onDestroy()
    }
}