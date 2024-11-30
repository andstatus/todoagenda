package org.andstatus.todoagenda

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

open class MyActivity : AppCompatActivity() {
    override fun setTitle(title: CharSequence?) {
        findViewById<Toolbar>(R.id.my_action_bar)?.title = title
    }

    override fun setTitle(titleId: Int) {
        findViewById<Toolbar>(R.id.my_action_bar)?.setTitle(titleId)
    }

}
