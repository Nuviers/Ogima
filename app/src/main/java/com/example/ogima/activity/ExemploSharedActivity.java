package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;

public class ExemploSharedActivity extends AppCompatActivity {

    private Usuario usuarioLocal;
    private TextView txtViewNomeUserLocal;
    private ImageView imgViewFotoUserLocal;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor salvarDadosEditor;

    private String nomeUsuarioLocal;
    private String fotoUsuarioLocal;

    private Boolean nome = false, foto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testes_shared);

        inicializandoComponentes();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            usuarioLocal = (Usuario) dados.getSerializable("usuarioDestinatario");
        }

        if (usuarioLocal != null) {
            //Wallpapers será o nome do arquivo de preferências e privado, então somente
            //o meu app pode acessar esses dados.
            sharedPreferences = getSharedPreferences("DadosUsuarioLocal", Context.MODE_PRIVATE);
            //salvarDadosNoDispositivo();
            recuperarDadosDoUsuarioLocal();
        }
    }

    private void inicializandoComponentes() {
        txtViewNomeUserLocal = findViewById(R.id.txtViewNomeUserLocal);
        imgViewFotoUserLocal = findViewById(R.id.imgViewFotoUserLocal);
    }

    private void salvarDadosNoDispositivo(Boolean salvarNome, Boolean salvarFoto){

        //Salva os dados.
        salvarDadosEditor = sharedPreferences.edit();

        if (salvarNome) {
            salvarDadosEditor.putString("nomeUsuario", usuarioLocal.getNomeUsuario());
        }

        if (salvarFoto) {
            salvarDadosEditor.putString("fotoUsuario", "https://media1.giphy.com/media/G2TVZ9jMEEGIShKCys/200w.gif?cid=afffb5fempcq2hwp6q70nrsk0kuqbg13xgy8t1482xkuvkpx&rid=200w.gif&ct=g");
        }

        //Finalmente os dados são salvos com o editor.apply();
        if (salvarNome || salvarFoto) {
            salvarDadosEditor.apply();
        }
    }

    private void recuperarDadosDoUsuarioLocal(){

        nomeUsuarioLocal = sharedPreferences.getString("nomeUsuario","");
        fotoUsuarioLocal = sharedPreferences.getString("fotoUsuario", "");

        //Somente adiciona o nome se o nome não existir na coluna nomeUsuario;
        if (!nomeUsuarioLocal.contains(usuarioLocal.getNomeUsuario())) {
            nome = true;
        }else{
            txtViewNomeUserLocal.setText(nomeUsuarioLocal);
        }

        if (!fotoUsuarioLocal.contains("https://media1.giphy.com/media/G2TVZ9jMEEGIShKCys/200w.gif?cid=afffb5fempcq2hwp6q70nrsk0kuqbg13xgy8t1482xkuvkpx&rid=200w.gif&ct=g")) {
            foto = true;
        }else{
            GlideCustomizado.montarGlideFoto(getApplicationContext(),
                    fotoUsuarioLocal, imgViewFotoUserLocal, android.R.color.transparent);
        }

        if (nome || foto) {
            salvarDadosNoDispositivo(nome, foto);
        }
    }
}