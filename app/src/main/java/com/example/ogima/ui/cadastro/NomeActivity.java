package com.example.ogima.ui.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ogima.R;
import com.example.ogima.activity.EditarPerfilActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class NomeActivity extends AppCompatActivity {

    private EditText edtTxtNomeCad;
    private TextView txtMensagemN, txtViewLimite;
    private Usuario usuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private FirebaseUser user;
    private String nomeEdit = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FloatingActionButton fabVoltar, fabProximo;
    private String blockCharacterSet = "'!@#$%¨&*()_+-=[{]}/?|,.;:~^´`";
    private boolean edicao = false;
    private Boolean limiteCaracteresPermitido = false;
    private final int MAX_LENGTH_NAME = 100;
    private final int MIN_LENGTH_NAME = 3;
    private String idUsuario = "";
    private String nomePesquisa = "";

    public interface AtualizarNomeCallback {
        void onNome();

        void onNomePesquisa();

        void onError(String message);
    }

    public interface NovoNomeCallback{
        void onNomeSalvo();
        void onNomePesquisaSalvo();
        void onError(String message);
    }

    @Override
    public void onBackPressed() {
        if (nomeEdit != null) {
            super.onBackPressed();
        }
    }

    public NomeActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cad_nome);
        inicializandoComponentes();
        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("dadosUsuario")) {
                usuario = (Usuario) dados.getSerializable("dadosUsuario");
            }
            if (dados.containsKey("alterarNome")) {
                nomeEdit = dados.getString("alterarNome");
                edicao = true;
            } else {
                edicao = false;
            }
        }
        configInicial();
        clickListeners();
        limiteCaracteres();
    }

    private InputFilter filterSymbol = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }

            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }

            return null;
        }
    };

    private void verificarNome() {
        if (limiteCaracteresPermitido) {
            String nome = edtTxtNomeCad.getText().toString().trim();
            String nomeFormatado = nome.replaceAll("\\s+", " ");
            nomeFormatado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeFormatado);
            nomePesquisa = "";
            nomePesquisa = Normalizer.normalize(nomeFormatado, Normalizer.Form.NFD);
            nomePesquisa = nomePesquisa.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

            if (edicao && nomeEdit != null
                    && !nomeEdit.isEmpty()) {
                //Edição
                if (nomeFormatado != null && !nomeFormatado.isEmpty()) {

                    setarNomeAoUsuario(nomeFormatado, nomePesquisa);

                    atualizarNome(false, "nomeUsuario", nomeFormatado, new AtualizarNomeCallback() {
                        @Override
                        public void onNome() {
                            atualizarNome(true, "nomeUsuarioPesquisa", nomePesquisa, this);
                        }

                        @Override
                        public void onNomePesquisa() {

                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }else{

                }
            } else {
                //Novo usuário.
                if (nomeFormatado != null && !nomeFormatado.isEmpty()) {

                    setarNomeAoUsuario(nomeFormatado, nomePesquisa);

                    novoNome(false, "nomeUsuario", nomeFormatado, new NovoNomeCallback() {
                        @Override
                        public void onNomeSalvo() {
                            novoNome(true, "nomeUsuarioPesquisa", nomePesquisa, this);
                        }

                        @Override
                        public void onNomePesquisaSalvo() {

                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }
            }
        } else {
            ToastCustomizado.toastCustomizado("Limite de caractères não permitido, ele deve ser entre " + MIN_LENGTH_NAME + "-" + MAX_LENGTH_NAME, getApplicationContext());
        }
    }

    private void atualizarNome(boolean nomePesquisa, String campo, String nome, AtualizarNomeCallback callback) {
        DatabaseReference atualizarNomeRef = firebaseRef.child("usuarios")
                .child(idUsuario);
        Map<String, Object> update = new HashMap<>();
        update.put(campo, nome);
        atualizarNomeRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (nomePesquisa) {
                    callback.onNomePesquisa();
                } else {
                    callback.onNome();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private void novoNome(boolean nomePesquisa, String campo, String nome, NovoNomeCallback callback){
        DatabaseReference salvarNomeRef = firebaseRef.child("usuarios")
                .child(idUsuario).child(campo);
        salvarNomeRef.setValue(nome).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (nomePesquisa) {
                    callback.onNomePesquisaSalvo();
                }else{
                    callback.onNomeSalvo();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    private void setarNomeAoUsuario(String nomeUsuario, String nomePesquisa){
        usuario.setNomeUsuario(nomeUsuario);
        usuario.setNomeUsuarioPesquisa(nomePesquisa);
    }

    private void configInicial() {

        user = autenticacao.getCurrentUser();
        usuario = new Usuario();
        edtTxtNomeCad.setFilters(new InputFilter[]{filterSymbol});

        if (edicao && nomeEdit != null
                && !nomeEdit.isEmpty()) {
            fabVoltar.setVisibility(View.VISIBLE);
            edtTxtNomeCad.setText(nomeEdit);
        } else {
            usuario.setEmailUsuario(user.getEmail());
            if (user.isEmailVerified()) {
                usuario.setStatusEmail(true);
            }
        }
    }

    private void limiteCaracteres() {
        InputFilter[] filtersDescricao = new InputFilter[1];
        filtersDescricao[0] = new InputFilter.LengthFilter(MAX_LENGTH_NAME); // Define o limite máximo de 2.000 caracteres.
        edtTxtNomeCad.setFilters(filtersDescricao);
        edtTxtNomeCad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                txtViewLimite.setText(currentLength + "/" + MAX_LENGTH_NAME);
                if (currentLength < MIN_LENGTH_NAME) {
                    limiteCaracteresPermitido = false;
                } else if (currentLength >= MIN_LENGTH_NAME) {
                    limiteCaracteresPermitido = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void continuarCadastro(){
        if (usuario != null) {
            //Remover a parte de apelido e fazer o ajuste para ir para próxima diretamente.
            Intent intent = new Intent(NomeActivity.this, ApelidoActivity.class);
            intent.putExtra("dadosUsuario", usuario);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void voltarParaEdicaoPerfil(){
        Intent intent = new Intent(getApplicationContext(), EditarPerfilActivity.class);
        startActivity(intent);
        finish();
    }

    private void clickListeners() {
        if (edicao) {
            fabVoltar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        fabProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarNome();
            }
        });
    }

    private void inicializandoComponentes() {
        edtTxtNomeCad = findViewById(R.id.edtTxtNomeCad);
        txtMensagemN = findViewById(R.id.txtMensagemN);
        txtViewLimite = findViewById(R.id.txtViewLimiteNomeCad);
        fabVoltar = findViewById(R.id.floatingVoltarNome);
        fabProximo = findViewById(R.id.fabParc);
    }
}