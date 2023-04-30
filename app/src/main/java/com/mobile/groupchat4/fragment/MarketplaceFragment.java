package com.mobile.groupchat4.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.groupchat4.MainActivity;
import com.mobile.groupchat4.ProductPublishActivity;
import com.mobile.groupchat4.R;
import com.mobile.groupchat4.adapters.AdapterProduct;
import com.mobile.groupchat4.adapters.AdapterUsers;
import com.mobile.groupchat4.models.ModelProduct;
import com.mobile.groupchat4.models.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    SearchView searchView;
    RecyclerView products_recycleView;
    Button sellButton;

    Spinner spinner;

    AdapterProduct adapterProduct;
    List<ModelProduct> productList;

    public MarketplaceFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();


        firebaseAuth = FirebaseAuth.getInstance();

        products_recycleView = view.findViewById(R.id.products_recycleView);
        products_recycleView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        products_recycleView.setLayoutManager(layoutManager);

        productList = new ArrayList<>();
        loadProducts();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Spinner Drop down list
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.all_spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        // Set an item selected listener if needed
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Your code to execute when an item is selected
                String productCategory = parent.getItemAtPosition(position).toString();
                loadFilteredProducts(productCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Your code to execute when no item is selected
            }
        });

        // Search View
        searchView = view.findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Update this part
                if (!TextUtils.isEmpty(s.trim())) {
                    searchProducts(s);
                } else {
                    loadProducts();
                }
                return false;
            }
        });


        sellButton = view.findViewById(R.id.sellButton);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProductPublishActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadProducts() {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Product");
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelProduct product = ds.getValue(ModelProduct.class);
                    if(product != null)
                        productList.add(product);
                }

                // Set the adapter
                adapterProduct = new AdapterProduct(getActivity(), productList);
                products_recycleView.setAdapter(adapterProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchProducts(final String query) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Product");
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelProduct product = ds.getValue(ModelProduct.class);
                    if (product != null &&
                            product.getProductTitle().toLowerCase().contains(query.toLowerCase())) {
                        productList.add(product);
                    }
                }

                // Set the adapter
                adapterProduct = new AdapterProduct(getActivity(), productList);
                products_recycleView.setAdapter(adapterProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFilteredProducts(final String productCategory) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Product");
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelProduct product = ds.getValue(ModelProduct.class);
                    if (productCategory.equalsIgnoreCase("All") || (product != null && product.getProductCategory().equalsIgnoreCase(productCategory))) {
                        productList.add(product);
                    }
                }

                // Set the adapter
                adapterProduct = new AdapterProduct(getActivity(), productList);
                products_recycleView.setAdapter(adapterProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        }
        else {
            //user not signed in, go to main Activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu options in fragment


        super.onCreate(savedInstanceState);
    }

    //    inflate options menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);



        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(id == R.id.action_profile){
                // home fragment transaction
                ProfileFragment fragment2 = new ProfileFragment();
                FragmentTransaction ft2 = getParentFragmentManager().beginTransaction();
                ft2.replace(R.id.content, fragment2, "");
                ft2.commit();
                return true;
        }
        else if(id == R.id.action_profile){
            // home fragment transaction
            actionBar.setTitle("Profile");
            ProfileFragment fragment2 = new ProfileFragment();
            FragmentTransaction ft2 = getParentFragmentManager().beginTransaction();
            ft2.replace(R.id.content, fragment2, "");
            ft2.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}