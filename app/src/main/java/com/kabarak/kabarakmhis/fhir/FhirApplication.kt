package com.kabarak.kabarakmhis.fhir

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.android.fhir.*
import com.google.android.fhir.sync.Sync
import com.kabarak.kabarakmhis.fhir.data.Constants.ACCESS_TOKEN
import com.kabarak.kabarakmhis.fhir.data.Constants.DEMO_API_SERVER
import com.kabarak.kabarakmhis.fhir.data.Constants.DEMO_SERVER
import com.kabarak.kabarakmhis.fhir.data.Constants.LOGIN
import com.kabarak.kabarakmhis.fhir.data.Constants.SERVER_SET
import com.kabarak.kabarakmhis.fhir.data.Constants.SERVER_URL
import com.kabarak.kabarakmhis.fhir.data.Constants.SERVER_URL_DEMO
import com.kabarak.kabarakmhis.fhir.data.Constants.USER_ACCOUNT
import com.kabarak.kabarakmhis.fhir.data.Constants.WELCOME
import com.kabarak.kabarakmhis.fhir.data.FhirPeriodicSyncWorker
import com.kabarak.kabarakmhis.helperclass.AuthResponse


class FhirApplication : Application() {
    // Only initiate the FhirEngine when used for the first time, not when the app is created.
    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }

    private lateinit var instance: Context
    private val sharedPrefFile = "Neonatal"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
        }
        instance = this.applicationContext

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                getHapiServerURL(this)?.let { ServerConfiguration(it) }
            )
        )
        Sync.oneTimeSync<FhirPeriodicSyncWorker>(this)

    }


    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }

    companion object {

        fun fhirEngine(context: Context) =
            (context.applicationContext as FhirApplication).fhirEngine

        fun getSharedPreferences(context: Context): SharedPreferences {
            return (context.applicationContext as FhirApplication).sharedPreferences
        }

        fun getServerURL(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                SERVER_URL, DEMO_API_SERVER
            )
        }

        fun getHapiServerURL(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                SERVER_URL_DEMO, DEMO_SERVER
            )
        }

        fun isWelcomed(context: Context): Boolean {
            return (context.applicationContext as FhirApplication).sharedPreferences.getBoolean(
                WELCOME,
                false
            )
        }

        fun isLoggedIn(context: Context): Boolean {
            return (context.applicationContext as FhirApplication).sharedPreferences.getBoolean(
                LOGIN,
                false
            )
        }

        fun isServerSet(context: Context): Boolean {
            return (context.applicationContext as FhirApplication).sharedPreferences.getBoolean(
                SERVER_SET,
                false
            )
        }

        fun setServerDetails(context: Context, b: Boolean, url: String, demo: String) {
            (context.applicationContext as FhirApplication).editor.putBoolean(SERVER_SET, b)
                .putString(SERVER_URL, url).putString(SERVER_URL_DEMO, demo).commit()
        }

        fun setWelcomed(context: Context, b: Boolean) {
            (context.applicationContext as FhirApplication).editor.putBoolean(WELCOME, b).commit()
        }

        fun setLoggedIn(context: Context, b: Boolean) {
            (context.applicationContext as FhirApplication).editor.putBoolean(LOGIN, b).commit()
        }

        fun updateDetails(context: Context, it: AuthResponse) {
            (context.applicationContext as FhirApplication).editor.putString(ACCESS_TOKEN, it.token)
                .commit()
        }

        fun updateProfile(context: Context, it: String) {
            (context.applicationContext as FhirApplication).editor.putString(USER_ACCOUNT, it)
                .commit()
        }

        fun fetchAuthToken(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                ACCESS_TOKEN,
                ""
            )
        }

        fun getProfile(context: Context): String? {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                USER_ACCOUNT,
                ""
            )
        }

        fun setCurrent(context: Context, state: String) {
            (context.applicationContext as FhirApplication).editor.putString("State", state)
                .commit()
        }

        fun getCurrent(context: Context): String {
            return (context.applicationContext as FhirApplication).sharedPreferences.getString(
                "State", ""
            ).toString()
        }

    }
}
