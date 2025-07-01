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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygameteam2.fetch.FetchCard
import com.example.memorygameteam2.fetch.FetchCardAdapter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FetchActivity : AppCompatActivity() {
    private var bgThread: Thread? = null
    private var numberSelected: Int = 0
    private var selectedImages: MutableList<Int> = mutableListOf<Int>()
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

    // initialise buttons for Fetch Activity

    private fun initButtons() {
        val fetchButton = findViewById<Button>(R.id.fetch_button)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val progressText = findViewById<TextView>(R.id.progress_text)
        val playButton = findViewById<Button>(R.id.play_button)

        // set onClick listener when 'Fetch' is pressed
        fetchButton.setOnClickListener {
            var fetchLink = findViewById<EditText>(R.id.fetch_link).text.toString()
            bgThread =
                Thread {
                    val html = getHtmlContent(fetchLink)
                    val imageUrls = extractImageUrls(html)
                    fetchImages = mutableListOf<FetchCard>()

                    // fetch images from URL and present image on cards
                    for ((index, imageUrl) in imageUrls.withIndex()) {
                        if (fetchImages.size == 20) {
                            break
                        }
                        var file = makeFile("image_$index.jpg")
                        var success = downloadToFile(imageUrl, file)
                        if (success) {
                            getImage(file)
                        }

                        // update progress bar and progress text
                        runOnUiThread {
                            if (fetchImages.size == 20) {
                                progressBar.progress = 100
                                progressText.text = "Select 6 images plz!"
                            } else {
                                progressBar.progress = ((index + 1) * 100 / 20)
                                progressText.text = "${index + 1} / 20 images loaded..."
                            }
                        }
                    }

                    runOnUiThread {
                        // set up RecyclerView with 4 columns
                        var rv = findViewById<RecyclerView>(R.id.fetch_rv)
                        rv.layoutManager = GridLayoutManager(this, 4)
                        rv.adapter =
                            FetchCardAdapter(fetchImages) { pos ->
                                onFetchCardClicked(pos, rv.adapter as FetchCardAdapter)
                            }
                    }
                }
            bgThread?.start()
        }

        // when 'Play' is pressed: pass selected image info to Play Activity
        playButton.setOnClickListener {
            val imagePosArray = selectedImages.toCollection(ArrayList()) // must be converted to pass in Intent
            val intent = Intent(this, PlayActivity::class.java)
            intent.putIntegerArrayListExtra("imageList", imagePosArray)
            startActivity(intent)
        }
    }

    // function to handle clicked Card View

    private fun onFetchCardClicked(
        pos: Int,
        adapter: FetchCardAdapter,
    ) {
        val selectedImage = fetchImages[pos]
        val playButton = findViewById<Button>(R.id.play_button)

        if (!selectedImage.isSelected) {
            if (selectedImages.size < 6) {
                selectedImage.isSelected = true
                numberSelected++
                selectedImages.add(pos)
                adapter.notifyItemChanged(pos)

                if (selectedImages.size == 6) {
                    playButton.isEnabled = true
                    playButton.text = "Play!"
                } else {
                    playButton.visibility = View.VISIBLE
                    playButton.text = "${selectedImages.size} / 6 images selected"
                }
                Log.d("clickChange", selectedImages.toString()) // for checking, TO REMOVE!
            }
        } else {
            playButton.isEnabled = false
            selectedImage.isSelected = false
            numberSelected--
            selectedImages.remove(pos)
            adapter.notifyItemChanged(pos)
            playButton.text = "${selectedImages.size} / 6 images selected"
            Log.d("clickChange", selectedImages.toString()) // for checking, TO REMOVE!
        }
    }

    // 1. to get images from web page, get the webpage HTML source

    private fun getHtmlContent(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Mozilla")

        return connection.inputStream.use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                reader.readText()
            }
        }
    }

    // 2. then parse the HTML using Jsoup to find image URLs only

    private fun extractImageUrls(html: String): List<String> {
        val doc: Document = Jsoup.parse(html)
        val imgElements: Elements = doc.select("img[src]")
        return imgElements
            // .take(20)               // limit to 20 items. // TO AMEND!
            .map { it.attr("src") } // maps values of "src" attribute (link) as List of String.
            .filter { src ->
                src.startsWith("http") && (src.contains(".jpg") || src.contains(".jpeg"))
            }
    }

    // 3. finally make file to save to and download those images

    private fun makeFile(filename: String): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, filename)
    }

    private fun downloadToFile(
        imageUrl: String,
        file: File,
    ): Boolean {
        return try {
            URL(imageUrl).openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            Log.e("DownloadError", "Error downloading image")
            file.delete()
            false
        }
    }

    // update individual card view with downloaded images

    private fun getImage(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        fetchImages.add(FetchCard(bitmap))
    }

    // close bgThread on closing Activity

    override fun onDestroy() {
        super.onDestroy()
        bgThread?.interrupt()
        bgThread = null
    }
}
