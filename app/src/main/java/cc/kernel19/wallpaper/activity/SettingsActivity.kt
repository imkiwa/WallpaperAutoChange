package cc.kernel19.wallpaper.activity


import cc.kernel19.wallpaper.R

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMenuItemSelected(featureId: Int, item: android.view.MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                android.support.v4.app.NavUtils.navigateUpFromSameTask(this)
            }
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }


    override fun onIsMultiPane(): Boolean {
        return cc.kernel19.wallpaper.activity.SettingsActivity.Companion.isXLargeTablet(this)
    }

    @android.annotation.TargetApi(android.os.Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<android.preference.PreferenceActivity.Header>) {
        loadHeadersFromResource(cc.kernel19.wallpaper.R.xml.pref_headers, target)
    }


    override fun isValidFragment(fragmentName: String): Boolean {
        return android.preference.PreferenceFragment::class.java.name == fragmentName
                || cc.kernel19.wallpaper.activity.SettingsActivity.GeneralPreferenceFragment::class.java.name == fragmentName
    }

    @android.annotation.TargetApi(android.os.Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : android.preference.PreferenceFragment() {
        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(cc.kernel19.wallpaper.R.xml.pref_wallpaper)
            setHasOptionsMenu(true)

            cc.kernel19.wallpaper.activity.SettingsActivity.Companion.bindPreferenceSummaryToValue(findPreference(getString(R.string.key_frequency)))
        }

        override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(android.content.Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = android.preference.Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is android.preference.ListPreference) {
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: android.content.Context): Boolean {
            return context.resources.configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK >= android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: android.preference.Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = cc.kernel19.wallpaper.activity.SettingsActivity.Companion.sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            cc.kernel19.wallpaper.activity.SettingsActivity.Companion.sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    android.preference.PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}
