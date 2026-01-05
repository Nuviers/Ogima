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
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EditarPerfilActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton imageButtonAlterarGenero,
            imageButtonAlterarNome;
    private TextView textViewNomeAtual, textViewGeneroAtual,
            textViewNumeroAtual;
    private ListView listaInteresses;
    private Button buttonVoltar, buttonAlterarNumero, buttonRemoverNumero,
            buttonAlterarInteresses, buttonAlterarFotos, buttonExcluirConta,
            buttonDeslogar, btnChangePass;

    private ImageView imageViewPerfilAlterar, imageViewFundoPerfilAlterar;

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String nome, genero, numero, fotoPerfil, fundoPerfil;
    private ArrayList<String> arrayInteresse = new ArrayList<>();
    private ArrayAdapter<String> adapterInteresse;

    private String dadoModificado;
    private StorageReference storageRef;
    private boolean verificarGoogle = false;
    private Switch  switchAddGrupo;
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

        if (autenticacao.getCurrentUser() != null) {
            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuarioLogado = Base64Custom.codificarBase64(emailUsuario);
        }
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        Bundle dados = getIntent().getExtras();
        if (dados != null) {
            if (dados.containsKey("irParaProfile")) {
                irParaProfile = dados.getString("irParaProfile");
            }
        }

        try {
            dadosRecuperados("inicio", "inicio");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //Inicializar componentes
        textViewNomeAtual = findViewById(R.id.textViewNomeAtual);
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
        imageButtonAlterarGenero = findViewById(R.id.imageButtonAlterarGenero);

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
        imageButtonAlterarGenero.setOnClickListener(this);

        buttonAlterarNumero.setOnClickListener(this);
        buttonRemoverNumero.setOnClickListener(this);
        buttonAlterarInteresses.setOnClickListener(this);
        buttonAlterarFotos.setOnClickListener(this);
        buttonExcluirConta.setOnClickListener(this);
        buttonDeslogar.setOnClickListener(this);
        btnChangePass.setOnClickListener(this);
        btnGruposBloqueados.setOnClickListener(this);

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

        EditText editTextDadoSheet = bottomSheetDialog.findViewById(R.id.editTextDadoSheet);
        TextView textViewTituloDialog = bottomSheetDialog.findViewById(R.id.textViewTituloDialog);
        TextView textViewMensagemDialog = bottomSheetDialog.findViewById(R.id.textViewMensagemDialog);
        Button buttonExcluirContaSheet = bottomSheetDialog.findViewById(R.id.buttonExcluirContaSheet);
        EditText editTextEmailReauth = bottomSheetDialog.findViewById(R.id.editTextEmailReauth);
        EditText editTextSenhaReauth = bottomSheetDialog.findViewById(R.id.editTextSenhaReauth);
        Button buttonReauthGoogle = bottomSheetDialog.findViewById(R.id.buttonReauthGoogle);

        FirebaseUser user = autenticacao.getCurrentUser();
        if (user != null) {
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().contains("google.com")) verificarGoogle = true;
            }
        }

        if (verificarGoogle) {
            try {
                if (buttonReauthGoogle != null) buttonReauthGoogle.setVisibility(View.VISIBLE);
                if (buttonExcluirContaSheet != null) buttonExcluirContaSheet.setVisibility(View.GONE);
                if (editTextEmailReauth != null) editTextEmailReauth.setVisibility(View.GONE);
                if (editTextSenhaReauth != null) editTextSenhaReauth.setVisibility(View.GONE);
                if (textViewTituloDialog != null)
                    textViewTituloDialog.setText("Digite DELETE no campo abaixo e clique no botão excluir conta pelo google para confirmar a exclusão!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (buttonReauthGoogle != null) {
                buttonReauthGoogle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dadoModificado = editTextDadoSheet.getText().toString().trim();

                        if (!dadoModificado.isEmpty() && dadoModificado.equals("DELETE")) {
                            excluirConta("nada", "nada");
                            bottomSheetDialog.dismiss();
                        } else {
                            try {
                                if (textViewMensagemDialog != null)
                                    textViewMensagemDialog.setText("Digite DELETE no campo de texto");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }

        } else {
            // Logado por email
            if (buttonExcluirContaSheet != null) {
                buttonExcluirContaSheet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dadoModificado = editTextDadoSheet.getText().toString().trim();
                        String recuperarEmail = editTextEmailReauth.getText().toString().trim();
                        String recuperarSenha = editTextSenhaReauth.getText().toString().trim();

                        if (dadoModificado.isEmpty() || !dadoModificado.equals("DELETE")) {
                            if (textViewMensagemDialog != null)
                                textViewMensagemDialog.setText("Digite DELETE no campo de texto");
                            return;
                        }

                        if (recuperarEmail.isEmpty() || recuperarSenha.isEmpty()) {
                            if (textViewMensagemDialog != null)
                                textViewMensagemDialog.setText("Por favor, informe seus dados para confirmar");
                            return;
                        }

                        if (dadoModificado.equals("DELETE")) {
                            excluirConta(recuperarEmail, recuperarSenha);
                            bottomSheetDialog.dismiss();
                        }
                    }
                });
            }
        }

        bottomSheetDialog.show();
    }

    public void dadosRecuperados(String novoDado, String filho) {
        if (idUsuarioLogado == null) return;

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuarioLogado);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario != null) {
                        if (usuario.getIdGruposBloqueados() != null
                                && usuario.getIdGruposBloqueados().size() > 0) {
                            btnGruposBloqueados.setVisibility(View.VISIBLE);
                        } else {
                            btnGruposBloqueados.setVisibility(View.GONE);
                        }

                        String phoneMask;

                        nome = usuario.getNomeUsuario();
                        arrayInteresse = usuario.getInteresses();
                        genero = usuario.getGeneroUsuario();
                        numero = usuario.getNumero();
                        fotoPerfil = usuario.getMinhaFoto();
                        fundoPerfil = usuario.getMeuFundo();
                        conviteGrupoSomentePorAmigos = usuario.getGruposSomentePorAmigos();
                        privacidadePostagens = usuario.getPrivacidadePostagens();

                        //Criando adaptador para listview
                        adapterInteresse = new ArrayAdapter<String>(getApplicationContext(),
                                R.layout.customizedtextlist,
                                arrayInteresse);

                        if (emailUsuario != null) {

                            if (numero != null && !numero.equals("desvinculado")) {
                                if (numero.length() > 4 && nome != null && nome.length() > 4) {
                                    phoneMask = numero.substring(0, 3)
                                            + numero.substring(3, numero.length() - 4)
                                            .replaceAll("[^\\d]", "")
                                            .replaceAll("\\d", "*")
                                            + numero.substring(numero.length() - 4);
                                    textViewNumeroAtual.setText(phoneMask);
                                } else {
                                    textViewNumeroAtual.setText(numero);
                                }
                            } else {
                                textViewNumeroAtual.setText(numero);
                            }

                            try {
                                textViewNomeAtual.setText(nome);
                                textViewGeneroAtual.setText(genero);
                                listaInteresses.setAdapter(adapterInteresse);

                                if (fotoPerfil != null) {
                                    GlideCustomizado.loadUrlComListener(getApplicationContext(),
                                            fotoPerfil, imageViewPerfilAlterar,
                                            android.R.color.transparent,
                                            GlideCustomizado.CIRCLE_CROP,
                                            false, usuario.isStatusEpilepsia(),
                                            new GlideCustomizado.ListenerLoadUrlCallback() {
                                                @Override
                                                public void onCarregado() {}
                                                @Override
                                                public void onError(String message) {}
                                            });
                                } else {
                                    GlideCustomizado.loadDrawableCircular(getApplicationContext(),
                                            R.drawable.background_car, imageViewPerfilAlterar,
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
                                                public void onCarregado() {}
                                                @Override
                                                public void onError(String message) {}
                                            });
                                } else {
                                    GlideCustomizado.loadDrawableImage(getApplicationContext(),
                                            R.drawable.placeholderuniverse, imageViewFundoPerfilAlterar, // Corrigido para imageViewFundoPerfilAlterar
                                            android.R.color.transparent);
                                }

                                if (conviteGrupoSomentePorAmigos != null) {
                                    switchAddGrupo.setChecked(conviteGrupoSomentePorAmigos);
                                } else {
                                    switchAddGrupo.setChecked(false);
                                }

                                if (privacidadePostagens != null) {
                                    verificaPrivacidadePostagemAtual(privacidadePostagens);
                                } else {
                                    radioBtnPstTodos.setChecked(true);
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                } else if (snapshot == null) {
                    ToastCustomizado.toastCustomizado("Nenhum dado localizado", getApplicationContext());
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ToastCustomizado.toastCustomizado("Ocorreu um erro: " + error.getMessage(), getApplicationContext());
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.putExtra("irParaProfile", "irParaProfile");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButtonAlterarNome: {
                Intent intent = new Intent(getApplicationContext(), EdicaoCadActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("tipoEdicao", "nome");
                startActivity(intent);
                break;
            }
            case R.id.imageButtonAlterarGenero: {
                Intent intent = new Intent(getApplicationContext(), EdicaoCadActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("tipoEdicao", "genero");
                startActivity(intent);
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
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("edit", "edit");
                startActivity(intent);
                finish();
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
            case R.id.switchAddGrupo: {
                if (autenticacao.getCurrentUser() != null) {
                    DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuarioLogado);
                    usuarioRef.child("gruposSomentePorAmigos").setValue(switchAddGrupo.isChecked());
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
        builder.setMessage("A exclusão resultará na perda de todos os dados da sua conta, não será possível recuperá-la!");
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
        if (usuarioAtual == null) return;

        // --- CENÁRIO: CONTA GOOGLE ---
        if (verificarGoogle) {
            // 1. Configura as opções do Google novamente (precisa do Web Client ID)
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.SEND_GOGL_ACCESS) // Certifique-se que essa constante está acessível aqui
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

            // 2. Tenta renovar o token silenciosamente (Silent Sign In)
            // Isso gera um token NOVO e FRESCO para podermos deletar a conta
            googleSignInClient.silentSignIn().addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                @Override
                public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                    // Sucesso! Temos um token novo.
                    if (googleSignInAccount != null) {
                        AuthCredential credentialGoogle = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

                        // 3. Agora reautentica no Firebase com o token novo
                        usuarioAtual.reauthenticate(credentialGoogle).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ToastCustomizado.toastCustomizado("Processando exclusão...", getApplicationContext());
                                    excluirDadosESair(usuarioAtual);
                                } else {
                                    ToastCustomizado.toastCustomizado("Erro técnico ao reautenticar. Tente mais tarde.", getApplicationContext());
                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Se a renovação silenciosa falhar (muito tempo sem logar), pede para o usuário logar de novo
                    ToastCustomizado.toastCustomizado("Sessão Google expirada. Por segurança, faça Logout e Login novamente para excluir.", getApplicationContext());
                }
            });

        }
        // --- CENÁRIO: CONTA EMAIL/SENHA ---
        else {
            AuthCredential credential = EmailAuthProvider.getCredential(email, senha);
            usuarioAtual.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        ToastCustomizado.toastCustomizado("Processando exclusão...", getApplicationContext());
                        excluirDadosESair(usuarioAtual);
                    } else {
                        ToastCustomizado.toastCustomizado("Senha incorreta.", getApplicationContext());
                    }
                }
            });
        }
    }

    private void excluirDadosESair(FirebaseUser usuarioAtual) {
        if (idUsuarioLogado == null) return;

        DatabaseReference remocaoRef = firebaseRef.child("usuarios").child(idUsuarioLogado);

        // Remove Fotos do Storage
        StorageReference imagemRef = storageRef.child("usuarios").child(idUsuarioLogado).child("minhaFoto.jpeg");
        StorageReference fundoRef = storageRef.child("usuarios").child(idUsuarioLogado).child("meuFundo.jpeg");

        imagemRef.delete().addOnFailureListener(e -> {}); // Ignora se não existir
        fundoRef.delete().addOnFailureListener(e -> {});

        // Remove dados do Realtime Database
        remocaoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Deletar o usuário da Autenticação por último
                usuarioAtual.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ToastCustomizado.toastCustomizado("Conta excluída com sucesso!", getApplicationContext());

                            // Desloga de tudo e volta pro inicio
                            FirebaseAuth.getInstance().signOut();
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                            GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                            mSignInClient.signOut();

                            Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            ToastCustomizado.toastCustomizado("Erro ao excluir conta de autenticação. Contate o suporte.", getApplicationContext());
                        }
                    }
                });
            }
        });
    }

    private void deslogarUsuario() {
        if (idUsuarioLogado != null) {
            DatabaseReference offlineUserRef = firebaseRef.child("usuarios").child(idUsuarioLogado).child("online");
            offlineUserRef.onDisconnect().setValue(false);
            offlineUserRef.setValue(false);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                .requestEmail()
                .build();
        GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        mSignInClient.signOut();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void exibirGruposBloqueados() {
        Intent intent = new Intent(EditarPerfilActivity.this, GruposBloqueadosActivity.class);
        startActivity(intent);
        finish();
    }

    private void salvarPrivacidadePostagens() {
        if (idUsuarioLogado == null) return;
        DatabaseReference privacidadePostagensRef = firebaseRef
                .child("usuarios").child(idUsuarioLogado).child("privacidadePostagens");

        radioGroupPostagens.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioBtnMarcado = findViewById(i);
                if (radioBtnMarcado != null && radioBtnMarcado.isChecked()) {
                    String privacidadeEscolhida = radioBtnMarcado.getText().toString();
                    privacidadePostagensRef.setValue(privacidadeEscolhida);
                }
            }
        });
    }

    private void verificaPrivacidadePostagemAtual(String privacidadeAtual) {
        if (privacidadeAtual == null) return;
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

    private void irParaQRCode() {
        Intent intent = new Intent(getApplicationContext(), QRCodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}