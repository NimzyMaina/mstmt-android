package com.openshamba.mpesa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.openshamba.mpesa.app.BaseActivity;
import com.openshamba.mpesa.data.ServiceGenerator;
import com.openshamba.mpesa.data.entities.ApiResponse;
import com.openshamba.mpesa.services.ErrorHandler;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends BaseActivity {

    private static final int MY_PERMISSION_REQUEST = 100;

    private int PICK_IMAGE_FROM_GALLARY_REQUEST =1;

    EditText id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        if(ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(UploadActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST);
        }

        Button uploadBtn = (Button) findViewById(R.id.button2);
        id = (EditText) findViewById(R.id.idNumber);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.getText().toString().isEmpty()){
                    showSnackBar("Please enter the ID number","error");
                    return;
                }
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(
                        Intent.createChooser(intent,"Select Mpesa Statement"),
                        PICK_IMAGE_FROM_GALLARY_REQUEST
                );
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout){
            logoutUser();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_FROM_GALLARY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            Uri uri = data.getData();
            upload(uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else {

                }
                return;
            }

        }
    }

    private void upload(Uri uri) {

        showpDialog();

        // Description text for teh file
        RequestBody descriptionPart = RequestBody.create(MultipartBody.FORM,id.getText().toString());

        // Actual file being uploaded
        File originalFile = FileUtils.getFile(this,uri);

        // Get file part
        RequestBody filePart = RequestBody.create(
                MediaType.parse(getContentResolver().getType(uri)),
                FileUtils.getFile(this,uri)
        );

        // Get multipart file object
        MultipartBody.Part file = MultipartBody.Part.createFormData("file",originalFile.getName(),filePart);

        // Make call object
        Call<ResponseBody> call = ServiceGenerator.getClient(session.getKeyApiKey()).upload(descriptionPart,file);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hidepDialog();
                if (response.isSuccessful()) {
                    showSnackBar("File was uploaded successfully","success");
                }
                else {
                    ApiResponse error = ErrorHandler.parseError(response);
                    showSnackBar(error.getMessage(),"error");
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hidepDialog();
                showSnackBar("Unable to upload file to server","error");
            }
        });
    }
}
