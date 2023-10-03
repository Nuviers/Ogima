package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.example.ogima.ui.cadastro.NumeroActivity;
import com.example.ogima.ui.intro.IntrodActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class EditarPerfilActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageButton imageButtonAlterarGenero, imageButtonAlterarApelido,
            imageButtonAlterarNome, imageButtonAlterarLink;
    private TextView textViewApelidoAtual, textViewNomeAtual, textViewGeneroAtual,
            textViewNumeroAtual;
    private ListView listaInteresses;
    private Button buttonVoltar, buttonAlterarNumero, buttonRemoverNumero,
            buttonAlterarInteresses, buttonAlterarFotos, buttonExcluirConta,
            buttonDeslogar, btnChangePass;
    private Usuario usuarioLogado;

    private String emailUser;
    public Usuario usuario;
    private ImageView imageViewPerfilAlterar, imageViewFundoPerfilAlterar;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String nome, genero, apelido, numero, fotoPerfil, fundoPerfil,
            link, dadoModificado;
    private ArrayList<String> arrayInteresse = new ArrayList<>();
    ArrayAdapter<String> adapterInteresse;

    private BottomSheetDialog bottomSheetDialog;
    private ImageButton imageButtonAlterar;

    private String generoRecebido;
    private StorageReference imagemRef, fundoRef;
    private StorageReference storageRef;
    private String verificarGoogle = "falso";
    private String exibirApelido;
    private Switch switchExibirNome, switchExibirApelido, switchAddGrupo;
    private String resultadoNick;
    private Boolean conviteGrupoSomentePorAmigos = false;
    private Button btnGruposBloqueados;

    private String irParaProfile = null;

    private RadioGroup radioGroupPostagens;
    private RadioButton radioBtnPstTodos, radioBtnPstVinculos,
            radioBtnPstAmigos, radioBtnPstSeguidores;

    private String idUsuarioLogado, emailUsuario;
    private String privacidadePostagens = null;

    private TextView txtViewQRCode;
    private ImageButton imgBtnQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();


        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }
        }

        try {
            dadosRecuperados("inicio", "inicio");
            //textViewNomeAtual.setMovementMethod(new ScrollingMovementMethod());
            //textViewNomeAtual.setHorizontallyScrolling(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Inicializar componentes
        textViewNomeAtual = findViewById(R.id.textViewNomeAtual);
        textViewApelidoAtual = findViewById(R.id.textViewApelidoAtual);
        textViewGeneroAtual = findViewById(R.id.textViewGeneroAtual);
        textViewNumeroAtual = findViewById(R.id.textViewNumeroAtual);
        imageViewPerfilAlterar = findViewById(R.id.imageViewPerfilAlterar);
        imageViewFundoPerfilAlterar = findViewById(R.id.imageViewFundoPerfilAlterar);
        listaInteresses = findViewById(R.id.listViewInteresses);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        buttonAlterarNumero = findViewById(R.id.buttonAlterarNumero);
        buttonRemoverNumero = findViewById(R.id.buttonRemoverNumero);
        buttonAlterarInteresses = findViewById(R.id.buttonAlterarInteresses);
        buttonAlterarFotos = findViewById(R.id.buttonAlterarFotos);
        buttonExcluirConta = findViewById(R.id.buttonExcluirConta);
        buttonDeslogar = findViewById(R.id.buttonDeslogar);
        btnChangePass = findViewById(R.id.btnChangePass);
        btnGruposBloqueados = findViewById(R.id.btnGruposBloqueados);

        imageButtonAlterarNome = findViewById(R.id.imageButtonAlterarNome);
        imageButtonAlterarApelido = findViewById(R.id.imageButtonAlterarApelido);
        imageButtonAlterarGenero = findViewById(R.id.imageButtonAlterarGenero);
        imageButtonAlterarLink = findViewById(R.id.imageButtonAlterarLink);

        switchExibirNome = findViewById(R.id.switchExibirNome);
        switchExibirApelido = findViewById(R.id.switchExibirApelido);
        switchAddGrupo = findViewById(R.id.switchAddGrupo);

        radioGroupPostagens = findViewById(R.id.radioGroupPrivacidadePostagens);
        radioBtnPstTodos = findViewById(R.id.radioBtnPrivacidadeTodos);
        radioBtnPstVinculos = findViewById(R.id.radioBtnPrivacidadeVinculos);
        radioBtnPstAmigos = findViewById(R.id.radioBtnPrivacidadeAmigos);
        radioBtnPstSeguidores = findViewById(R.id.radioBtnPrivacidadeSeguidores);

        txtViewQRCode = findViewById(R.id.txtViewQRCode);
        imgBtnQRCode = findViewById(R.id.imgBtnQRCode);

        //Configurando clique dos botões
        imageButtonAlterarNome.setOnClickListener(this);
        imageButtonAlterarApelido.setOnClickListener(this);
        imageButtonAlterarGenero.setOnClickListener(this);
        imageButtonAlterarLink.setOnClickListener(this);

        buttonAlterarNumero.setOnClickListener(this);
        buttonRemoverNumero.setOnClickListener(this);
        buttonAlterarInteresses.setOnClickListener(this);
        buttonAlterarFotos.setOnClickListener(this);
        buttonExcluirConta.setOnClickListener(this);
        buttonDeslogar.setOnClickListener(this);
        btnChangePass.setOnClickListener(this);
        btnGruposBloqueados.setOnClickListener(this);

        switchExibirNome.setOnClickListener(this);
        switchExibirApelido.setOnClickListener(this);
        switchAddGrupo.setOnClickListener(this);

        txtViewQRCode.setOnClickListener(this);
        imgBtnQRCode.setOnClickListener(this);

        buttonVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        salvarPrivacidadePostagens();
    }

    private void showBottomSheetDialog(String dadoAtual, String filho, String titulo) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditarPerfilActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_alterar_layout);

        LinearLayout alterarDado = bottomSheetDialog.findViewById(R.id.alterarDado);
        EditText editTextDadoSheet = bottomSheetDialog.findViewById(R.id.editTextDadoSheet);
        TextView textViewTituloDialog = bottomSheetDialog.findViewById(R.id.textViewTituloDialog);
        TextView textViewMensagemDialog = bottomSheetDialog.findViewById(R.id.textViewMensagemDialog);
        Button buttonExcluirContaSheet = bottomSheetDialog.findViewById(R.id.buttonExcluirContaSheet);
        EditText editTextEmailReauth = bottomSheetDialog.findViewById(R.id.editTextEmailReauth);
        EditText editTextSenhaReauth = bottomSheetDialog.findViewById(R.id.editTextSenhaReauth);
        Button buttonReauthGoogle = bottomSheetDialog.findViewById(R.id.buttonReauthGoogle);
        //*dadosRecuperados("inicio","inicio");

        for (UserInfo user : autenticacao.getCurrentUser().getProviderData()) {
            if (user.getProviderId().contains("google.com")) verificarGoogle = "verdadeiro";
        }

        if (verificarGoogle.equals("verdadeiro")) {
            //Toast.makeText(getApplicationContext(), "Logado pelo google", Toast.LENGTH_SHORT).show();
            try {
                buttonReauthGoogle.setVisibility(View.VISIBLE);
                buttonExcluirContaSheet.setVisibility(View.GONE);
                editTextEmailReauth.setVisibility(View.GONE);
                editTextSenhaReauth.setVisibility(View.GONE);
                textViewTituloDialog.setText("Digite DELETE no campo abaixo e clique no botão excluir conta pelo google para confirmar a exclusão!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            buttonReauthGoogle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dadoModificado = editTextDadoSheet.getText().toString();

                    if (!dadoModificado.isEmpty() && dadoModificado.equals("DELETE")) {
                        excluirConta("nada", "nada");
                    } else {
                        try {
                            textViewMensagemDialog.setText("Digite DELETE no campo de texto");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            });

        } else {
            //Toast.makeText(getApplicationContext(), "Logado por email", Toast.LENGTH_SHORT).show();
        }

        buttonExcluirContaSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dadoModificado = editTextDadoSheet.getText().toString();
                String recuperarEmail = editTextEmailReauth.getText().toString();
                String recuperarSenha = editTextSenhaReauth.getText().toString();

                if (dadoModificado.isEmpty() || !dadoModificado.equals("DELETE")) {
                    try {
                        textViewMensagemDialog.setText("Digite DELETE no campo de texto");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (recuperarEmail.isEmpty() || recuperarSenha.isEmpty()) {
                    try {
                        textViewMensagemDialog.setText("Por favor, informe seus dados e digite DELETE no campo de texto");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (dadoModificado.equals("DELETE") && !recuperarEmail.isEmpty() && !recuperarSenha.isEmpty()) {

                    if (!verificarGoogle.equals("verdadeiro")) {
                        excluirConta(recuperarEmail, recuperarSenha);
                    }
                }
            }
        });

        bottomSheetDialog.show();
    }

    public void dadosRecuperados(String novoDado, String filho) {

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuarioLogado);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario.getIdGruposBloqueados() != null
                            && usuario.getIdGruposBloqueados().size() > 0) {
                        btnGruposBloqueados.setVisibility(View.VISIBLE);
                    } else {
                        btnGruposBloqueados.setVisibility(View.GONE);
                    }

                    String phoneMask;

                    nome = usuario.getNomeUsuario();
                    apelido = usuario.getApelidoUsuario();
                    arrayInteresse = usuario.getInteresses();
                    genero = usuario.getGeneroUsuario();
                    numero = usuario.getNumero();
                    //link = usuario.getLinkUsuario();
                    fotoPerfil = usuario.getMinhaFoto();
                    fundoPerfil = usuario.getMeuFundo();
                    exibirApelido = usuario.getExibirApelido();
                    conviteGrupoSomentePorAmigos = usuario.getGruposSomentePorAmigos();
                    privacidadePostagens = usuario.getPrivacidadePostagens();

                    //Criando adaptador para listview
                    adapterInteresse = new ArrayAdapter<String>(getApplicationContext(),
                            //android.R.layout.simple_list_item_1,
                            R.layout.customizedtextlist,
                            //android.R.id.text1,
                            //,
                            arrayInteresse);

                    if (emailUsuario != null) {

                        if (numero != null && !numero.equals("desvinculado")) {
                            phoneMask = numero.substring(0, 3)
                                    + numero.substring(3, nome.length() - 4)
                                    .replaceAll("[^\\d]", "")
                                    .replaceAll("\\d", "*")
                                    + numero.substring(numero.length() - 4);
                            textViewNumeroAtual.setText(phoneMask);
                        } else {
                            textViewNumeroAtual.setText(numero);
                        }

                        try {

                            textViewNomeAtual.setText(nome);
                            textViewApelidoAtual.setText(apelido);
                            textViewGeneroAtual.setText(genero);
                            //
                            listaInteresses.setAdapter(adapterInteresse);

                            if (fotoPerfil != null) {
                                GlideCustomizado.loadUrlComListener(getApplicationContext(),
                                        fotoPerfil, imageViewPerfilAlterar,
                                        android.R.color.transparent,
                                        GlideCustomizado.CIRCLE_CROP,
                                        false, usuario.isStatusEpilepsia(),
                                        new GlideCustomizado.ListenerLoadUrlCallback() {
                                            @Override
                                            public void onCarregado() {

                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                            } else {
                                GlideCustomizado.loadDrawableCircular(getApplicationContext(),
                                        R.drawable.animeprofileopera, imageViewPerfilAlterar,
                                        android.R.color.transparent);
                            }
                            if (fundoPerfil != null) {
                                GlideCustomizado.loadUrlComListener(getApplicationContext(),
                                        fundoPerfil, imageViewFundoPerfilAlterar,
                                        android.R.color.transparent,
                                        GlideCustomizado.CENTER_CROP,
                                        false, usuario.isStatusEpilepsia(),
                                        new GlideCustomizado.ListenerLoadUrlCallback() {
                                            @Override
                                            public void onCarregado() {

                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                            } else {
                                GlideCustomizado.loadDrawableImage(getApplicationContext(),
                                        R.drawable.placeholderuniverse, imageViewPerfilAlterar,
                                        android.R.color.transparent);
                            }

                            if (exibirApelido.equals("não")) {
                                switchExibirApelido.setChecked(false);
                                switchExibirNome.setChecked(true);
                                //Toast.makeText(getApplicationContext(), "Igual a não " + exibirApelido, Toast.LENGTH_SHORT).show();
                            } else if (exibirApelido.equals("sim")) {
                                switchExibirApelido.setChecked(true);
                                switchExibirNome.setChecked(false);
                                //Toast.makeText(getApplicationContext(), "Igual a sim " + exibirApelido, Toast.LENGTH_SHORT).show();
                            } else if (exibirApelido == null) {
                                switchExibirApelido.setChecked(false);
                                switchExibirNome.setChecked(true);
                                //Toast.makeText(getApplicationContext(), "Igual a nulo", Toast.LENGTH_SHORT).show();
                            }

                            if (conviteGrupoSomentePorAmigos != null) {
                                if (conviteGrupoSomentePorAmigos) {
                                    switchAddGrupo.setChecked(true);
                                } else {
                                    switchAddGrupo.setChecked(false);
                                }
                            }

                            if (privacidadePostagens != null) {
                                verificaPrivacidadePostagemAtual(privacidadePostagens);
                            } else {
                                radioBtnPstTodos.setChecked(true);
                            }

                            usuarioRef.removeEventListener(this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                } else if (snapshot == null) {
                    ToastCustomizado.toastCustomizado("Nenhum dado localizado", getApplicationContext());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado("Ocorreu um erro: " + error.getMessage(), getApplicationContext());
            }
        });
    }


    @Override
    public void onBackPressed() {

        if (irParaProfile != null) {
            // Método para bloquear o retorno.
            Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
            intent.putExtra("irParaProfile", "irParaProfile");
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            finish();
        } else {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {


            case R.id.imageButtonAlterarNome: {
                Intent intent = new Intent(getApplicationContext(), EdicaoCadActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("tipoEdicao", "nome");
                startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarApelido: {

                break;
            }

            case R.id.imageButtonAlterarGenero: {
                Intent intent = new Intent(getApplicationContext(), EdicaoCadActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("tipoEdicao", "genero");
                startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarLink: {
                try {
                    //Toast.makeText(getApplicationContext(), "Clicado alterar link", Toast.LENGTH_SHORT).show();
                    //showBottomSheetDialog(link, "linkUsuario");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }

            case R.id.buttonAlterarInteresses: {
                Intent intent = new Intent(getApplicationContext(), EdicaoCadActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("tipoEdicao", "interesses");
                startActivity(intent);
                break;
            }

            case R.id.buttonAlterarFotos: {
                Intent intent = new Intent(getApplicationContext(), FotoPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("edit", "edit");
                startActivity(intent);
                break;
            }

            case R.id.buttonAlterarNumero: {

                if (numero != null && !numero.equals("desvinculado")) {
                    ToastCustomizado.toastCustomizado("Para vincular outro número de telefone, por favor desvincule o atual!", getApplicationContext());
                } else {
                    Intent intent = new Intent(getApplicationContext(), NumeroActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("vincularNumero", "vincularN");
                    startActivity(intent);
                }
                break;
            }

            case R.id.buttonRemoverNumero: {
                if (numero != null && !numero.equals("desvinculado")) {
                    alertaDesvinculacao();
                } else {
                    ToastCustomizado.toastCustomizado("Não existe nenhum número de telefone vinculado a essa conta", getApplicationContext());
                }
                break;
            }

            case R.id.buttonExcluirConta: {
                exibirExcluirDialog();
                break;
            }

            case R.id.buttonDeslogar: {
                deslogarUsuario();
                break;
            }

            case R.id.switchExibirNome: {

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
                try {
                    usuarioRef.child("exibirApelido").setValue("não");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dadosRecuperados(null, null);
                break;
            }

            case R.id.switchExibirApelido: {

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
                try {
                    usuarioRef.child("exibirApelido").setValue("sim");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dadosRecuperados(null, null);
                break;
            }

            case R.id.switchAddGrupo: {
                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);


                if (switchAddGrupo.isChecked()) {
                    usuarioRef = usuarioRef.child("gruposSomentePorAmigos");
                    usuarioRef.setValue(true);
                } else {
                    usuarioRef = usuarioRef.child("gruposSomentePorAmigos");
                    usuarioRef.setValue(false);
                }

                break;
            }

            case R.id.btnChangePass: {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ao clicar para alterar a sua senha, sua conta será deslogada!");
                builder.setMessage("Deseja prosseguir com a alteração?");
                builder.setCancelable(false);
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        UsuarioUtils.deslogarUsuario(getApplicationContext(), new UsuarioUtils.DeslogarUsuarioCallback() {
                            @Override
                            public void onDeslogado() {
                                Intent intent = new Intent(getApplicationContext(), ProblemasLogin.class);
                                intent.putExtra("changePass", "changePass");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }

            case R.id.btnGruposBloqueados: {
                exibirGruposBloqueados();
                break;
            }

            case R.id.txtViewQRCode:
            case R.id.imgBtnQRCode:
                irParaQRCode();
                break;
        }
    }

    private void alertaDesvinculacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deseja desvincular seu número de telefone?");
        builder.setMessage("Para sua segurança, aconselhamos que vincule posteriormente outro número a sua conta");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getApplicationContext(), DesvincularNumeroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }


        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exibirExcluirDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deseja excluir sua conta?");
        builder.setMessage("A exclusão resultará na perda de todos os dados da sua conta, " +
                " não será possível recuperá-la!");
        builder.setCancelable(false);
        builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showBottomSheetDialog(null, null, null);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void excluirConta(String email, String senha) {

        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference remocaoRef = firebaseRef.child("usuarios").child(idUsuario);

        for (UserInfo user : usuarioAtual.getProviderData()) {
            if (user.getProviderId().contains("google.com")) verificarGoogle = "verdadeiro";
        }

        AuthCredential credential = EmailAuthProvider
                .getCredential(email, senha);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        try {
            if (verificarGoogle.equals("verdadeiro")) {
                //Toast.makeText(getApplicationContext(), "Logado pelo google", Toast.LENGTH_SHORT).show();

                if (acct != null) {
                    AuthCredential credentialGoogle = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                    user.reauthenticate(credentialGoogle).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                excluirDados();
                            } else {
                                ToastCustomizado.toastCustomizado("Ocorreu um erro ao reautenticar usuário, deslogue" +
                                        " e logue na sua conta para que seja possível a exclusão" +
                                        "caso o erro persista entre em contato com o suporte!", getApplicationContext());
                            }
                        }
                    });
                }

            } else if (!verificarGoogle.equals("verdadeiro")) {
                //Toast.makeText(getApplicationContext(), "Logado por email", Toast.LENGTH_SHORT).show();

                usuarioAtual.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ToastCustomizado.toastCustomizado("Espere o processo ser concluído", getApplicationContext());
                                    excluirDados();
                                } else {
                                    ToastCustomizado.toastCustomizado("Dados inválidos, digite seu email e sua senha corretamente!", getApplicationContext());
                                }
                            }
                        });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void excluirDados() {

        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference remocaoRef = firebaseRef.child("usuarios").child(idUsuario);


        //Removendo fotos
        imagemRef = storageRef
                .child("imagens")
                .child("perfil")
                .child(idUsuario)
                .child("fotoPerfil.jpeg");

        fundoRef = storageRef
                .child("imagens")
                .child("perfil")
                .child(idUsuario)
                .child("fotoFundo.jpeg");

        imagemRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ToastCustomizado.toastCustomizado("Foto de perfil excluida com sucesso", getApplicationContext());
                }
            }
        });

        fundoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ToastCustomizado.toastCustomizado("Fundo de perfil excluido com sucesso", getApplicationContext());
                }
            }
        });

        //Desvinculando
        if (usuarioAtual != null) {
            List<? extends UserInfo> providers = usuarioAtual.getProviderData();

            for (UserInfo userInfo : providers) {
                if (userInfo.getProviderId().equals(PhoneAuthProvider.PROVIDER_ID)) {
                    // O usuário tem um telefone vinculado à conta
                    autenticacao.getCurrentUser().unlink("phone").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizado("Número de telefone desvinculado", getApplicationContext());
                            } else {
                                ToastCustomizado.toastCustomizado("Erro ao desvincular, tente novamente!", getApplicationContext());
                            }
                        }
                    });
                    break;
                }
            }
        }

        //Excluindo dados do firebase
        remocaoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ToastCustomizado.toastCustomizado("Dados excluidos", getApplicationContext());
                } else {
                    ToastCustomizado.toastCustomizado("Ocorreu um erro ao excluir os dados, caso persista o erro entre em contato com o suporte!", getApplicationContext());
                }
            }
        });

        //Deletando usuario da autenticação
        if (usuarioAtual != null) {
            usuarioAtual.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizado("Conta excluida com sucesso!", getApplicationContext());
                                //Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
                                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                //startActivity(intent);
                                //finish();
                                deslogarUsuario();
                            } else {
                                ToastCustomizado.toastCustomizado("Ocorreu um erro ao excluir a conta, tente novamente!", getApplicationContext());
                            }
                        }
                    });
        }
    }

    private void deslogarUsuario() {

        NotificationManager notificationManager = (NotificationManager) EditarPerfilActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        DatabaseReference offlineUserRef = firebaseRef.child("usuarios")
                .child(idUsuarioLogado).child("online");

        offlineUserRef.onDisconnect().setValue(false);

        offlineUserRef.setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                        .requestEmail()
                        .build();

                GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

                FirebaseAuth.getInstance().signOut();
                mSignInClient.signOut();
                Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                finish();
            }
        });
        //onBackPressed();
    }

    private void exibirGruposBloqueados() {
        Intent intent = new Intent(EditarPerfilActivity.this, GruposBloqueadosActivity.class);
        startActivity(intent);
        finish();
    }

    private void salvarPrivacidadePostagens() {

        DatabaseReference privacidadePostagensRef = firebaseRef
                .child("usuarios").child(idUsuarioLogado).child("privacidadePostagens");

        radioGroupPostagens.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton radioBtnMarcado = findViewById(i);

                boolean isChecked = radioBtnMarcado.isChecked();

                if (isChecked) {
                    String privacidadeEscolhida = radioBtnMarcado.getText().toString();
                    privacidadePostagensRef.setValue(privacidadeEscolhida);
                    ToastCustomizado.toastCustomizadoCurto("Marcado: " + radioBtnMarcado.getText(), getApplicationContext());
                }
            }
        });
    }

    private void verificaPrivacidadePostagemAtual(String privacidadeAtual) {
        switch (privacidadeAtual) {
            case "Todos":
                radioBtnPstTodos.setChecked(true);
                break;
            case "Somente amigos e seguidores":
                radioBtnPstVinculos.setChecked(true);
                break;
            case "Somente amigos":
                radioBtnPstAmigos.setChecked(true);
                break;
            case "Somente seguidores":
                radioBtnPstSeguidores.setChecked(true);
                break;
        }
    }

    private void irParaQRCode(){
        Intent intent = new Intent(getApplicationContext(), TesteQRCodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

