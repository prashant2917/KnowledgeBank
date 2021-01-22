package com.pocket.knowledge.activities

import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {


    open fun setToolBarTitle(title:String){
    //todo override this if want to set title to toolbar
    }
}