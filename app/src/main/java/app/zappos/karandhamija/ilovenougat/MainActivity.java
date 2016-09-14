package app.zappos.karandhamija.ilovenougat;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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

    private RecyclerView mRecyclerView;
    private LinearLayout lView1, lView2;
    ArrayList<ProductObject> mProjectObjectList = new ArrayList<ProductObject>();
    private ProgressDialog mGetProductDialog = null;
    private ProgressDialog mCompareProductDialog = null;
    private MyRecyclerAdapter myRecyclerAdapter;
    FloatingActionButton fabSearch, fabClose;
    String mTextString = null;
    String mCompareProductID = null;
    String mComparePrice = null;
    private int mTotalCount;
    private EditText mtext;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 6;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    static int mPageNumber = 2;
    static int currentQueryProvider = 0;    // 0 for Zappos and 1 for 6PM

    public static final String ZAPPOS_API_URL= "https://api.zappos.com/Search?term=";

    public static final String ZAPPOS_PRODUCT_KEY="&key=b743e26728e16b81da139182bb2094357c31d331";

    public static final String PM6_API_URL= "https://api.6pm.com/Search?term=";

    public static final String PAGE_STRING= "&page=";

    public static final String ZAPPOS = "ZAPPOS.COM";

    public static final String PM6 = "6PM.COM";

    public static final String PM6_PRODUCT_KEY="&key=524f01b7e2906210f7bb61dcbe1bfea26eb722eb";

    public void initializeVariables(){
        mTotalCount = 0;
        previousTotal = 0;
        loading = true;
        mPageNumber = 2;
        visibleThreshold = 6;
        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        lView1 = (LinearLayout) findViewById(R.id.lView1);
        lView2 = (LinearLayout) findViewById(R.id.lView2);
        mtext = (EditText) findViewById(R.id.searchText);

        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);
        fabClose = (FloatingActionButton) findViewById(R.id.fabClose);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lView1.setVisibility(View.GONE);
                lView2.setVisibility(View.VISIBLE);
                fabSearch.setVisibility(View.GONE);
                fabClose.setVisibility(View.VISIBLE);
            }
        });
        fabClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lView1.setVisibility(View.VISIBLE);
                lView2.setVisibility(View.GONE);
                fabSearch.setVisibility(View.VISIBLE);
                fabClose.setVisibility(View.GONE);
                initializeVariables();
                if(mProjectObjectList != null && mProjectObjectList.size() != 0 ){
                    mProjectObjectList.clear();
                    mProjectObjectList = null;
                }
                if(mRecyclerView != null){
                    mRecyclerView.removeAllViews();
                }
                if(mtext != null){
                    mtext.setText("");
                }

                hideKeyboard(lView1);
            }
        });

        final Button mGetProductButton = (Button) findViewById(R.id.getProductButton);

        mGetProductButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Editable mString = mtext.getText();
                if(!mString.toString().isEmpty()){
                    if(isNetworkAvailable()){
                        mTextString = mString.toString().trim();
                        mTextString = mTextString.replaceAll(" ", "%20");
                        currentQueryProvider = 0;
                        hideKeyboard(arg0);
                        if(mProjectObjectList != null){
                            mProjectObjectList.clear();
                        }
                        new GetProductTask().execute(mTextString);
                    }
                    else {
                        Toast.makeText(MainActivity.this, R.string.internet_connection, Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, R.string.error_input, Toast.LENGTH_SHORT).show();
                }

            }
        });

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {

                    if(isNetworkAvailable()){
                        String serverURL = mTextString + PAGE_STRING + mPageNumber;
                        new GetProductTask().execute(serverURL);
                    }
                    loading = true;
                    mPageNumber++;
                }
            }
        });

        myRecyclerAdapter = new MyRecyclerAdapter(MainActivity.this, mProjectObjectList, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable() && currentQueryProvider == 0){
                    mCompareProductID = ((TextView)v.findViewById(R.id.productId)).getText().toString();
                    mComparePrice = ((TextView)v.findViewById(R.id.price)).getText().toString();
                    String serverURL = PM6_API_URL + mCompareProductID + PM6_PRODUCT_KEY;
                    new CompareProductTask().execute(serverURL);
                }
                else if (!isNetworkAvailable()){
                    Toast.makeText(MainActivity.this, R.string.internet_connection, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRecyclerView.setAdapter(myRecyclerAdapter);
        myRecyclerAdapter.notifyDataSetChanged();


    }


    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class GetProductTask  extends AsyncTask<String, Void, Void> {

        private String mContent;
        private String mError = null;

        protected void onPreExecute() {

            if(mGetProductDialog != null && mGetProductDialog.isShowing()){
                mGetProductDialog.dismiss();
                mGetProductDialog = null;
            }
            mGetProductDialog = new ProgressDialog(MainActivity.this);
            if(mGetProductDialog != null && !mGetProductDialog.isShowing()){
                String company = null;
                if(currentQueryProvider == 0){
                    company = ZAPPOS;
                }
                else {
                    company = PM6;
                }
                mGetProductDialog.setMessage("Searching for products on " + company);
                mGetProductDialog.setCanceledOnTouchOutside(false);
                mGetProductDialog.show();
            }

        }

        protected Void doInBackground(String... urls) {

            String serverUrl = null;
            if(currentQueryProvider == 0){
                serverUrl = ZAPPOS_API_URL + urls[0] + ZAPPOS_PRODUCT_KEY;
            }
            else {
                serverUrl = PM6_API_URL + urls[0] + PM6_PRODUCT_KEY;
            }

            mContent = requestContent(serverUrl);

            return null;
        }

        protected void onPostExecute(Void unused) {

            if(mGetProductDialog != null && mGetProductDialog.isShowing()){
                mGetProductDialog.dismiss();
            }

            if (mError != null) {
                Log.v("karan", "Error is " + mError);
            }

            if(mContent != null)
            {
                JSONObject jsonResponse;

                try {
                    jsonResponse = new JSONObject(mContent);

                    JSONArray jsonMainNode = jsonResponse.optJSONArray("results");
                    mTotalCount = Integer.parseInt(jsonResponse.getString("totalResultCount"));

                    int lengthJsonArr = jsonMainNode.length();
                    if(lengthJsonArr != 0 && mTotalCount > mProjectObjectList.size()){
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
                    else if(lengthJsonArr == 0 && mTotalCount == mProjectObjectList.size()){
                        Toast.makeText(MainActivity.this, R.string.product_new_not_found, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, R.string.product_not_found, Toast.LENGTH_SHORT).show();
                    }



                } catch (JSONException e) {

                    e.printStackTrace();
                }

                displayList();
            }
            else {
                Toast.makeText(MainActivity.this, R.string.error_getting_result, Toast.LENGTH_SHORT).show();
            }
        }

        private void displayList(){
            myRecyclerAdapter.notifyDataSetChanged();
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
                mCompareProductDialog.setMessage("Checking product on 6PM.COM..");
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
            if(mContent != null)
            {
                JSONObject jsonResponse;

                try {

                    jsonResponse = new JSONObject(mContent);
                    String price = null;
                    float difference = -1;

                    JSONArray jsonMainNode = jsonResponse.optJSONArray("results");

                    int lengthJsonArr = jsonMainNode.length();
                    if(lengthJsonArr != 0){
                        for(int i=0; i < lengthJsonArr; i++)
                        {
                            JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                            String productID = jsonChildNode.optString("productId").toString();
                            price = jsonChildNode.optString("price").toString();

                            difference = Float.parseFloat(mComparePrice.replace("$", "")) - Float.parseFloat(price.replace("$", ""));

                            if(productID.equalsIgnoreCase(mCompareProductID)){
                                productFound = true;
                                break;
                            }
                        }
                    }

                    if(mCompareProductDialog != null && mCompareProductDialog.isShowing()){
                        mCompareProductDialog.dismiss();
                    }

                    if(productFound){
                        showDifferenceDialog(difference);
                    }
                    else {
                        Toast.makeText(MainActivity.this, R.string.no_product_6pm, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
            else {

                if(mCompareProductDialog != null && mCompareProductDialog.isShowing()){
                    mCompareProductDialog.dismiss();
                }

                Toast.makeText(MainActivity.this, R.string.error_6pm, Toast.LENGTH_SHORT).show();
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

    public void showDifferenceDialog(float difference){
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        if(difference > 0 ){
            mDialogBuilder.setMessage("Product found on 6PM is cheaper by $" + difference +"! Do you want to search at 6pm ?");
        }
        else {
            mDialogBuilder.setMessage("Product found on 6PM is has a similar price! Do you want to search at 6pm ?");
        }
        mDialogBuilder.setCancelable(true);
        mDialogBuilder.setPositiveButton(
                R.string.string_yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(isNetworkAvailable()){
                            if(mProjectObjectList != null){
                                mProjectObjectList.clear();
                            }
                            currentQueryProvider = 1;
                            initializeVariables();
                            new GetProductTask().execute(mTextString);
                        }
                        dialog.dismiss();
                    }
                });

        mDialogBuilder.setNegativeButton(
                R.string.string_no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog mDialog = mDialogBuilder.create();
        mDialog.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
