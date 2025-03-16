package org.andstatus.todoagenda.prefs

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder

/**
 * See http://stackoverflow.com/questions/9220039/android-preferencescreen-title-in-two-lines
 */
class MultilineCheckBoxPreference : CheckBoxPreference {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr,
    )

    override fun onBindViewHolder(viewHolder: PreferenceViewHolder) {
        super.onBindViewHolder(viewHolder)
        val textView = viewHolder.findViewById(android.R.id.title) as? TextView
        if (textView != null) {
            textView.isSingleLine = false
        }
    }
}
