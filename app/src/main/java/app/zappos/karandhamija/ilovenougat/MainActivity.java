package app.zappos.karandhamija.ilovenougat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout lView1, lView2;
    ArrayList<ProductObject> mProjectObjectList = new ArrayList<ProductObject>();
    private ProgressDialog mGetProductDialog = null;
    private ProgressDialog mCompareProductDialog = null;
    private MyRecyclerAdapter myRecyclerAdapter;
    FloatingActionButton fabSearch, fabClose;
    String mTextString = null;
    String mCompareProductID = null;
    String mCompareStyleID = null;
    String mCompareColorID = null;

    public static final String ZAPPOS_API_URL= "https://api.zappos.com/Search?term=";

    public static final String ZAPPOS_PRODUCT_KEY="&key=b743e26728e16b81da139182bb2094357c31d331";

    public static final String PM6_API_URL= "https://api.6pm.com/Search?term=";

    public static final String PM6_PRODUCT_KEY="&key=524f01b7e2906210f7bb61dcbe1bfea26eb722eb";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        lView1 = (LinearLayout) findViewById(R.id.lView1);
        lView2 = (LinearLayout) findViewById(R.id.lView2);

        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabClose = (FloatingActionButton) findViewById(R.id.fabClose);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lView1.setVisibility(View.GONE);
                lView2.setVisibility(View.VISIBLE);
                fabSearch.setVisibility(View.GONE);
                fabClose.setVisibility(View.VISIBLE);
                EditText mtext = (EditText) lView2.findViewById(R.id.searchText);
                mtext.requestFocus();

            }
        });
        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lView1.setVisibility(View.VISIBLE);
                lView2.setVisibility(View.GONE);
                fabSearch.setVisibility(View.VISIBLE);
                fabClose.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(lView1.getWindowToken(), 0);
            }
        });

        final Button mGetProductButton = (Button) findViewById(R.id.getProductButton);

        mGetProductButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                EditText mtext = (EditText) findViewById(R.id.searchText);
                Editable mString = mtext.getText();
                if(!mString.toString().isEmpty()){
                    if(isNetworkAvailable()){
                        mTextString = mString.toString().trim();
                        mTextString = mTextString.replaceAll(" ", "%20");
                        String serverURL = ZAPPOS_API_URL + mTextString + ZAPPOS_PRODUCT_KEY;
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
                        new GetProductTask().execute(serverURL);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Please input the text correctly", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    private class GetProductTask  extends AsyncTask<String, Void, Void> {

        private String mContent;
        private String mError = null;

        protected void onPreExecute() {

            mGetProductDialog = new ProgressDialog(MainActivity.this);
            if(mGetProductDialog != null && !mGetProductDialog.isShowing()){
                mGetProductDialog.setMessage("Please wait..");
                mGetProductDialog.setCanceledOnTouchOutside(false);
                mGetProductDialog.show();
            }

        }

        protected Void doInBackground(String... urls) {

            mContent = requestContent(urls[0]);

            return null;
        }

        protected void onPostExecute(Void unused) {

            if(mProjectObjectList != null){
                mProjectObjectList.clear();
            }

            if(mGetProductDialog != null && mGetProductDialog.isShowing()){
                mGetProductDialog.dismiss();
            }

            if (mError != null) {


            }

            if(mContent != null)
            {
                JSONObject jsonResponse;

                try {

                    jsonResponse = new JSONObject(mContent);

                    JSONArray jsonMainNode = jsonResponse.optJSONArray("results");

                    int lengthJsonArr = jsonMainNode.length();
                    if(lengthJsonArr != 0){
                        for(int i=0; i < lengthJsonArr; i++)
                        {
                            ProductObject newObject = new ProductObject();
                            JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                            newObject.setBrandName(jsonChildNode.optString("brandName").toString());
                            newObject.setThumbnailImageUrl(jsonChildNode.optString("thumbnailImageUrl").toString());
                            newObject.setProductId(jsonChildNode.optString("productId").toString());
                            newObject.setOriginalPrice(jsonChildNode.optString("originalPrice").toString());
                            newObject.setStyleId(jsonChildNode.optString("styleId").toString());
                            newObject.setColorId(jsonChildNode.optString("colorId").toString());
                            newObject.setPrice(jsonChildNode.optString("price").toString());
                            newObject.setPercentOff(jsonChildNode.optString("percentOff").toString());
                            newObject.setProductUrl(jsonChildNode.optString("productUrl").toString());
                            newObject.setProductName(jsonChildNode.optString("productName").toString());

                            mProjectObjectList.add(newObject);
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, " No product found! Please check some other product.", Toast.LENGTH_SHORT).show();
                    }



                } catch (JSONException e) {

                    e.printStackTrace();
                }
            displayList();

            }
            else {
                Toast.makeText(MainActivity.this, "Error getting the result from the Zappos.com", Toast.LENGTH_SHORT).show();
            }
        }

        private void displayList(){
            LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(llm);


            Log.d("karan","Display The List"+mProjectObjectList.size());
            myRecyclerAdapter = new MyRecyclerAdapter(MainActivity.this, mProjectObjectList, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isNetworkAvailable()){
                        mCompareColorID = ((TextView)v.findViewById(R.id.colorId)).getText().toString();
                        mCompareProductID = ((TextView)v.findViewById(R.id.productId)).getText().toString();
                        mCompareStyleID = ((TextView)v.findViewById(R.id.styleId)).getText().toString();
                        String serverURL = PM6_API_URL + mTextString + PM6_PRODUCT_KEY;
                        new CompareProductTask().execute(serverURL);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            recyclerView.setAdapter(myRecyclerAdapter);

        }
        public String requestContent(String url) {
            HttpClient httpclient = new DefaultHttpClient();
            String result = null;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = null;
            InputStream instream = null;

            try {
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    instream = entity.getContent();
                    result = convertStreamToString(instream);
                }

            } catch (Exception e) {
                // manage exceptions
            } finally {
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (Exception exc) {

                    }
                }
            }

            return result;
        }

        public String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }

            return sb.toString();
        }

    }



    private class CompareProductTask  extends AsyncTask<String, Void, Void> {

        private String mContent;
        private String mError = null;

        protected void onPreExecute() {

            mCompareProductDialog = new ProgressDialog(MainActivity.this);
            if(mCompareProductDialog != null && !mCompareProductDialog.isShowing()){
                mCompareProductDialog.setMessage("Comparing Product on 6pm..");
                mCompareProductDialog.setCanceledOnTouchOutside(false);
                mCompareProductDialog.show();
            }

        }

        protected Void doInBackground(String... urls) {

            mContent = requestContent(urls[0]);

            return null;
        }

        protected void onPostExecute(Void unused) {

            if (mError != null) {


            }

            boolean productFound = false;
            System.out.println("Inside onPostExecute ");
            if(mContent != null)
            {
                System.out.println("Inside onPostExecute and mContent != null");
                JSONObject jsonResponse;

                try {

                    jsonResponse = new JSONObject(mContent);

                    JSONArray jsonMainNode = jsonResponse.optJSONArray("results");

                    int lengthJsonArr = jsonMainNode.length();
                    if(lengthJsonArr != 0){
                        for(int i=0; i < lengthJsonArr; i++)
                        {
                            System.out.println("Inside onPostExecute comparing products ");
                            JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                            String productID = jsonChildNode.optString("productId").toString();
                            String styleID = jsonChildNode.optString("styleId").toString();
                            String colorID = jsonChildNode.optString("colorId").toString();

                            if(productID.equalsIgnoreCase(mCompareProductID) && styleID.equalsIgnoreCase(mCompareStyleID)
                                    && colorID.equalsIgnoreCase(mCompareColorID)){

                                productFound = true;
                                break;
                            }

                        }
                    }

                    if(mCompareProductDialog != null && mCompareProductDialog.isShowing()){
                        mCompareProductDialog.dismiss();
                    }

                    if(productFound){
                        Toast.makeText(MainActivity.this, " Similar product found on 6PM !!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, " No product found on 6PM", Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
            else {

                if(mCompareProductDialog != null && mCompareProductDialog.isShowing()){
                    mCompareProductDialog.dismiss();
                }

                Toast.makeText(MainActivity.this, "Error getting the result from the 6PM", Toast.LENGTH_SHORT).show();
            }

        }

        public String requestContent(String url) {
            HttpClient httpclient = new DefaultHttpClient();
            String result = null;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = null;
            InputStream instream = null;

            try {
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    instream = entity.getContent();
                    result = convertStreamToString(instream);
                }

            } catch (Exception e) {
                // manage exceptions
            } finally {
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (Exception exc) {

                    }
                }
            }

            return result;
        }

        public String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }

            return sb.toString();
        }

    }

    @Override
    public void onPause(){

        super.onPause();
        if(mGetProductDialog != null){
            mGetProductDialog.dismiss();
        }
        if(mCompareProductDialog != null){
            mCompareProductDialog.dismiss();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
