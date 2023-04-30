package com.mobile.groupchat4.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.groupchat4.GroupChatActivity;
import com.mobile.groupchat4.ProductDescriptionActivity;
import com.mobile.groupchat4.models.ModelProduct;
import com.mobile.groupchat4.R;
import com.squareup.picasso.Picasso;


import java.util.List;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.ProductViewHolder> {

    Context context;
    List<ModelProduct> productList;

    public AdapterProduct(Context context, List<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ModelProduct product = productList.get(position);

        holder.productTitleTv.setText(product.getProductTitle());
        holder.sizeTv.setText(product.getProductSize());
        holder.conditionTv.setText(product.getProductCondition());
        holder.priceTv.setText(product.getProductPrice());
        String productImage = product.getProductImage();


        try{
            Picasso.get().load(productImage).placeholder(R.drawable.ic_product_image_black).into(holder.productImageIv);

        }catch(Exception e){
            holder.productImageIv.setImageResource(R.drawable.ic_product_image_black);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProductDescriptionActivity.class);
                intent.putExtra("productID", product.getProductID());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return productList.size();
    }


    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView productImageIv;
        TextView productTitleTv, sizeTv, conditionTv, priceTv;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            productImageIv = itemView.findViewById(R.id.productImageIv);
            productTitleTv = itemView.findViewById(R.id.productTitleTv);
            sizeTv = itemView.findViewById(R.id.sizeTv);
            conditionTv = itemView.findViewById(R.id.conditionTv);
            priceTv = itemView.findViewById(R.id.priceTv);
        }
    }
}
