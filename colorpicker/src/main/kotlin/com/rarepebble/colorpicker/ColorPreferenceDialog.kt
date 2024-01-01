/*
 * Copyright (C) 2015 Martin Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rarepebble.colorpicker

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat

/** AndroidX version created by yvolk@yurivolkov.com
 * based on this answer: https://stackoverflow.com/a/53290775/297710
 * and on the code of https://github.com/koji-1009/ChronoDialogPreference
 */
class ColorPreferenceDialog(private val preference: ColorPreference) : PreferenceDialogFragmentCompat() {
    private var mPicker: ColorPickerView? = null

    init {
        val b = Bundle()
        b.putString(ARG_KEY, preference.key)
        arguments = b
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nexus 7 needs the keyboard hiding explicitly.
        // A flag on the activity in the manifest doesn't
        // apply to the dialog, so needs to be in code:
        val window = requireActivity().window
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onCreateDialogView(context: Context): View? {
        val picker = ColorPickerView(getContext())
        picker.color = preference.color ?: Color.GRAY
        picker.showAlpha(preference.showAlpha)
        picker.showHex(preference.showHex)
        picker.showPreview(preference.showPreview)
        mPicker = picker
        return mPicker
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as AlertDialog?
        if (preference.selectNoneButtonText != null && preference.defaultColor != null && mPicker != null && dialog != null) {
            // In order to prevent dialog from closing we setup its onLickListener this late
            val neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
            neutralButton?.setOnClickListener { mPicker!!.setCurrentColor(preference.defaultColor!!) }
        }
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        if (preference.selectNoneButtonText != null) {
            builder.setNeutralButton(preference.selectNoneButtonText, null)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            mPicker?.let { picker ->
                val color = picker.color
                if (preference.callChangeListener(color)) {
                    preference.color = color
                }
            }
        }
    }
}
