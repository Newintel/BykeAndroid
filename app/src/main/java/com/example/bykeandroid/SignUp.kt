package com.example.bykeandroid

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.content.Intent;
import android.os.Bundle;
import android.view.View;




class SignUp : Fragment() {
    var bSingUp: Button? = null

    var etUserName: EditText? = null
    var etLastName: EditText? = null
    var etFirstName: EditText? = null
    var etBirthday: EditText? = null
//    var etEmail: EditText? = null
    var etPassword: EditText? = null


//    btninsert = (Button)findViewById(R.id.btn_insert);
//    btninsert.setOnClickListener( new View.OnClickListener() {
//        public void onClick(View v) {
//            insertValues();
//            EditText userName = (EditText) findViewById(R.id.editText1);
//
//            if( userName.getText().toString().length() == 0 )
//                userName.setError( "First name is required!" );
//
//            Intent i = new Intent(getApplicationContext(), Login.class);
//            startActivity(i);
//        }
//    });
}