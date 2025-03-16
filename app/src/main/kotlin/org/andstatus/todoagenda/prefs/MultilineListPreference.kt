package org.andstatus.todoagenda.prefs

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder

class MultilineListPreference : ListPreference {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onBindViewHolder(viewHolder: PreferenceViewHolder) {
        super.onBindViewHolder(viewHolder)
        val textView = viewHolder.findViewById(android.R.id.title) as? TextView?
        if (textView != null) {
            textView.isSingleLine = false
        }
    }
}
