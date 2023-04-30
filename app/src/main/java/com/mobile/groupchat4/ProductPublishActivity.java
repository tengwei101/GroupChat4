package com.mobile.groupchat4;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Locale;

public class ProductPublishActivity extends AppCompatActivity {

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //picked image uri
    private Uri image_uri = null;

    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;

    //UI VIews
    private TextView cancelTv, publishTv;
    private ImageView productIv;
    private EditText titleEt, priceEt, sizeEt, descriptionEt;
    private Spinner categorySpinner, conditionSpinner;

    private ProgressDialog progressDialog;

    private String productCategory;
    private String productCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_publish);

        actionBar = getSupportActionBar();

        actionBar.setTitle("Sell Product");

        // init UI Views
        cancelTv = findViewById(R.id.cancelTv);
        publishTv = findViewById(R.id.publishTv);
        productIv = findViewById(R.id.productIv);
        titleEt = findViewById(R.id.titleEt);
        priceEt = findViewById(R.id.priceEt);
        sizeEt = findViewById(R.id.sizeEt);
        descriptionEt = findViewById(R.id.descriptionEt);

        firebaseAuth = FirebaseAuth.getInstance();

        initializeCategorySpinner();
        initializeConditionSpinner();

        //pick image
        productIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        // handle click event
        publishTv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                startPublishingProduct();
            }
        });

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private boolean isValidPriceFormat(String productPrice) {
        // Regular expression to match the format 0.00
        String regex = "^\\d+(\\.\\d{2})?$";
        return productPrice.matches(regex);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startPublishingProduct() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Publishing Product");

        String productTitle = titleEt.getText().toString().trim();
        String productPriceRaw = priceEt.getText().toString().trim();
        String productSize = sizeEt.getText().toString().trim();
        String productDescription = descriptionEt.getText().toString().trim();


        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Please enter product title...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }

        if(TextUtils.isEmpty(productPriceRaw)){
            Toast.makeText(this, "Please enter product price...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }
        
        if(!isValidPriceFormat(productPriceRaw)){
            Toast.makeText(this, "Please enter a valid price format (0.00)...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(productSize)){
            Toast.makeText(this, "Please enter product size...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }
        

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));
        String productPrice;
        try {
            double priceValue = Double.parseDouble(productPriceRaw);
            productPrice = currencyFormat.format(priceValue);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        //timestamp: group icon image, groupid, timeCreated etc
        String p_timeStamp = ""+System.currentTimeMillis();

        if(image_uri == null){
            //creating group without icon image
            Toast.makeText(this, "Please insert product image...", Toast.LENGTH_SHORT).show();
            return; //dont proceed further
        }

        progressDialog.show();

        String fileNameAndPath = "Product-Imags/" + "image" + firebaseAuth.getUid()+"_"+p_timeStamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!p_uriTask.isSuccessful());
                        Uri p_downloadUri = p_uriTask.getResult();
                        if(p_uriTask.isSuccessful()){
                            publishProduct(""+productTitle, ""+productPrice, ""+productCategory, ""+productSize, ""+productCondition,
                                    ""+productDescription, ""+p_downloadUri, ""+p_timeStamp);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductPublishActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Add this method to initialize the category spinner
    private void initializeCategorySpinner() {
        //Category Spinner
        categorySpinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set an item selected listener if needed
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item as a string
                String selectedItem = parent.getItemAtPosition(position).toString();

                // Set the productCategory variable to the selected item
                productCategory = selectedItem;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // Add this method to initialize the category spinner
    private void initializeConditionSpinner() {
        //Condition Spinner
        conditionSpinner = findViewById(R.id.conditionSpinner);
        ArrayAdapter<CharSequence> condition_adapter = ArrayAdapter.createFromResource(this,
                R.array.condition_spinner_items, R.layout.condition_spinner_item);
        condition_adapter.setDropDownViewResource(R.layout.condition_spinner_item);
        conditionSpinner.setAdapter(condition_adapter);

        // Set an item selected listener if needed
        conditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item as a string
                String selectedItem = parent.getItemAtPosition(position).toString();

                // Set the productCategory variable to the selected item
                productCondition = selectedItem;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Your code to execute when no item is selected
            }
        });
    }

    private void publishProduct(String p_title, String p_price, String p_category, String p_size,
                                String p_condition, String p_description, String p_image, String p_timestamp){
        String userID = firebaseAuth.getUid();
        String p_id = userID + "_" + p_timestamp;
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("productID", ""+p_id);
        hashMap.put("productTitle", ""+p_title);
        hashMap.put("productPrice", ""+p_price);
        hashMap.put("productCategory", ""+p_category);
        hashMap.put("productSize", ""+p_size);
        hashMap.put("productCondition", ""+p_condition);
        hashMap.put("productDescription", p_description);
        hashMap.put("timestamp", ""+p_timestamp);
        hashMap.put("productImage", ""+p_image);
        hashMap.put("sellerID", ""+userID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Product");
        ref.child(p_id).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //created successfully
                        progressDialog.dismiss();
                        Toast.makeText(ProductPublishActivity.this, "Product publishing...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProductPublishActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void showImagePickDialog() {
        //options to pick image from
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            //camera clicked
                            if(!checkCameraPermission()){
                                requestCameraPermission();
                            }
                            else{
                                pickFromCamera();
                            }
                        }
                        else{
                            //galery clicked
                            if(!checkStoragePermission()){
                                requestStoragePermission();
                            }
                            else{
                                pickFromGallery();
                            }
                        }
                    }
                }).show();

    }

    private void pickFromGallery(){
        // Use the Storage Access Framework to pick an image from the gallery
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Product Image Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Product Image Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else{
                        //both or one is denied
                        Toast.makeText(this, "Camera & Storage permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length >0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission allowed
                        pickFromGallery();
                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Storage permission are required", Toast.LENGTH_SHORT).show();

                    }
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //was picked form gallery
                image_uri = data.getData();

                //set to imageView
                productIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //was picked form camera


                //set to imageView
                productIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}