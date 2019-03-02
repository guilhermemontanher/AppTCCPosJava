package com.example.guilhermemontanher.apptccposjava;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.guilhermemontanher.apptccposjava.databinding.ActivityMainBinding;
import com.example.guilhermemontanher.apptccposjava.listener.RecyclerViewButtonClick;
import com.example.guilhermemontanher.apptccposjava.model.Param;
import com.example.guilhermemontanher.apptccposjava.view.adapter.RecyclerAdapterParams;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Guilherme Montanher
 * @version 1.0
 * @since 20/01/2019
 */
public class MainActivity extends AppCompatActivity implements RecyclerViewButtonClick {

    private ActivityMainBinding binding;

    private List<Param> paramList = new ArrayList<>();

    private RecyclerAdapterParams recyclerAdapterParams;

    private long startTime = 0;
    private long endTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(MainActivity.this);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url = binding.editTextUrl.getText().toString();
                int method = binding.spinnerMethod.getSelectedItemPosition();

                //Constructor with method, url, response listener and error response listener
                startTime = SystemClock.elapsedRealtime();
                StringRequest stringRequest = new StringRequest(method, url,
                        new Response.Listener<String>() {
                            @SuppressLint({"SetJavaScriptEnabled", "SimpleDateFormat"})
                            @Override
                            public void onResponse(String response) {
                                endTime = SystemClock.elapsedRealtime();
                                binding.webViewPage.getSettings().setJavaScriptEnabled(true);
                                binding.webViewPage.loadDataWithBaseURL("", response, "text/html", "UTF-8", "");
                                StringBuilder output = new StringBuilder();
                                output.append("Successful requisition!\r\n");
                                output.append("Resquest time - " + String.valueOf(new SimpleDateFormat("ss.SSS").format(new Date(endTime - startTime))));
                                Snackbar.make(binding.rootView, output, Snackbar.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        final Snackbar snackBar = Snackbar.make(binding.rootView, "That didn't work!", Snackbar.LENGTH_LONG);
                        snackBar.setAction("Try again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                binding.imageButtonSend.callOnClick();
                                snackBar.dismiss();
                            }
                        });
                        snackBar.show();

                    }
                }) { //Optional methods, used for authentitacion, passing parameters, etc
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();

                        if (binding.spinnerAuth.getSelectedItemPosition() == 1) {
                            String credentials = binding.editTextUsername.getText() + ":" + binding.editTextPassword.getText();
                            String auth = "Basic "
                                    + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", auth);
                        }

                        return headers;
                    }

                    @Override
                    public String getBodyContentType() {
                        return super.getBodyContentType();
                    }

                    @Override
                    public byte[] getBody() {
                        HashMap<String, String> params = new HashMap<String, String>();

                        for (Param param : paramList) {
                            params.put(param.getName(), param.getValue());
                        }
                        return new JSONObject(params).toString().getBytes();
                    }
                };

                try {
                    Log.d("Headers", stringRequest.getHeaders().toString());
                } catch (AuthFailureError authFailureError) {
                    authFailureError.printStackTrace();
                }
                queue.add(stringRequest);
            }
        });

        binding.buttonAddParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.editTextParamName.getText().toString().isEmpty()) {
                    paramList.add(
                            new Param(binding.editTextParamName.getText().toString(),
                                    binding.editTextParamValue.getText().toString())
                    );
                    recyclerAdapterParams.notifyDataSetChanged();

                } else {
                    binding.editTextParamName.setError("Required field");
                }
            }
        });

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerViewParam.setLayoutManager(layoutManager);
        binding.recyclerViewParam.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerViewParam.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        recyclerAdapterParams = new RecyclerAdapterParams(this, paramList);
        binding.recyclerViewParam.setAdapter(recyclerAdapterParams);
        recyclerAdapterParams.setOnClickRecyclerViewButtonClick(this);

        binding.spinnerAuth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        binding.viewBasicAuth.setVisibility(View.GONE);
                        break;
                    case 1:
                        binding.viewBasicAuth.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(int position) {
        paramList.remove(position);
        recyclerAdapterParams.notifyDataSetChanged();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
