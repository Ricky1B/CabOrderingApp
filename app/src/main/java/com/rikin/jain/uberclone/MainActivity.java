package com.rikin.jain.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.btnSignUpLogIn:
               if(state == State.SIGNUP){
                   if(rdoPassenger.isChecked() == false && rdoDriver.isChecked() == false){
                       Toast.makeText(this,"Specify Passenger or Driver!", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   final ParseUser appUser = new ParseUser();
                   appUser.setUsername(edtUsername.getText().toString());
                   appUser.setPassword(edtPassword.getText().toString());
                   if(rdoDriver.isChecked()){
                       appUser.put("as","Driver");
                   } else if(rdoPassenger.isChecked()){
                       appUser.put("as","Passenger");
                   }
                   appUser.signUpInBackground(new SignUpCallback() {
                       @Override
                       public void done(ParseException e) {
                           if(e == null){
                               Toast.makeText(MainActivity.this,"Signed Up",Toast.LENGTH_SHORT).show();
                               transitionToPassengerActivity();
                           }
                       }
                   });
               } else if(state ==State.LOGIN){
                   if(rdoPassenger.isChecked() == false && rdoDriver.isChecked() == true){
                       Toast.makeText(this,"Specify Passenger or Driver!", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                       @Override
                       public void done(ParseUser user, ParseException e) {
                           if(user != null && e==null){
                               Toast.makeText(MainActivity.this,"User logged in",Toast.LENGTH_SHORT).show();
                               transitionToPassengerActivity();

                           }
                       }
                   });
               }
               break;
           case R.id.btnOneTimeLogin:
               if(edtPassengerOrDriver.getText().toString().equals("Driver") || edtPassengerOrDriver.getText().toString().equals("Passenger")){
                   if(ParseUser.getCurrentUser() ==null){
                       ParseAnonymousUtils.logIn(new LogInCallback() {
                           @Override
                           public void done(ParseUser user, ParseException e) {
                               if(user !=null && e == null){
                                   Toast.makeText(MainActivity.this,"We have an anonymous user",Toast.LENGTH_SHORT).show();
                                   user.put("as",edtPassengerOrDriver.getText().toString());
                                   user.saveInBackground(new SaveCallback() {
                                       @Override
                                       public void done(ParseException e) {
                                           if(e==null){
                                               transitionToPassengerActivity();
                                           }
                                       }
                                   });
                               }
                           }
                       });
                   }
                   else {
                       Toast.makeText(MainActivity.this,"You are already logged in",Toast.LENGTH_SHORT).show();

                   }
               } else {
                   Toast.makeText(MainActivity.this,"Specify Driver or Passenger",Toast.LENGTH_SHORT).show();

               }
               break;
       }
    }

    enum State{
        SIGNUP, LOGIN
    }
    private State state;
    private EditText edtUsername, edtPassword, edtPassengerOrDriver;
    private Button btnSignUpLogIn, btnOneTimeLogin;
    private RadioButton rdoPassenger, rdoDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(ParseUser.getCurrentUser() != null){
            //ParseUser.logOut();
            transitionToPassengerActivity();
        }
        state = State.SIGNUP;
        btnSignUpLogIn = findViewById(R.id.btnSignUpLogIn);
        btnSignUpLogIn.setOnClickListener(this);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        btnOneTimeLogin.setOnClickListener(this);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassengerOrDriver = findViewById(R.id.edtPassengerOrDriver);
        rdoDriver = findViewById(R.id.rdoDriver);
        rdoPassenger = findViewById(R.id.rdoPassenger);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()){
           case R.id.logInuserItem:
               if(state == State.SIGNUP){
                   state = State.LOGIN;
                   item.setTitle("Signup");
                   btnSignUpLogIn.setText("LOGIN");
               } else if(state == State.LOGIN){
                   state = State.SIGNUP;
                   item.setTitle("Login");
                   btnSignUpLogIn.setText("SIGNUP");
               }
               break;
       }
        return super.onOptionsItemSelected(item);
    }
    public void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(MainActivity.this,PassengerActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
