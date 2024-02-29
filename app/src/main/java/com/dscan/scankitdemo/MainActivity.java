package com.dscan.scankitdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class MainActivity extends AppCompatActivity {

    // AccountAuthService provides a set of APIs, including silentSignIn, getSignInIntent, and signOut.
    private AccountAuthService mAuthService;

    // Set HUAWEI ID sign-in authorization parameters.
    private AccountAuthParams mAuthParam;

    // Define the request code for signInIntent.
    private static final int REQUEST_CODE_SIGN_IN = 1000;

    // Define the log flag.
    private static final String TAG = "Account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HwAds.init(this);

        BannerView bannerView = findViewById(R.id.hw_banner_view);
        bannerView.setAdId("testw6vs28auh3");
        bannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
        AdParam adParam = new AdParam.Builder().build();
        bannerView.loadAd(adParam);

        findViewById(R.id.HuaweiIdAuthButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silentSignInByHwId();
            }
        });
    }

    private void silentSignInByHwId() {
        // 1. Use AccountAuthParams to specify the user information to be obtained, including the user ID (OpenID and UnionID), email address, and profile (nickname and picture).
        // 2. By default, DEFAULT_AUTH_REQUEST_PARAM specifies two items to be obtained, that is, the user ID and profile.
        // 3. If your app needs to obtain the user's email address, call setEmail().
        //  4. To support ID token-based HUAWEI ID sign-in, use setIdToken(). User information can be parsed from the ID token.
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setEmail()
                .setIdToken()
                .createParams();
        // Use AccountAuthParams to build AccountAuthService.
        mAuthService = AccountAuthManager.getService(this, mAuthParam);
        // Sign in with a HUAWEI ID silently.
        Task<AuthAccount> task = mAuthService.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                // The silent sign-in is successful. Process the returned AuthAccount object to obtain the HUAWEI ID information.
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
                dealWithResultOfSignIn(authAccount);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // The silent sign-in fails. Your app will call getSignInIntent() to show the authorization or sign-in screen.
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Intent signInIntent = mAuthService.getSignInIntent();
                    startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
                }
            }
        });
    }

    /**
     * Process the returned AuthAccount object to obtain the HUAWEI ID information.
     *
     * @param authAccount AuthAccount object, which contains the HUAWEI ID information.
     */
    private void dealWithResultOfSignIn(AuthAccount authAccount) {
        Log.i(TAG, "idToken:" + authAccount.getIdToken());
        // TODO: After obtaining the ID token, your app will send it to your app server if there is one. If you have no app server, your app will verify and parse the ID token locally.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Log.i(TAG, "onActivitResult of sigInInIntent, request code: " + REQUEST_CODE_SIGN_IN);
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                // The sign-in is successful, and the authAccount object that contains the HUAWEI ID information is obtained.
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
                AuthAccount authAccount = authAccountTask.getResult();
                dealWithResultOfSignIn(authAccount);
                Log.i(TAG, "onActivitResult of sigInInIntent, request code: " + REQUEST_CODE_SIGN_IN);
            } else {
                // The sign-in fails. Find the failure cause from the status code. For more information, please refer to the "Error Codes" section in the API Reference.
                Log.e(TAG, "sign in failed : " +((ApiException)authAccountTask.getException()).getStatusCode());
            }
        }
    }
}