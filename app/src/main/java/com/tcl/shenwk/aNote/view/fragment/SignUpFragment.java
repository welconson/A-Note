package com.tcl.shenwk.aNote.view.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.util.TextUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shenwk on 2018/4/4.
 */

public class SignUpFragment extends Fragment{
    private static final String TAG = "SignUpFragment";

    private static final int SIGN_UP_RESULT_SUCCESS = 0;
    private static final int SIGN_UP_RESULT_ALREADY_SIGN_UP = 1;
    private static final int SIGN_UP_RESULT_HAVE_NOT_ACTIVATED = 2;
    private static final int SIGN_UP_RESULT_SEND_EMAIL_ERROR = 3;
    private static final int SIGN_UP_RESULT_SERVER_DATABASE_ERROR = 4;


    private View button;
    private TextView inputEmail;
    private TextView inputPassword;
    private TextView inputConfirmPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_up_layout, container, false);
        button = view.findViewById(R.id.sign_up);
        button.setOnClickListener(signUpOnClickListener);
        inputEmail = view.findViewById(R.id.input_email);
        inputPassword = view.findViewById(R.id.input_password);
        inputConfirmPassword = view.findViewById(R.id.input_confirm_password);
        view.findViewById(R.id.sign_up_hint).setOnClickListener(hintOnClickListener);
        return view;
    }



    private View.OnClickListener hintOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener signUpOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isInputValid()){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("email", inputEmail.getText());
                    jsonObject.put("password", inputPassword.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                NetworkBase networkBase = new NetworkBase(getContext());
                networkBase.sendRequest(UrlSource.URL_SIGN_UP, jsonObject, resListener, resErrListener);
                Log.i(TAG, "onClick: url " + UrlSource.URL_SIGN_UP);
            }
        }
    } ;

    private boolean isInputValid(){
        if(!TextUtil.isValidEmail(inputEmail.getText())){
            Toast.makeText(getContext(), R.string.email_address_not_valid, Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!TextUtil.isPasswordMatched(inputPassword.getText(), inputConfirmPassword.getText())){
            Toast.makeText(getContext(), R.string.confirm_password_not_match, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    Response.Listener<JSONObject> resListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.i(TAG, "onResponse: get response from server");
            try {
                Log.i(TAG, "onResponse: res = " + response.get("result"));
                switch (((int) response.get("result"))){
                    case SIGN_UP_RESULT_SUCCESS:
                        Log.i(TAG, "onResponse: new sign up email and activating email is sent");
                        break;
                    case SIGN_UP_RESULT_HAVE_NOT_ACTIVATED:
                        Log.i(TAG, "onResponse: the email has been signed up before, need a new activating email?");
                        break;
                    case SIGN_UP_RESULT_ALREADY_SIGN_UP:
                        Log.i(TAG, "onResponse: the email has been signed up and activated, login in now");
                        break;
                    case SIGN_UP_RESULT_SEND_EMAIL_ERROR:
                        Log.i(TAG, "onResponse: server sends activating email error ");
                        break;
                    case SIGN_UP_RESULT_SERVER_DATABASE_ERROR:
                        Log.i(TAG, "onResponse: server database error");
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Response.ErrorListener resErrListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onErrorResponse: " + error);
            Toast.makeText(getContext(), "response error from server", Toast.LENGTH_SHORT).show();
        }
    };
}
