package com.mobile.groupchat4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.groupchat4.models.ModelProduct;
import com.mobile.groupchat4.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ProductDescriptionActivity extends AppCompatActivity {

    private ActionBar actionBar;

    DatabaseReference productDatabase;

    FirebaseAuth firebaseAuth;

    String myUid, productID, productTitle, sellerID, sellerPhone, category, price, size, sellerName;

    ImageView productIv, sellerIv;
    TextView productTitleTv, productDescriptionTv, priceTv, categoryTv, sizeTv, conditionTv;
    TextView waterWasteTv, lightWasteTv, emissionWasteTv;
    TextView nameTv, emailTv, phoneTv;
    ImageButton favorite_button;
    ImageView sendWhatsappIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_description);

        productDatabase = FirebaseDatabase.getInstance().getReference();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(productTitle);
        actionBar.hide();

        productIv = findViewById(R.id.productIv);
        productTitleTv = findViewById(R.id.productTitleTv);
        productDescriptionTv = findViewById(R.id.productDescriptionTv);
        priceTv = findViewById(R.id.priceTv);
        categoryTv = findViewById(R.id.categoryTv);
        sizeTv = findViewById(R.id.sizeTv);
        conditionTv = findViewById(R.id.conditionTv);
        waterWasteTv = findViewById(R.id.waterWasteTv);
        lightWasteTv = findViewById(R.id.lightWasteTv);
        emissionWasteTv = findViewById(R.id.emissionWasteTv);
        favorite_button = findViewById(R.id.favorite_button);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        sellerIv = findViewById(R.id.sellerIv);
        sendWhatsappIv = findViewById(R.id.sendWhatsappIv);

        sendWhatsappIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWhatsAppMessage(sellerPhone);
            }
        });


        //get id of the group
        Intent intent = getIntent();
        productID = intent.getStringExtra("productID");

        loadProductData(productID);

    }

    private void loadProductData(String productID) {
        productDatabase.child("Product").child(productID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelProduct product = dataSnapshot.getValue(ModelProduct.class);

                if (product != null) {
                    productTitleTv.setText(product.getProductTitle());
                    productTitle = product.getProductTitle();
                    productDescriptionTv.setText(product.getProductDescription());
                    priceTv.setText(product.getProductPrice());
                    price = product.getProductPrice();
                    categoryTv.setText("CATEGORY - " + product.getProductCategory());
                    category = product.getProductCategory();
                    sizeTv.setText("SIZE - " + product.getProductSize());
                    size = product.getProductSize();
                    conditionTv.setText("CONDITION - " + product.getProductCondition());
                    sellerID = product.getSellerID();
                    loadSeller(sellerID);
                    // Load the product image
                    try {
                        Picasso.get().load(product.getProductImage()).placeholder(R.drawable.ic_product_image_black).into(productIv);
                    } catch (Exception e) {
                        productIv.setImageResource(R.drawable.ic_product_image_black);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(ProductDescriptionActivity.this, "Failed to load product data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSeller(String sellerID) {
        productDatabase.child("Users").child(sellerID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser seller = dataSnapshot.getValue(ModelUser.class);

                if (seller != null) {
                    // Set seller information to the corresponding TextViews
                    nameTv.setText(seller.getName());
                    sellerName = seller.getName();
                    emailTv.setText(seller.getEmail());
                    phoneTv.setText(seller.getPhone());
                    sellerPhone = seller.getPhone();

                    // You can also load the seller's image using an image loading library like Picasso or Glide
                    // Example with Picasso:
                     String sellerImage = seller.getImage();
                     try {
                         Picasso.get().load(sellerImage).placeholder(R.drawable.ic_default_img).into(sellerIv);
                     } catch (Exception e) {
                         sellerIv.setImageResource(R.drawable.ic_default_img);
                     }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductDescriptionActivity.this, "Failed to load seller data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendWhatsAppMessage(String phoneNumber) {
        try {
            if(phoneNumber.equals("")){
                Toast.makeText(ProductDescriptionActivity.this, "Missing Seller Phone Number...", Toast.LENGTH_SHORT).show();
            }
            String message = "Hello! I am interested in your product on the SeCloth app.\n\n" +
                    "Product Title: " + productTitle + "\n" +
                    "Price: " + price + "\n" +
                    "Category: " + category + "\n" +
                    "Size: " + size + "\n" +
                    "Seller: " + sellerName;
            String encodedMessage = URLEncoder.encode(message, "UTF-8");
            String whatsappUrl = "https://api.whatsapp.com/send?phone=" + "+6" + phoneNumber + "&text=" + encodedMessage;

            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl));
            startActivity(whatsappIntent);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(ProductDescriptionActivity.this, "Error encoding message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ProductDescriptionActivity.this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_profile).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);


        return super.onCreateOptionsMenu(menu);
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){

            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();


            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

            myUid = user.getUid(); //current user id

        }
        else {
            //user not signed in, go to main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }


}