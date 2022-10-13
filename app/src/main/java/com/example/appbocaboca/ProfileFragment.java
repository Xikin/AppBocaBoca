package com.example.appbocaboca;

import static android.app.Activity.RESULT_OK;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
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

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {


    //FIREBASEAUTH
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;

    //path
    String storagePath = "Users_Profile_Cover_Imgs/";

    // Componentes views xml
    ImageView avatar, coverIv;
    TextView nameT, emailT, telefoneT;
    FloatingActionButton fab;

    //progress dialog
    ProgressDialog pd;
    // permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //ARRAYS OF PERMISSIONS TO BE REQUESTED
    String cameraPermissions[];
    String storagePermissions[];
    // uri of picked image
    Uri image_uri;

    //
    String profileOrCoverPhoto;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */

    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //iniciar Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Iniciar as views
        avatar = view.findViewById(R.id.avataricon);
        coverIv = view.findViewById(R.id.coverIv);
        emailT = view.findViewById(R.id.emailTv);
        telefoneT = view.findViewById(R.id.telefoneTv);
        nameT = view.findViewById(R.id.nameTv);
        fab = view.findViewById(R.id.fab);

        //initial progress dialog
        pd = new ProgressDialog(getActivity());



        /*Nos temos que conseguir informações do usuario logado.
         * Conseguimos isso usando o email ou o uid, aqui nos usaremos o email
         * Usando a query orderByChild nos vamos mostrar os detalhes de um nó o
         * qual a chave chamada email tem valor igual ao email logado.
         * Vai procurar todos os nós , onde a chave combina combina vai pegar os detalhes*/

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //checar até obter os dados necessários
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String nome = "" + ds.child("nome").getValue();
                    String email = "" + ds.child("email").getValue();
                    String telefone = "" + ds.child("telefone").getValue();
                    String imagem = "" + ds.child("imagem").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    //set data

                    nameT.setText("nome");
                    emailT.setText("email");
                    telefoneT.setText("telefone");
                    try {
                        //Se a imagem for recebida entao mudar
                        Picasso.get().load(imagem).into(avatar);

                    } catch (Exception e) {

                        //Se tiver uma exception enquanto conseguir a  imagem entao definir uma imagem default
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatar);

                    }
                    try {
                        //Se a imagem for recebida entao mudar
                        Picasso.get().load(cover).into(coverIv);

                    } catch (Exception e) {

                        //Se tiver uma exception enquanto conseguir a  imagem entao definir uma imagem default

                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // fab button click "Botao flutuante nao mexa nessa caralha"
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        requestPermissions( storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkcCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
       requestPermissions( cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Editar foto do Perfil", "Editar foto da capa", "Editar Nome", "Editar Telefone"};
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Escolher ação");
        // set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicls
                if (which == 0) {
                    // Edit profile clicked
                    pd.setMessage("Atualizar foto do perfil");
                    profileOrCoverPhoto = "imagem";
                    showImagePicDialog();

                } else if (which == 1) {
                    // Edit Cover Clicked
                    pd.setMessage("Atualizar foto da capa");
                    profileOrCoverPhoto ="cover";
                    showImagePicDialog();

                } else if (which == 2) {
                    //Edit name Clicked
                    pd.setMessage("Atualizar Nome");
                    showNamePhoneUpdateDialog ("nome");
                } else if (which == 3) {
                    //Edit phone clicked
                    pd.setMessage("Atualizar Número do Telefone");
                    showNamePhoneUpdateDialog ("telefone");
                }

            }
        });
        // create and show dialog
        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(String key) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Update  "+ key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter"+ key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            String value = editText.getText().toString().trim();

            if (!TextUtils.isEmpty(value)){
                pd.show();
                HashMap<String, Object> result = new HashMap<>();
                result.put(key,value);

                databaseReference.child(user.getUid()).updateChildren(result)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                pd.dismiss();
                                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

            }
            else{
                Toast.makeText(getActivity(), "Please Enter"+key, Toast.LENGTH_SHORT).show();
            }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {



            }
        });

        builder.create().show();



    }

    private void showImagePicDialog() {
        //  show dialog containing options camera and gallery to pick the image
        String options[] = {"Camera", "Galeria",};
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Escolha a Imagem");
        // set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item clicls
                if (which == 0) {
                    // Camera Clicked

                    if (!checkcCameraPermission()) {
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    // Gallery Clicked
                        if (!checkStoragePermission()){
                            requestStoragePermission();
                        }
                        else{
                            pickFromGallery();
                        }

                }

            }
        });
        // create and show dialog
        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeAccepted) {
                        pickFromCamera();
                    }

                } else {
                    Toast.makeText(getActivity(), "Ative a Permissão de Armazenamento da Câmera", Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeAccepted) {
                        pickFromGallery();
                    }

                } else {
                    Toast.makeText(getActivity(), "Ative a Permissão de Armazenamento ", Toast.LENGTH_SHORT).show();
                }

            }
            break;


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){

            if (requestCode == IMAGE_PICK_GALLERY_CODE){

                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){

                uploadProfileCoverPhoto(image_uri);

            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
    pd.show();





    String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+ user.getUid();

    StorageReference storageReference2nd = storageReference.child(filePathAndName);
    storageReference2nd.putFile(uri)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                   Uri downloadUri = uriTask.getResult();
                //check if image is ulpoaded or not
                    if (uriTask.isSuccessful()){

                        HashMap<String,Object> results = new HashMap<>();
                        results.put(profileOrCoverPhoto,downloadUri.toString());
                        databaseReference.child(user.getUid()).updateChildren(results)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Carregando Imagem", Toast.LENGTH_SHORT).show();
                                    }
                                })

                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(getActivity(), "Essa foto e tão feia que deu erro", Toast.LENGTH_SHORT).show();

                                        pd.dismiss();
                                    }
                                });

                    }
                    else {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Deu algum erro. Mas não xingue a mãe do programador", Toast.LENGTH_SHORT).show();
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

    private void pickFromGallery() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Foto temporária");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descrição temporária");
        //
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromCamera() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("imagem/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }






    // chave da classe abaixo
}