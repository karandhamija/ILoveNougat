package app.zappos.karandhamija.ilovenougat;

import android.content.Context;
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
import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private RecyclerView recyclerView;
    ArrayList<ProductObject> mProjectObjectList = new ArrayList<ProductObject>();
    private MyRecyclerAdapter myRecyclerAdapter;

    public static final String ZAPPOS_API_URL= "https://api.zappos.com/Search?term=";

    public static final String ZAPPOS_PRODUCT_KEY="&key=b743e26728e16b81da139182bb2094357c31d331";

    public static final String AM6_API_URL= "https://api.6pm.com/Search?term=";

    public static final String AM6_PRODUCT_KEY="&key=524f01b7e2906210f7bb61dcbe1bfea26eb722eb";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        final Button GetServerData = (Button) findViewById(R.id.GetServerData);

        GetServerData.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                EditText mtext = (EditText) findViewById(R.id.serverText);
                Editable mString = mtext.getText();
                if(!mString.toString().isEmpty()){
                    String mTextString = mString.toString().trim();
                    String serverURL = ZAPPOS_API_URL + mTextString + ZAPPOS_PRODUCT_KEY;
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
                    new LongOperation().execute(serverURL);
                }
                else {
                    Toast.makeText(MainActivity.this, "Please input the text correctly", Toast.LENGTH_LONG).show();
                }

            }
        });

    }


    private class LongOperation  extends AsyncTask<String, Void, Void> {

        private String mContent;
        private String mError = null;
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);


        protected void onPreExecute() {

            Dialog.setMessage("Please wait..");
            Dialog.show();

        }

        protected Void doInBackground(String... urls) {

            mContent = requestContent(urls[0]);

            return null;
        }

        protected void onPostExecute(Void unused) {

            if(mProjectObjectList != null){
                mProjectObjectList.clear();
            }

            Dialog.dismiss();

            if (mError != null) {


            } else {

                JSONObject jsonResponse;

                try {

                    jsonResponse = new JSONObject(mContent);

                    JSONArray jsonMainNode = jsonResponse.optJSONArray("results");

                    int lengthJsonArr = jsonMainNode.length();
                    Toast.makeText(MainActivity.this, "Result count is " + lengthJsonArr, Toast.LENGTH_LONG).show();

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

                } catch (JSONException e) {

                    e.printStackTrace();
                }
            displayList();

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
                    TextView mTextView = (TextView) v.findViewById(R.id.productUrl);
                    System.out.println("Name is " + mTextView.getText());
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

}
