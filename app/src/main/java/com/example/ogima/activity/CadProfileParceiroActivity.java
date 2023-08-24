package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.fragment.FaqFragment;
import com.example.ogima.fragment.FriendsFragment;
import com.example.ogima.fragment.FriendshipRequestFragment;
import com.example.ogima.fragment.RecupEmailFragment;
import com.example.ogima.fragment.parc.EsconderPerfilParcFragment;
import com.example.ogima.fragment.parc.FotosParceirosFragment;
import com.example.ogima.fragment.parc.InteressesParceirosFragment;
import com.example.ogima.fragment.parc.NomeParcFragment;
import com.example.ogima.fragment.parc.OpcoesExibirPerfilParcFragment;
import com.example.ogima.fragment.parc.OrientacaoSexualParcFragment;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.LockedViewPager;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.CadastroParcPagerAdapter;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class CadProfileParceiroActivity extends AppCompatActivity implements DataTransferListener {

    String idUsuario;
    private SmartTabLayout smartTab;
    private LockedViewPager viewpager;
    private FragmentPagerItemAdapter fragmentPagerItemAdapter;
    private String fragmentDesejado;
    private Boolean retornarAoItem = false;
    private int itemAtual;
    private Button btnTesteFrag;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_profile_parceiro);
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        inicializandoComponentes();

        fragmentPagerItemAdapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("", NomeParcFragment.class, enviarIdDonoPerfil("TESTE DE CONTEUDO"))
                .add("", OpcoesExibirPerfilParcFragment.class)
                .add("", OrientacaoSexualParcFragment.class)
                .add("", InteressesParceirosFragment.class)
                .add("", FotosParceirosFragment.class)
                .add("", EsconderPerfilParcFragment.class)
                .create());

        viewpager.setAdapter(fragmentPagerItemAdapter);
        smartTab.setViewPager(viewpager);
        viewpager.setPagingEnabled(false);
        smartTab.setClickable(false);


        if (retornarAoItem) {
            viewpager.setCurrentItem(itemAtual);
            retornarAoItem = false;
        }

        btnTesteFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewpager.setCurrentItem(1);
            }
        });
    }

    private void inicializandoComponentes() {
        viewpager = findViewById(R.id.viewpagerParc);
        smartTab = findViewById(R.id.smartTabParc);

        btnTesteFrag = findViewById(R.id.btnTesteFrag);
    }


    private Bundle enviarIdDonoPerfil(String conteudo) {
        Bundle bundle = new Bundle();
        bundle.putString("conteudo", conteudo);
        return bundle;
    }

    @Override
    public void onUsuarioParc(Usuario usuarioParc, String etapa) {

        //Basicamente ele recebe por interface em qual etapa ele está no momento
        //e de acordo com isso eu envio o usuario por interface para a etapa seguinte.

        Fragment opcoesExibirPerfil = fragmentPagerItemAdapter.getPage(1);
        Fragment orientacao = fragmentPagerItemAdapter.getPage(2);
        Fragment interesses = fragmentPagerItemAdapter.getPage(3);
        Fragment fotos = fragmentPagerItemAdapter.getPage(4);
        Fragment esconderPerfil = fragmentPagerItemAdapter.getPage(5);

        switch (etapa) {
            case "nome":
                if (opcoesExibirPerfil instanceof OpcoesExibirPerfilParcFragment) {
                    ((OpcoesExibirPerfilParcFragment) opcoesExibirPerfil).setName(usuarioParc);
                }
                viewpager.setCurrentItem(1);
                break;
            case "exibirPara":
                if (orientacao instanceof OrientacaoSexualParcFragment) {
                    ((OrientacaoSexualParcFragment) orientacao).setName(usuarioParc);
                }
                viewpager.setCurrentItem(2);
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuarioParc.getNomeParc(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Exibir para: " + usuarioParc.getExibirPerfilPara(), getApplicationContext());
                break;
            case "orientacao":
                viewpager.setCurrentItem(3);
                if (interesses instanceof InteressesParceirosFragment) {
                    ((InteressesParceirosFragment) interesses).setName(usuarioParc);
                }
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuarioParc.getNomeParc(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Exibir para: " + usuarioParc.getExibirPerfilPara(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Orientacao " + usuarioParc.getOrientacaoSexual(), getApplicationContext());
                break;
            case "interesses":
                viewpager.setCurrentItem(4);
                if (fotos instanceof FotosParceirosFragment) {
                    ((FotosParceirosFragment) fotos).setName(usuarioParc);
                }
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuarioParc.getNomeParc(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Exibir para: " + usuarioParc.getExibirPerfilPara(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Orientacao " + usuarioParc.getOrientacaoSexual(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Interesses size " + usuarioParc.getListaInteressesParc().size(), getApplicationContext());
                break;
            case "fotos":
                viewpager.setCurrentItem(5);
                if (esconderPerfil instanceof EsconderPerfilParcFragment) {
                    ((EsconderPerfilParcFragment) esconderPerfil).setName(usuarioParc);
                }
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuarioParc.getNomeParc(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Exibir para: " + usuarioParc.getExibirPerfilPara(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Orientacao " + usuarioParc.getOrientacaoSexual(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Interesses size " + usuarioParc.getListaInteressesParc().size(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Fotos size " + usuarioParc.getFotosParc().size(), getApplicationContext());
                break;
            case "esconderPerfil":
                Intent intent = new Intent(CadProfileParceiroActivity.this, ProfileParcActivity.class);
                intent.putExtra("usuarioParc", usuarioParc);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                ToastCustomizado.toastCustomizadoCurto("Nome " + usuarioParc.getNomeParc(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Exibir para: " + usuarioParc.getExibirPerfilPara(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Orientacao " + usuarioParc.getOrientacaoSexual(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Interesses size " + usuarioParc.getListaInteressesParc().size(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Fotos size " + usuarioParc.getFotosParc().size(), getApplicationContext());
                ToastCustomizado.toastCustomizadoCurto("Ids a esconder size " + usuarioParc.getIdsEsconderParc().size(), getApplicationContext());
                break;
        }
    }
}