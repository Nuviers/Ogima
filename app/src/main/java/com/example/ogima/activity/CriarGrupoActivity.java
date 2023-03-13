package com.example.ogima.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterParticipantesGrupo;
import com.example.ogima.adapter.AdapterUsuariosGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashSet;

public class CriarGrupoActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarCadastroGrupo;
    private ImageButton imgBtnBackCadastroGrupo;
    private RecyclerView recyclerParticipantesGrupo;
    private HashSet<Usuario> listaParticipantesSelecionados;
    private AdapterParticipantesGrupo adapterParticipantesGrupo;

    private EditText edtTextNomeGrupo, edtTextDescricaoGrupo;
    private ImageView imgViewNovoGrupo, imgViewSelecionarFotoGrupo;
    private TextView txtViewLimiteNomeGrupo, txtViewLimiteDescricaoGrupo;
    private Button btnDefinirTopicosGrupo, btnGrupoPublico, btnGrupoParticular, btnCriarGrupo;

    private final int MAX_LENGTH_NAME = 100;
    private final int MAX_LENGTH_DESCRIPTION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_grupo);

        inicializarComponentes();

        toolbarCadastroGrupo.setTitle("");
        setSupportActionBar(toolbarCadastroGrupo);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            listaParticipantesSelecionados = (HashSet<Usuario>) dados.get("listaParticipantes");
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        configuracaoRecyclerView();

        configuracaoClickListener();

        limitadorNomeGrupo();
    }


    private void configuracaoRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerParticipantesGrupo.setLayoutManager(linearLayoutManager);
        recyclerParticipantesGrupo.setHasFixedSize(true);

        if (adapterParticipantesGrupo != null) {

        } else {
            adapterParticipantesGrupo = new AdapterParticipantesGrupo(listaParticipantesSelecionados, getApplicationContext());
        }
        recyclerParticipantesGrupo.setAdapter(adapterParticipantesGrupo);
    }

    private void configuracaoClickListener() {

        imgBtnBackCadastroGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void limitadorNomeGrupo() {

        edtTextNomeGrupo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();

                txtViewLimiteNomeGrupo.setText(currentLength + "/"+MAX_LENGTH_NAME);

                if (currentLength >= MAX_LENGTH_NAME) {
                   ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edtTextDescricaoGrupo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int currentLength = charSequence.length();

                txtViewLimiteDescricaoGrupo.setText(currentLength + "/"+MAX_LENGTH_DESCRIPTION);

                if (currentLength >= MAX_LENGTH_DESCRIPTION) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void inicializarComponentes() {
        toolbarCadastroGrupo = findViewById(R.id.toolbarCadastroGrupo);
        imgBtnBackCadastroGrupo = findViewById(R.id.imgBtnBackCadastroGrupo);
        recyclerParticipantesGrupo = findViewById(R.id.recyclerParticipantesGrupo);
        edtTextNomeGrupo = findViewById(R.id.edtTextNomeGrupo);
        imgViewNovoGrupo = findViewById(R.id.imgViewNovoGrupo);
        txtViewLimiteNomeGrupo = findViewById(R.id.txtViewLimiteNomeGrupo);
        btnDefinirTopicosGrupo = findViewById(R.id.btnDefinirTopicosGrupo);
        imgViewSelecionarFotoGrupo = findViewById(R.id.imgViewSelecionarFotoGrupo);
        edtTextDescricaoGrupo = findViewById(R.id.edtTextDescricaoGrupo);
        txtViewLimiteDescricaoGrupo = findViewById(R.id.txtViewLimiteDescricaoGrupo);
        btnGrupoPublico = findViewById(R.id.btnGrupoPublico);
        btnGrupoParticular = findViewById(R.id.btnGrupoParticular);
        btnCriarGrupo = findViewById(R.id.btnCriarGrupo);
    }
}