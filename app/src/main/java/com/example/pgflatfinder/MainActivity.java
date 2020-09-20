package com.example.pgflatfinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private BubbleNavigationLinearView bubbleNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bubbleNavigation = findViewById(R.id.bottom_navigation_view);
        bubbleNavigation.setCurrentActiveItem(R.id.home);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
        bottomMenu();

    }

    private void bottomMenu() {

        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {

                Fragment fragment = null;

                if(position == 0)
                {
                    fragment = new HomeFragment();
                }
                else if(position == 1)
                {
                    fragment = new FavoritesFragment();
                }
                else if(position == 2)
                {
                    fragment = new ProfileFragment();
                }
                else
                {
                    fragment = new HomeFragment();
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null)
        {
            Intent intent = new Intent(MainActivity.this,StartScreen.class);
            startActivity(intent);
            finish();
        }
    }
}