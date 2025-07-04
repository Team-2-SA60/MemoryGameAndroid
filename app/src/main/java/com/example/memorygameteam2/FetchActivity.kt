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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.fetch.FetchCard
import com.example.memorygameteam2.fetch.FetchCardAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlin.collections.MutableList
import kotlin.coroutines.cancellation.CancellationException

class FetchActivity : AppCompatActivity() {

    private var fetchJob: Job? = null
    private var fetchImages: MutableList<FetchCard> = MutableList(20) { FetchCard() }
    private var selectedImages: MutableList<Int> = mutableListOf()
    private var done: Boolean = false

    // all necessary items

    private lateinit var dir: File
    private lateinit var rv: RecyclerView
    private lateinit var fetchButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var playButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fetch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        rv = findViewById<RecyclerView>(R.id.fetch_rv)
        fetchButton = findViewById<Button>(R.id.fetch_button)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressText = findViewById<TextView>(R.id.progress_text)
        playButton = findViewById<Button>(R.id.play_button)

        initRv()
        initButtons()
    }

    // initialise the RecyclerView with placeholder cards

    private fun initRv() {
        rv.layoutManager = GridLayoutManager(this@FetchActivity, 4)
        rv.adapter = FetchCardAdapter(fetchImages) { pos ->
            onFetchCardClicked(pos, rv.adapter as FetchCardAdapter)
        }
    }

    // set onClickListener events

    private fun initButtons() {

        // when 'Fetch' is clicked, fetch images from URL and display in view
        fetchButton.setOnClickListener {
            val fetchLink = findViewById<EditText>(R.id.fetch_link).text.toString()

            // cancel previous coroutine if it is already running
            fetchJob?.cancel()
            clearAll()

            // launch fetchJob coroutine attached to FetchActivity lifecycle
            // we call our suspend functions within the coroutine (suspend -> pause while operations running)
            fetchJob = lifecycleScope.launch {
                try {
                    fetchImages(fetchLink)
                } catch (e: Exception) {
                    handleFetchError(e)
                }
            }
        }

        // when 'Play' is clicked, pass selected images and start PlayActivity
        playButton.setOnClickListener {
            val imagePosArray = selectedImages.toCollection(ArrayList())
            Intent(this@FetchActivity, PlayActivity::class.java).also {
                it.putIntegerArrayListExtra("imageList", imagePosArray)
                startActivity(it)
                finish()
            }
        }
    }

    /*  Function to fetch images and display to RecyclerView
        1. getHtmlContent: Get all HTML content from specified URL
        2. extractImageUrls: Get only image src URL within the HTML content
        3. downloadToFile: Downloads images into our Android application
    */

    private suspend fun fetchImages(fetchLink: String) {

        // run fetch operation (on IO thread)
        withContext(Dispatchers.IO) {
            val html = getHtmlContent(fetchLink)
            val imageUrls = extractImageUrls(html.toString())

            // for each image URL in the HTML page, download and display it on our card
            for ((index, imageUrl) in imageUrls.withIndex()) {
                if (index >= 20) break

                val file = File(dir, "image_$index.jpg")
                val success = downloadToFile(imageUrl, file)

                if (success) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        // add image to a card if successful decoding
                        fetchImages[index] = FetchCard(bitmap)

                        // update the RecyclerView cards (on UI thread)
                        withContext(Dispatchers.Main) {
                            rv.adapter?.notifyItemChanged(index)
                            progressBar.progress = ((index + 1) * 100 / 20)
                            progressText.text = "${index + 1} / 20 images loaded..."
                        }
                    } else {
                        // delete file if there is error in decoding
                        file.delete()
                    }
                }

                delay(100) // add small delay between downloads to make it visible
            }

            // update progress once all items are loaded (on UI thread)
            withContext(Dispatchers.Main) {
                progressBar.progress = 100
                progressText.text = "Select 6 images plz!"
            }

            done = true
        }
    }

    private fun handleFetchError(e: Exception) {
        when (e) {
            is FileNotFoundException -> {
                clearAll()
                showToast("Please ensure URL has valid images")
            }
            is IOException -> {
                clearAll()
                showToast("Please enter a valid URL")
            }
            is CancellationException -> {
                clearAll()
                showToast("Re-fetching images...")
            }
            else -> {
                clearAll()
                showToast("Error: ${e.message}")
                Log.e("FetchActivity", "Fetch error", e)
            }
        }
    }

    private fun showToast(message: String) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Toast.makeText(this@FetchActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 1. Get all HTML content from specified URL

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

    // 2. Get only image src URL within the HTML content

    private fun extractImageUrls(html: String): List<String> {
        val doc: Document = Jsoup.parse(html)
        val imgElements: Elements = doc.select("img[src]")
        return imgElements
            .map { it.attr("src") }
            .filter { src ->
                src.startsWith("http") && (src.contains(".jpg") || src.contains(".jpeg"))
            }
    }

    // 3. Downloads images into our Android application

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
                file.delete() // delete file if there is error in downloading
                false
            }
        }
    }

    /*  Function to handle clicked cards
        1. Updates card selected status
        2. Updates number of cards selected and UI effect
        3. Once 6 images selected, 'Play' button appears
    */

    private fun onFetchCardClicked(pos: Int, adapter: FetchCardAdapter) {
        if (!done) return // if not yet finished fetching, disallow click

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

    // clean slate when re-fetching images from URL

    private fun clearAll() {
        dir.listFiles()?.forEach { it.delete() } // delete all existing files
        fetchImages = MutableList(20) { FetchCard() } // renew all card from list
        selectedImages = mutableListOf() // remove all selected images information
        rv.adapter?.notifyDataSetChanged() // update RecyclerView on removal
        progressBar.progress = 0 // set progress back to 0
        progressText.text = "Enter a URL to fetch images" // set progress text back to 0
        playButton.visibility = View.INVISIBLE // remove visibility of play button
        playButton.text = "" // remove text for play button
        playButton.isEnabled = false // disable play button
        initRv() // re-initialise RecyclerView
        done = false // reset 'done' status
    }

    override fun onDestroy() {
        fetchJob?.cancel()
        super.onDestroy()
    }
}