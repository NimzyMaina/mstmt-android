package com.openshamba.mpesa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.openshamba.mpesa.app.BaseActivity;
import com.openshamba.mpesa.data.ServiceGenerator;
import com.openshamba.mpesa.data.entities.ApiResponse;
import com.openshamba.mpesa.data.entities.LoginResponse;
import com.openshamba.mpesa.services.ErrorHandler;

import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

//    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(session.isLoggedIn()){
           startActivity(new Intent(MainActivity.this,UploadActivity.class));
        }

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.openshamba.mpesa",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        // Find fb button
        loginButton = (LoginButton)findViewById(R.id.login_button);
        // find txtview
        textView = (TextView) findViewById(R.id.textView);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setVisibility(View.INVISIBLE);
            }
        });

        // assign callback to button
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                login(loginResult.getAccessToken().getToken());
                //loginButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                loginButton.setVisibility(View.VISIBLE);
                showSnackBar("Login attempt canceled.","error");
            }

            @Override
            public void onError(FacebookException e) {
                loginButton.setVisibility(View.VISIBLE);
                showSnackBar("Login attempt Failed","error");
            }
        });
    }

    private void login(String auth_token) {

        showpDialog("Hang on! Just one more step...");

        retrofit2.Call<LoginResponse> call = ServiceGenerator.getClient().login(
                auth_token
        );

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LoginResponse> call, Response<LoginResponse> response) {
                hidepDialog();
                if(response.isSuccessful()) {
                    session.setLogin(true,response.body().getData());
                    startActivity(new Intent(MainActivity.this, UploadActivity.class));
                    finish();
                }else {
                    loginButton.setVisibility(View.VISIBLE);
                    fbLogout();
                    ApiResponse error = ErrorHandler.parseError(response);
                    showSnackBar(error.getMessage(),"error");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<LoginResponse> call, Throwable t) {
                loginButton.setVisibility(View.VISIBLE);
                hidepDialog();
                fbLogout();
                showSnackBar("Please check your internet connection","error");
            }
        });

    }

}
