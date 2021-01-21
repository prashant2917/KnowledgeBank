package com.pocket.knowledge.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


   open fun setToolBarTitle(title:String){
    //todo override this if want to set title to toolbar
    }
}