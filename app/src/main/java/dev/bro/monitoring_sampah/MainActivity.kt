package dev.bro.monitoring_sampah

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var txtLevel: TextView
    private lateinit var txtBau: TextView
    private lateinit var txtSuhu: TextView
    private lateinit var txtKelembapan: TextView

    private lateinit var progressLevel: ProgressBar

    private lateinit var bar1: View
    private lateinit var bar2: View
    private lateinit var bar3: View

    private lateinit var dbRef: DatabaseReference

    // Jika pengukuran jarak: sesuaikan unit (cm atau meter)
    private val minDistance = 3f    // contoh: jarak minimum (full) -> 3 (unit yang sama dengan data)
    private val maxDistance = 35f   // contoh: jarak maximum (empty) -> 35

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Texts
        txtLevel = findViewById(R.id.txtLevel)
        txtBau = findViewById(R.id.bau)
        txtSuhu = findViewById(R.id.txtSuhu)
        txtKelembapan = findViewById(R.id.txtKelembapan)

        // Progress bar
        progressLevel = findViewById(R.id.progressLevel)
        progressLevel.max = 100

        // Bau bars
        bar1 = findViewById(R.id.barBau1)
        bar2 = findViewById(R.id.barBau2)
        bar3 = findViewById(R.id.barBau3)

        // Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("sensor/sim1")
        Log.d("FIREBASE_PATH", "Using path: sensor/sim1")

        listenFirebase()
    }

    private fun listenFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // debugging: lihat payload lengkap
                Log.d("FIREBASE_DATA", snapshot.value?.toString() ?: "NULL snapshot")

                // 1) Prioritas: jika ada key "levelSampah" pakai langsung (anggap sudah %)
                val levelFromFirebase = readIntFlexible(snapshot, "levelSampah")
                val bau = readIntFlexible(snapshot, "kadarBau")
                val suhu = readIntFlexible(snapshot, "suhu")
                val kelembapan = readIntFlexible(snapshot, "kelembapan")

                var levelPercent = -1

                if (levelFromFirebase != null) {
                    // Jika DB sudah menyimpan persen langsung, pakai itu
                    levelPercent = levelFromFirebase.coerceIn(0, 100)
                    Log.d("LEVEL_LOGIC", "Using levelSampah from Firebase: $levelPercent%")
                } else {
                    // Kalau tidak ada, coba hitung dari 'tinggiTong' atau 'distance' (jarak)
                    val tinggiTongDouble = readDoubleFlexible(snapshot, "tinggiTong")
                    val distanceDouble = readDoubleFlexible(snapshot, "distance")

                    val distanceValue = when {
                        tinggiTongDouble != null -> tinggiTongDouble.toFloat()
                        distanceDouble != null -> distanceDouble.toFloat()
                        else -> null
                    }

                    if (distanceValue != null) {
                        levelPercent = calculateTrashLevel(distanceValue)
                        Log.d("LEVEL_LOGIC", "Calculated level from distance: $levelPercent% (distance=$distanceValue)")
                    } else {
                        // fallback: jika tak ada semua, set 0
                        levelPercent = 0
                        Log.w("LEVEL_LOGIC", "No level/distance found in snapshot -> fallback 0%")
                    }
                }

                // Update UI
                txtLevel.text = "$levelPercent%"
                progressLevel.progress = levelPercent

                // Kadar bau: update text & 3-bar visual
                txtBau.text = "Kadar Bau: ${bau ?: 0} ppm"
                updateBauBar(bau ?: 0)

                // Suhu & kelembapan
                txtSuhu.text = "${suhu ?: 0}°C"
                txtKelembapan.text = "${kelembapan ?: 0}%"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Firebase ERROR: ${error.message}")
            }
        })
    }

    // Utility: baca Int dari snapshot dengan fleksibilitas tipe (Long, Int, String)
    private fun readIntFlexible(snapshot: DataSnapshot, key: String): Int? {
        val node = snapshot.child(key)
        if (!node.exists()) return null
        val v = node.value ?: return null
        return when (v) {
            is Long -> v.toInt()
            is Int -> v
            is Double -> v.toInt()
            is String -> v.toIntOrNull()
            else -> null
        }
    }

    // Utility: baca Double/Float fleksibel
    private fun readDoubleFlexible(snapshot: DataSnapshot, key: String): Double? {
        val node = snapshot.child(key)
        if (!node.exists()) return null
        val v = node.value ?: return null
        return when (v) {
            is Double -> v
            is Long -> v.toDouble()
            is Int -> v.toDouble()
            is String -> v.toDoubleOrNull()
            else -> null
        }
    }

    // Jika input = jarak (distance), convert ke persen 0..100
    private fun calculateTrashLevel(distance: Float): Int {
        // asumsi: minDistance = jarak ketika tempat penuh; maxDistance = jarak ketika kosong
        var level = ((distance - minDistance) / (maxDistance - minDistance)) * 100f
        level = 100f - level
        return level.coerceIn(0f, 100f).toInt()
    }

    private fun updateBauBar(bau: Int) {
        // Reset dulu
        bar1.setBackgroundColor(Color.parseColor("#CFCFCF"))
        bar2.setBackgroundColor(Color.parseColor("#CFCFCF"))
        bar3.setBackgroundColor(Color.parseColor("#CFCFCF"))

        // Threshold contoh: sesuaikan dengan sensor mu
        when {
            bau < 33 -> bar1.setBackgroundColor(Color.parseColor("#8EE58C"))
            bau < 66 -> {
                bar1.setBackgroundColor(Color.parseColor("#FFC300"))
                bar2.setBackgroundColor(Color.parseColor("#FFC300"))
            }
            else -> {
                bar1.setBackgroundColor(Color.parseColor("#FF5733"))
                bar2.setBackgroundColor(Color.parseColor("#FF5733"))
                bar3.setBackgroundColor(Color.parseColor("#FF5733"))
            }
        }
    }
}
