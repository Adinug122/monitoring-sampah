package dev.bro.monitoring_sampah.fragmenttt

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.database.*
import dev.bro.monitoring_sampah.R

class Dashboard : Fragment() {

    private lateinit var txtLevel: TextView
    private lateinit var txtBau: TextView
    private lateinit var txtSuhu: TextView
    private lateinit var txtKelembapan: TextView

    private lateinit var progressLevel: ProgressBar

    private lateinit var bar1: View
    private lateinit var bar2: View
    private lateinit var bar3: View

    private lateinit var dbRef: DatabaseReference

    private val minDistance = 3f
    private val maxDistance = 35f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // INI YANG BENER → inflate layout fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // INIT VIEW DI SINI, BUKAN DI onCreate()
        txtLevel = view.findViewById(R.id.txtLevel)
        txtBau = view.findViewById(R.id.bau)
        txtSuhu = view.findViewById(R.id.txtSuhu)
        txtKelembapan = view.findViewById(R.id.txtKelembapan)

        progressLevel = view.findViewById(R.id.progressLevel)
        progressLevel.max = 100

        bar1 = view.findViewById(R.id.barBau1)
        bar2 = view.findViewById(R.id.barBau2)
        bar3 = view.findViewById(R.id.barBau3)

        // Firebase ref
        dbRef = FirebaseDatabase.getInstance().getReference("sensor/sim1")

        listenFirebase()

        return view
    }

    private fun listenFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val levelFromFirebase = readIntFlexible(snapshot, "levelSampah")
                val bau = readIntFlexible(snapshot, "kadarBau")
                val suhu = readIntFlexible(snapshot, "suhu")
                val kelembapan = readIntFlexible(snapshot, "kelembapan")

                var levelPercent = levelFromFirebase?.coerceIn(0, 100) ?: 0

                txtLevel.text = "$levelPercent%"
                progressLevel.progress = levelPercent

                txtBau.text = "Kadar Bau: ${bau ?: 0} ppm"
                updateBauBar(bau ?: 0)

                txtSuhu.text = "${suhu ?: 0}°C"
                txtKelembapan.text = "${kelembapan ?: 0}%"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Firebase ERROR: ${error.message}")
            }
        })
    }

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

    private fun updateBauBar(bau: Int) {
        bar1.setBackgroundColor(Color.parseColor("#CFCFCF"))
        bar2.setBackgroundColor(Color.parseColor("#CFCFCF"))
        bar3.setBackgroundColor(Color.parseColor("#CFCFCF"))

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
