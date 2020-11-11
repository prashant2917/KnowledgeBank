package com.pocket.knowledge.utils

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.ads.consent.*
import com.pocket.knowledge.R
import java.net.MalformedURLException
import java.net.URL

object GDPR {
    @JvmStatic
    fun getBundleAd(act: Activity?): Bundle {
        val extras = Bundle()
        val consentInformation = ConsentInformation.getInstance(act)
        if (consentInformation.consentStatus == ConsentStatus.NON_PERSONALIZED) {
            extras.putString("npa", "1")
        }
        return extras
    }

    @JvmStatic
    fun updateConsentStatus(act: Activity) {
        val consentInformation = ConsentInformation.getInstance(act)
        // for debug needed
        //consentInformation.addTestDevice("6E03755720167250AEBF7573B4E86B62");
        //consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        consentInformation.requestConsentInfoUpdate(arrayOf(act.getString(R.string.admob_publisher_id)), object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated. Display the consent consentForm if Consent Status is UNKNOWN
                if (consentStatus == ConsentStatus.UNKNOWN) {
                    GDPRForm(act).displayConsentForm()
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // Consent consentForm error.
                Log.e("GDPR", errorDescription)
            }
        })
    }

     class GDPRForm(private val activity: Activity) {
        private var consentForm: ConsentForm? = null
        fun displayConsentForm() {
            val builder = ConsentForm.Builder(activity, getUrlPrivacyPolicy(activity))
            builder.withPersonalizedAdsOption()
            builder.withNonPersonalizedAdsOption()
            builder.withListener(object : ConsentFormListener() {
                override fun onConsentFormLoaded() {
                    // Consent consentForm loaded successfully.
                    consentForm!!.show()
                }

                override fun onConsentFormOpened() {
                    // Consent consentForm was displayed.
                }

                override fun onConsentFormClosed(consentStatus: ConsentStatus, userPrefersAdFree: Boolean) {
                    // Consent consentForm was closed.
                    Log.e("GDPR", "Status : $consentStatus")
                }

                override fun onConsentFormError(errorDescription: String) {
                    // Consent consentForm error.
                    Log.e("GDPR", errorDescription)
                }
            })
            consentForm = builder.build()
            consentForm?.load()
        }

        private fun getUrlPrivacyPolicy(act: Activity): URL? {
            var mUrl: URL? = null
            try {
                mUrl = URL(act.getString(R.string.privacy_policy_url))
            } catch (e: MalformedURLException) {
                Log.e("GDPR", e.message)
            }
            return mUrl
        }

    }
}