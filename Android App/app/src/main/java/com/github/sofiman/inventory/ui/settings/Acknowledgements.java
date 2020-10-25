package com.github.sofiman.inventory.ui.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.sofiman.inventory.R;
import com.github.sofiman.inventory.utils.LayoutHelper;

public class Acknowledgements extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acknowledgements);

        LayoutHelper.addStatusBarOffset(this, findViewById(R.id.acknowledgement));
        findViewById(R.id.acknowledgements_back).setOnClickListener(v -> supportFinishAfterTransition());
    }
}