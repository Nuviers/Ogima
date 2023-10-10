package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.ogima.R;
import com.example.ogima.fragment.FaqFragment;
import com.example.ogima.fragment.GifPostFragment;
import com.example.ogima.fragment.PhotoPostFragment;
import com.example.ogima.fragment.TextPostFragment;
import com.example.ogima.fragment.VideoPostFragment;
import com.example.ogima.helper.LockedViewPager;
import com.example.ogima.helper.UsuarioUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ConfigurePostActivity extends AppCompatActivity {

    private String idUsuario = "";
    private LockedViewPager viewpager;
    private FragmentPagerItemAdapter fragmentPager;
    private Boolean retornarAoItem = false;
    private int itemAtual;
    private String tipoMidia = "";
    private String urlGif = "";
    private String descricao = "";
    private Uri uriRecuperada = null;

    @Override
    protected void onStart() {
        super.onStart();
        //Remove possíveis fragment deixados em segundo plano.
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        retornarAoItem = true;
        itemAtual = viewpager.getCurrentItem();
    }

    public ConfigurePostActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_post);
        inicializarComponentes();
        configBundle();
        clickListeners();
        configFragments();
    }

    private void configFragments() {
        if (tipoMidia != null && !tipoMidia.isEmpty()) {
            Class<? extends Fragment> fragmentClass = null;
            switch (tipoMidia) {
                case "foto":
                    fragmentClass = PhotoPostFragment.class;
                    break;
                case "video":
                    fragmentClass = VideoPostFragment.class;
                    break;
                case "gif":
                    fragmentClass = GifPostFragment.class;
                    break;
                case "texto":
                    fragmentClass = TextPostFragment.class;
                    break;
            }

            if (fragmentClass == null) {
                return;
            }

            fragmentPager = new FragmentPagerItemAdapter(
                    getSupportFragmentManager(), FragmentPagerItems.with(ConfigurePostActivity.this)
                    .add("", fragmentClass, enviarBundle())
                    .create());

            viewpager.setAdapter(fragmentPager);
            viewpager.setPagingEnabled(false);
        }

        if (retornarAoItem) {
            viewpager.setCurrentItem(itemAtual);
            retornarAoItem = false;
        }
    }

    private Bundle enviarBundle() {
        if (tipoMidia != null && !tipoMidia.isEmpty()) {
            if (tipoMidia.equals("gif") && urlGif != null
                    && !urlGif.isEmpty()) {
                return bundleGif();
            } else if (tipoMidia.equals("texto") &&
                    descricao != null && !descricao.isEmpty()) {
                return bundleTexto();
            } else if (uriRecuperada != null) {
                if (tipoMidia.equals("foto")
                        || tipoMidia.equals("video")) {
                    return bundleUri();
                }
            }
        }
        return null;
    }

    private Bundle bundleUri() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("uriRecuperada", uriRecuperada);
        return bundle;
    }

    private Bundle bundleGif() {
        Bundle bundle = new Bundle();
        bundle.putString("urlGif", urlGif);
        return bundle;
    }

    private Bundle bundleTexto() {
        Bundle bundle = new Bundle();
        bundle.putString("descricao", descricao);
        return bundle;
    }

    private void configBundle() {
        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("tipoMidia")) {
                tipoMidia = dados.getString("tipoMidia");
            }
            if (dados.containsKey("uriRecuperada")) {
                uriRecuperada = (Uri) dados.get("uriRecuperada");
            }
            if (dados.containsKey("urlGif")) {
                urlGif = dados.getString("urlGif");
            }
        }
    }

    private void clickListeners() {

    }

    private void inicializarComponentes() {
        viewpager = findViewById(R.id.viewpagerConfigPost);
    }
}