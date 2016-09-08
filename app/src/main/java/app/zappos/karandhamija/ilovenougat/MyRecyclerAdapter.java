package app.zappos.karandhamija.ilovenougat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder> {
    private ArrayList<ProductObject> testItemList;

    Context mContext = null;
    private final View.OnClickListener listener;

    public MyRecyclerAdapter(Context mContext, ArrayList<ProductObject> testItemList, View.OnClickListener listener) {
        this.mContext = mContext;
        this.testItemList = testItemList;
        this.listener = listener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        Log.d("karan","Inside the onCreatViewHolder");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_list, null);

        view.setOnClickListener(listener);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {

        ProductObject testItem = testItemList.get(i);

        //Setting text view title
        customViewHolder.brandName.setText(testItem.getBrandName());
        customViewHolder.productId.setText(testItem.getProductId());
        customViewHolder.originalPrice.setText(testItem.getOriginalPrice());
        customViewHolder.styleId.setText(testItem.getStyleId());
        customViewHolder.colorId.setText(testItem.getColorId());
        customViewHolder.price.setText(testItem.getPrice());
        customViewHolder.percentOff.setText(testItem.getPercentOff());
        customViewHolder.productUrl.setText(testItem.getProductUrl());
        customViewHolder.productName.setText(testItem.getProductName());

        Picasso.with(mContext).load(testItem.getThumbnailImageUrl()).into(customViewHolder.thumbnailImageUrl);
    }

    @Override
    public int getItemCount() {

        return (null != testItemList ? testItemList.size() : 0);
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected TextView brandName;
        protected ImageView thumbnailImageUrl;
        protected TextView productId;
        protected TextView originalPrice;
        protected TextView styleId;
        protected TextView colorId;
        protected TextView price;
        protected TextView percentOff;
        protected TextView productUrl;
        protected TextView productName;

        public CustomViewHolder(View view) {
            super(view);

            this.brandName = (TextView) view.findViewById(R.id.brandName);
            this.thumbnailImageUrl = (ImageView) view.findViewById(R.id.thumbnailImageUrl);
            this.productId = (TextView) view.findViewById(R.id.productId);
            this.originalPrice = (TextView) view.findViewById(R.id.originalPrice);
            this.styleId = (TextView) view.findViewById(R.id.styleId);
            this.colorId = (TextView) view.findViewById(R.id.colorId);
            this.price = (TextView) view.findViewById(R.id.price);
            this.percentOff = (TextView) view.findViewById(R.id.percentOff);
            this.productUrl = (TextView) view.findViewById(R.id.productUrl);
            this.productName = (TextView) view.findViewById(R.id.productName);
        }
    }
}
