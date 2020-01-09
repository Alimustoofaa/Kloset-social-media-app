package com.alimustofa.kloset;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.effect.Effect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebases
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //stoarge
    StorageReference storageReference;

    //path where images of user profile and cover will be stored
    String storagePath="User_Profile_Cover_Images/";


    //view from xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;

    //proggres dialog
    ProgressDialog pd;

    //permission contants
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;
    private static final int IMAGE_PICK_GALLERY_CODE =300;
    private static final int IMAGE_PICK_CAMERA_CODE =400;

    //array permission contans
    String cameraPermission[];
    String storagePermisson[];


    Uri image_uri;

    //for checking profile or cover photo
    String profileorCoverPhoto;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        //init array of permission
        cameraPermission= new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        coverIv = view.findViewById(R.id.coverIv);
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        Query query= databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required data get
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    String email = ""+ds.child("email").getValue();
                    String phone = ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //iff image is received then set
                        Picasso.get().load(image).into(avatarIv);

                    }catch (Exception e){
                        //if there is any exception while getting image then set dafault
                        Picasso.get().load(R.drawable.ic_dafault_img_white).into(avatarIv);
                    }
                    try {
                        //iff image is received then set
                        Picasso.get().load(cover).into(coverIv);

                    }catch (Exception e){
                        //if there is any exception while getting image then set dafault
                        Picasso.get().load(R.drawable.ic_add_image).into(coverIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogEditProfile();
            }
        });
        return view;
    }

    //storage permission
    private boolean checkStoragePermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
       return result ;
    }

    private void requestStoragePermission(){
       requestPermissions(storagePermisson, STORAGE_REQUEST_CODE);
    }

    //camera permission
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void showDialogEditProfile() {
        /*show dialog contaimimg options
            --Edit Profile:
            1) Name
            2) Phone
            3) Profile Picture
            4) Cover Photo

         */
        //optioon to show in dialog
        String option[] = {"Edit Profile Picture","Edit Cover Photo","Edit Name","edit Phone"};

        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set tittle
        builder.setTitle("Chose Action");
        //set items to dialog
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if(which == 0){
                    //edit profile clicked
                    pd.setMessage("Updating Profile Picture");
                    profileorCoverPhoto = "image";
                    showImagePicDialog();
                }else if(which == 1){
                    //edit cover clicked
                    pd.setMessage("Updating Cover Photo");
                    profileorCoverPhoto ="cover";
                    showImagePicDialog();

                }else  if(which == 2){
                    //edit name clicked
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");

                }else  if(which == 3){
                    //edit phone clicked
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {

        //custom dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);

        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object>result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(getActivity(), "Please Enter "+key, Toast.LENGTH_SHORT).show();

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {

        String option[] = {"Camera","Gallery"};

        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set tittle
        builder.setTitle("Pict Image From");
        //set items to dialog
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if(which == 0){
                    //Camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }

                }else if(which == 1){
                    //Gallery clicked
                if (!checkStoragePermission()){
                    requestStoragePermission();
                }else {
                    pickFromGallery();
                }

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        //permission enabled
                        pickFromCamera();
                    }
                    else {
                        ///permission disabled
                        Toast.makeText(getActivity(), "Please enable camera permission and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }case STORAGE_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        //permission enabled
                        pickFromGallery();
                    }
                    else {
                        ///permission disabled
                        Toast.makeText(getActivity(), "Please enable gallery permission ", Toast.LENGTH_SHORT).show();
                    }

            }
        }
        break;

    }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            if(resultCode == RESULT_OK){
                if (requestCode == IMAGE_PICK_GALLERY_CODE){
                    // image is picked from gallery, get uri of image
                    image_uri = data.getData();
                    uploadProfileCoverPhoto(image_uri);
                }if (requestCode == IMAGE_PICK_CAMERA_CODE){
                    // image is picked from camera, get uri of image
                    uploadProfileCoverPhoto(image_uri);

                }
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {
        pd.show();
        //path and name of image to be storga in firebase
        String filePathandName = storagePath+""+ profileorCoverPhoto+"_"+user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathandName);
        storageReference2nd.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri>uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not
                        if (uriTask.isSuccessful()){
                            //image uploaded
                            //add/update uri in users database
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(profileorCoverPhoto, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated..", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraInten = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraInten.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraInten, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        //pict from gallery
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery, IMAGE_PICK_GALLERY_CODE );
    }
    }


