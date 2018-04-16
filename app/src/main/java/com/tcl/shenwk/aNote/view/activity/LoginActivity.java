package com.tcl.shenwk.aNote.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.service.ANoteService;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.TextUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shenwk on 2018/4/3.
 */

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    public static final String FRAGMENT_TAG_LOGIN = "login";

    private static final int LOGIN_RESULT_SUCCESS = 0;
    private static final int LOGIN_RESULT_LOGIN_INFO_WRONG = 1;
    private static final int LOGIN_RESULT_SERVER_DATABASE_ERROR = 2;
    private static final int LOGIN_RESULT_PASSWORD_WRONG = 3;
    private static final int LOGIN_RESULT_EMAIL_NOT_ACTIVATED = 4;

    private LoginManager loginManager;
    private TextView emailText;
    private TextView passwordText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        View view = findViewById(R.id.sign_in);
        view.setOnClickListener(signInOnClickListener);
        view = findViewById(R.id.sign_up_hint);
        view.setOnClickListener(signUpOnClickListener);

        emailText = findViewById(R.id.input_email);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(Constants.PREFERENCE_FIELD_USER_EMAIL, "");
        emailText.setText(email);
        passwordText = findViewById(R.id.input_password);

        loginManager = LoginManager.getInstance(getApplicationContext());
    }

    private View.OnClickListener signInOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isInputValid()) {
                NetworkBase networkBase = NetworkBase.getInstance(getApplicationContext());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.PREFERENCE_FIELD_USER_EMAIL, emailText.getText().toString());
                    jsonObject.put(Constants.PREFERENCE_FIELD_USER_PASSWORD, passwordText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, UrlSource.URL_SIGN_IN, jsonObject, signInResponseListener, signInErrorListener);
                networkBase.addRequest(jsonRequest);
            }else{
                Log.i(TAG, "onClick: input invalid");
            }
        }
    } ;

    private boolean isInputValid(){
        if(!TextUtil.isValidEmail(emailText.getText())){
            Toast.makeText(getApplicationContext(), R.string.email_address_not_valid, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(passwordText.getText().length() < 1){
            Toast.makeText(getApplicationContext(), R.string.toast_password_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private View.OnClickListener signUpOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchToFragment(FRAGMENT_TAG_LOGIN);
        }
    };

    private void switchToFragment(String tag){
        loginManager.switchToFragment(getFragmentManager(), tag);
    }

    @Override
    public void onBackPressed() {
        if(!getFragmentManager().popBackStackImmediate())
            super.onBackPressed();
    }

    Response.Listener<JSONObject> signInResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if(response != null){
                try {
                    switch(((int) response.get("result"))){
                        case LOGIN_RESULT_SUCCESS:{
                            Log.i(TAG, "onResponse: login check passed");
                            loginManager.saveUserInfo(getApplicationContext(), response.getJSONObject("userInfo"));
                            startService(new Intent(getApplicationContext(), ANoteService.class));
                            loginManager.toHomePage(getApplicationContext());
                            finish();
                            break;
                        }
                        case LOGIN_RESULT_LOGIN_INFO_WRONG:{
                            Log.i(TAG, "onResponse: user info error");
                            Toast.makeText(getApplicationContext(), R.string.toast_login_email_or_password_wrong, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case LOGIN_RESULT_SERVER_DATABASE_ERROR:{
                            Log.i(TAG, "onResponse: server database error");
                            Toast.makeText(getApplicationContext(), R.string.toast_login_server_database_error, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case LOGIN_RESULT_PASSWORD_WRONG:{
                            Log.i(TAG, "onResponse: password error");
                            Toast.makeText(getApplicationContext(), R.string.toast_login_email_or_password_wrong, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case LOGIN_RESULT_EMAIL_NOT_ACTIVATED:{
                            Toast.makeText(getApplicationContext(), R.string.toast_login_email_not_activated, Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onResponse: user email is not activated now");
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Response.ErrorListener signInErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onErrorResponse: sign in error " + error);
            Toast.makeText(getApplicationContext(), R.string.toast_server_response_error, Toast.LENGTH_SHORT).show();
        }
    };
}
