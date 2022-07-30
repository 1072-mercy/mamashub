package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.adapter.ObservationAdapter
import com.intellisoft.kabarakmhis.new_designs.adapter.ViewDetailsAdapter
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_clinical_notes_list.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.*
import kotlinx.android.synthetic.main.activity_medical_surgical_history_view.no_record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicalSurgicalHistoryView : AppCompatActivity() {

    private val retrofitCallsFhir = RetrofitCallsFhir()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var kabarakViewModel: KabarakViewModel
    private val formatter = FormatterClass()

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    private val formatterClass = FormatterClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_surgical_history_view)
        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

        title = "Medical & Surgical History Details"

        btnAddHistory.setOnClickListener {
            val intent = Intent(this, MedicalHistory::class.java)
            startActivity(intent)
        }

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)


    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {

            val encounterId = formatter.retrieveSharedPreference(
                this@MedicalSurgicalHistoryView,
                DbResourceViews.MEDICAL_HISTORY.name
            )
            if (encounterId != null) {


                val observationList =
                    patientDetailsViewModel.getObservationsFromEncounter(encounterId)
                CoroutineScope(Dispatchers.Main).launch {

                    if (!observationList.isNullOrEmpty()) {
                        no_record.visibility = View.GONE
                    } else {
                        no_record.visibility = View.VISIBLE
                    }


                }

                if (observationList.isNotEmpty()) {

                    Log.e("----4", observationList.toString())

                    var sourceString = ""

                    for (item in observationList) {

                        val code = item.code
                        val display = item.value

                        Log.e("----code ", code.toString())
                        Log.e("----display ", display.toString())



//                    sourceString = "$sourceString\n\n${code.toUpperCase()}: $display"
                        sourceString = "$sourceString<br><b>${code.toUpperCase()}</b>: $display"
                    }


                    CoroutineScope(Dispatchers.Main).launch {


                        tvValue.text = Html.fromHtml(sourceString)
                        btnAddHistory.text = "Edit Medical History"
                    }

                }

//            getObservationDetails()

            }

        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, PatientProfile::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}