package com.example.memorygameteam2

import android.R.attr.bitmap
import android.R.attr.src
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

    // set onClick listener when 'Fetch' is pressed

    private fun initButtons() {
        var fetchButton = findViewById<Button>(R.id.fetch_button)
        fetchButton.setOnClickListener {
            var fetchLink = findViewById<EditText>(R.id.fetch_link).text.toString()
            bgThread =
                Thread {
                    val html = getHtmlContent(fetchLink)
                    val imageUrls = extractImageUrls(html)
                    fetchImages = mutableListOf<FetchCard>()

                    imageUrls.forEachIndexed { index, imageUrl ->
                        var file = makeFile("image_$index.jpg")
                        var success = downloadToFile(imageUrl, file) // check file 9
                        if (success) {
                            Log.d("DownloadSuccess", "Successfully downloaded image index $index")
                            getImage(file)
                        }
                    }

                    runOnUiThread {
                        // set up RecyclerView with 4 columns
                        var rv = findViewById<RecyclerView>(R.id.fetch_rv)
                        rv.layoutManager = GridLayoutManager(this, 4)
                        rv.adapter = FetchCardAdapter(fetchImages)
                    }
                }
            bgThread?.start()
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
            .take(20) // limit to 20 items. // TO AMEND!
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
            Log.e("DownloadError", "Error downloading image", e)
            file.delete()
            false
        }
    }

    // update individual card view with downloaded images

    private fun getImage(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        fetchImages.add(FetchCard(bitmap))
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        bgThread?.interrupt()
//        bgThread = null
//    }

    //
    // old code
    // fetch images

//    private fun fetchImages(): MutableList<FetchCard> {
//        val images = listOf (
//            R.drawable.bird_1,
//            R.drawable.bird_2,
//            R.drawable.bird_3,
//            R.drawable.bird_4,
//            R.drawable.bird_5,
//            R.drawable.bird_6,
//            R.drawable.bird_7,
//            R.drawable.bird_8,
//            R.drawable.bird_9,
//            R.drawable.bird_10,
//            R.drawable.bird_11,
//            R.drawable.bird_12,
//            R.drawable.bird_13,
//            R.drawable.bird_14,
//            R.drawable.bird_15,
//            R.drawable.bird_16,
//            R.drawable.bird_17,
//            R.drawable.bird_18,
//            R.drawable.bird_19,
//            R.drawable.bird_20
//        )
//        var fetchCards = mutableListOf<FetchCard>()
//        for (img in images) {
//            fetchCards.add(FetchCard(img))
//        }
//        return fetchCards
//    }
}
