package com.etbakhly_provider.uis.activities_home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.PagerAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.etbakhly_provider.R;
import com.etbakhly_provider.adapter.HomePagerAdapter;
import com.etbakhly_provider.databinding.ActivityHomeBinding;
import com.etbakhly_provider.databinding.DrawerHeaderBinding;
import com.etbakhly_provider.model.UserSettingsModel;
import com.etbakhly_provider.mvvm.ActivityHomeGeneralMvvm;
import com.etbakhly_provider.uis.activities_home.fragments.FragmentNewOrders;
import com.etbakhly_provider.uis.activities_home.fragments.FragmentCompletedOrders;
import com.etbakhly_provider.uis.activities_home.fragments.FragmentPendingOrders;
import com.etbakhly_provider.uis.activity_base.BaseActivity;
import com.etbakhly_provider.uis.activity_login.LoginActivity;
import com.etbakhly_provider.uis.activity_setting.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class HomeActivity extends BaseActivity {
    private ActivityHomeBinding binding;
    private NavController navController;
    private ActivityHomeGeneralMvvm activityHomeGeneralMvvm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();

    }


    private void initView() {
        activityHomeGeneralMvvm = ViewModelProviders.of(this).get(ActivityHomeGeneralMvvm.class);
        activityHomeGeneralMvvm.onLoggedOutSuccess().observe(this, isLoggedOut -> {
            if (isLoggedOut) {

                clearUserData();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        activityHomeGeneralMvvm.onTokenSuccess().observe(this, this::setUserModel);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.setModel(getUserModel());
        binding.setLang(getLang());
        DrawerHeaderBinding drawerHeaderBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.drawer_header, null, false);
        drawerHeaderBinding.setModel(getUserModel());
        binding.navView.addHeaderView(drawerHeaderBinding.getRoot());

        navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerView);
        binding.llLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityHomeGeneralMvvm.logout(getUserModel(), HomeActivity.this);
            }
        });
        activityHomeGeneralMvvm.updateToken(getUserModel());

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, binding.drawerView);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerView.isDrawerOpen(GravityCompat.START)) {
            binding.drawerView.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

        }
    }
}