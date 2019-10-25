package com.example.nextdestination;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create a thread pool with a single thread//

        Executor newExecutor = Executors.newSingleThreadExecutor();

        FragmentActivity activity = this;

        //Start listening for authentication events//

        final BiometricPrompt myBiometricPrompt = new BiometricPrompt(activity, newExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override

            //onAuthenticationError is called when a fatal error occurrs//

            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                } else {

                    //Print a message to Logcat//

                    Log.d(TAG, "An unrecoverable error occurred");
                }
            }

            //onAuthenticationSucceeded is called when a fingerprint is matched successfully//

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                //Print a message to Logcat//

                Log.d(TAG, "Fingerprint recognised successfully");
                Intent home = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(home);
            }

            //onAuthenticationFailed is called when the fingerprint doesn’t match//

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                //Print a message to Logcat//

                Log.d(TAG, "Fingerprint not recognised");
            }
        });

        //Create the BiometricPrompt instance//

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()

                //Add some text to the dialog//

                .setTitle("Sign Into Next Destination")
                .setSubtitle("Begin Your Adventure")
                .setNegativeButtonText("Cancel")

                //Build the dialog//

                .build();

        //Assign an onClickListener to the app’s “Authentication” button//

        findViewById(R.id.launchAuthentication).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBiometricPrompt.authenticate(promptInfo);
            }
        });

    }


}
