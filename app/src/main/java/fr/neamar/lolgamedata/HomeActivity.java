package fr.neamar.lolgamedata;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import fr.neamar.lolgamedata.adapter.AccountAdapter;
import fr.neamar.lolgamedata.pojo.Account;

public class HomeActivity extends SnackBarActivity {
    public static final String TAG = "HomeActivity";

    public SharedPreferences prefs;

    public RecyclerView recyclerView;

    public AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        prefs = getPreferences(MODE_PRIVATE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAccount();
            }
        });

        accountManager = new AccountManager(this);

        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initView() {
        ArrayList<Account> accounts = accountManager.getAccounts();

        AccountAdapter adapter = new AccountAdapter(accounts, this);
        recyclerView.setAdapter(adapter);
    }


    public void addAccount() {
        final Dialog d = new Dialog(this);
        d.setTitle(R.string.add_account_title);
        d.setContentView(R.layout.dialog_add_account);

        d.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                String name = ((TextView) d.findViewById(R.id.summonerText)).getText().toString();
                String region = ((Spinner) d.findViewById(R.id.summonerRegion)).getSelectedItem().toString().replaceAll(" .+$", "");

                Log.i(TAG, "Adding new account: " + name + " (" + region + ")");

                saveAccount(name, region);
            }
        });
        d.show();
    }

    public Account saveAccount(String name, String region) {
        final Account newAccount = new Account(name, region, "");

        final ProgressDialog dialog = ProgressDialog.show(this, "",
                String.format(getString(R.string.loading_summoner_data), name), true);
        dialog.show();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, "http://lol-game-stats.herokuapp.com/summoner/data?summoner=" + name + "&region=" + region, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        newAccount.summonerImage = response.optString("profileIcon", "");

                        accountManager.addAccount(newAccount);

                        AccountAdapter adapter = (AccountAdapter) recyclerView.getAdapter();
                        adapter.updateAccounts(accountManager.getAccounts());

                        dialog.dismiss();

                        displaySnack(String.format("Added account %s", newAccount.summonerName));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Log.e(TAG, error.toString());

                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    Log.i(TAG, responseBody);

                    displaySnack(responseBody.isEmpty() ? "Unable to load player data" : responseBody);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                }
            }
        });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonRequest);

        return newAccount;
    }
}