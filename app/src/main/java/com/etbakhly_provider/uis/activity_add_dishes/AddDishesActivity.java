package com.etbakhly_provider.uis.activity_add_dishes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etbakhly_provider.R;
import com.etbakhly_provider.adapter.SpinnerDishCategoryAdapter;
import com.etbakhly_provider.databinding.ActivityAddDishesBinding;
import com.etbakhly_provider.model.AddDishModel;
import com.etbakhly_provider.model.BuffetModel;
import com.etbakhly_provider.model.CategoryDishModel;
import com.etbakhly_provider.model.UserModel;
import com.etbakhly_provider.mvvm.ActivityAddDishMvvm;
import com.etbakhly_provider.share.Common;
import com.etbakhly_provider.uis.activity_add_buffet.AddBuffetActivity;
import com.etbakhly_provider.uis.activity_base.BaseActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddDishesActivity extends BaseActivity {
    private ActivityAddDishesBinding binding;
    private ActivityAddDishMvvm mvvm;
    private AddDishModel addDishModel;
    private SpinnerDishCategoryAdapter spinnerDishCategoryAdapter;
    private List<BuffetModel.Category> categoryList;
    private ActivityResultLauncher<Intent> launcher;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private int selectedReq = 0;
    private Uri uri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_dishes);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        List<BuffetModel.Category> categories = (List<BuffetModel.Category>) getIntent().getSerializableExtra("data");
        if (categories != null) {
            categoryList = new ArrayList<>(categories);

        } else {
            categoryList = new ArrayList<>();
        }
    }

    private void initView() {
        addDishModel = new AddDishModel();
        binding.setModel(addDishModel);
        mvvm = ViewModelProviders.of(this).get(ActivityAddDishMvvm.class);

        spinnerDishCategoryAdapter = new SpinnerDishCategoryAdapter(categoryList, this);
        binding.spinner.setAdapter(spinnerDishCategoryAdapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                addDishModel.setCategory_dishes_id(categoryList.get(i).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mvvm.getAddDishLiveData().observe(this, aBoolean -> {
            if (aBoolean) {
                Toast.makeText(AddDishesActivity.this, getResources().getString(R.string.succ), Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        binding.btnDone.setOnClickListener(view -> {
            if (addDishModel.isDataValid(this)) {
                addDishModel.setCaterer_id(getUserModel().getData().getCaterer().getId());
                mvvm.storeDish(this, addDishModel, uri);
            }
        });
        binding.setLang(getLang());

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (selectedReq == READ_REQ) {

                    uri = result.getData().getData();
                    addDishModel.setPhoto(uri.toString());
                    File file = new File(Common.getImagePath(this, uri));
                    Picasso.get().load(file).fit().into(binding.image);
                    binding.icon.setVisibility(View.GONE);

                } else if (selectedReq == CAMERA_REQ) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    uri = getUriFromBitmap(bitmap);
                    if (uri != null) {
                        addDishModel.setPhoto(uri.toString());

                        String path = Common.getImagePath(this, uri);
                        if (path != null) {
                            Picasso.get().load(new File(path)).fit().into(binding.image);

                        } else {
                            Picasso.get().load(uri).fit().into(binding.image);

                        }
                        binding.icon.setVisibility(View.GONE);
                    }
                }
            }
        });

        binding.flGallery.setOnClickListener(view -> {
            closeSheet();
            checkReadPermission();
        });
        binding.flCamera.setOnClickListener(view -> {
            closeSheet();
            checkCameraPermission();
        });

        binding.btnCancel.setOnClickListener(view -> closeSheet());

        binding.image.setOnClickListener(view -> openSheet());


        binding.llBack.setOnClickListener(view -> finish());
    }

    public void checkCameraPermission() {

        closeSheet();

        if (ContextCompat.checkSelfPermission(this, write_permission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, camera_permission) == PackageManager.PERMISSION_GRANTED
        ) {
            SelectImage(CAMERA_REQ);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{camera_permission, write_permission}, CAMERA_REQ);
        }
    }

    private void SelectImage(int req) {
        selectedReq = req;
        Intent intent = new Intent();

        if (req == READ_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);

            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");

            launcher.launch(intent);

        } else if (req == CAMERA_REQ) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                launcher.launch(intent);
            } catch (SecurityException e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();

            }


        }
    }

    public void openSheet() {
        binding.expandLayout.setExpanded(true, true);
    }

    public void closeSheet() {
        binding.expandLayout.collapse(true);

    }

    public void checkReadPermission() {
        closeSheet();
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, READ_REQ);
        } else {
            SelectImage(READ_REQ);
        }
    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }

}