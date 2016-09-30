package com.odoo.followup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.drive.internal.OnDriveIdResponse;
import com.odoo.followup.auth.Authenticator;

import java.util.List;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.OUser;
import odoo.listeners.IDatabaseListListener;
import odoo.listeners.IOdooConnectionListener;
import odoo.listeners.IOdooLoginCallback;
import odoo.listeners.OdooError;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        IOdooConnectionListener, IOdooLoginCallback {

    private EditText editHost, editUsername, editPassword;
    private View mView;
    private Odoo odoo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType(Authenticator.AUTH_TYPE);
        if (accounts.length > 0) {
            redirectToHome();
        }
        init();
    }

    private void init() {
        editHost = (EditText) findViewById(R.id.edtHost);
        editUsername = (EditText) findViewById(R.id.edtUsername);
        editPassword = (EditText) findViewById(R.id.edtPassword);
        findViewById(R.id.btnLogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mView = view;
        if (isValid()) {
            try {
                odoo = Odoo.createInstance(this, hostURL());
                odoo.setOnConnect(this);
            } catch (OdooVersionException e) {
                e.printStackTrace();
            }

        }
    }

    private Boolean isValid() {
        editHost.setError(null);
        editUsername.setError(null);
        editPassword.setText(null);

        if (editHost.getText().toString().trim().isEmpty()) {
            editHost.setError(getString(R.string.enter_url));
            return true;
        }

        if (editUsername.getText().toString().trim().isEmpty()) {
            editUsername.setError(getString(R.string.username_required));
            return true;
        }

        if (editPassword.getText().toString().trim().isEmpty()) {
            editPassword.setError(getString(R.string.password_required));
            return true;
        }
        return false;
    }

    private String hostURL() {
        String url = editHost.getText().toString().trim();
        if (url.contains("http://") || url.contains("https://"))
            return url;
        else
            return "http://" + url;
    }

    @Override
    public void onConnect(Odoo odoo) {

        odoo.getDatabaseList(new IDatabaseListListener() {
            @Override
            public void onDatabasesLoad(List<String> list) {
                if (list.size() > 1) {
                    LoginTo(list.get(0));
                } else {
                    LoginTo(list.get(0));
                }
            }
        });
    }

    public void LoginTo(String database) {
        odoo.authenticate(editUsername.getText().toString().trim(), editPassword.getText().toString()
                .trim(), database, this);
    }

    @Override
    public void onError(OdooError odooError) {
        Snackbar.make(mView, getString(R.string.invalid_url), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLoginSuccess(Odoo odoo, OUser oUser) {

        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account account = new Account(oUser.getAndroidName(), Authenticator.AUTH_TYPE);
        if (manager.addAccountExplicitly(account, oUser.getPassword(), oUser.getAsBundle())) {
            redirectToHome();
        }
    }

    private void redirectToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    public void onLoginFail(OdooError odooError) {
        Snackbar.make(mView, getString(R.string.invalid_username_or_password),
                Snackbar.LENGTH_LONG).show();
    }
}