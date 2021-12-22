package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.ogima.R;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.ApelidoActivity;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.example.ogima.ui.cadastro.GeneroActivity;
import com.example.ogima.ui.cadastro.InteresseActivity;
import com.example.ogima.ui.cadastro.NomeActivity;
import com.example.ogima.ui.cadastro.NumeroActivity;
import com.example.ogima.ui.intro.IntrodActivity;
import com.example.ogima.ui.menusInicio.NavigationDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class EditarPerfilActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageButton imageButtonAlterarGenero, imageButtonAlterarApelido,
            imageButtonAlterarNome, imageButtonAlterarLink;
    private TextView textViewApelidoAtual, textViewNomeAtual, textViewGeneroAtual,
            textViewNumeroAtual;
    private ListView listaInteresses;
    private Button buttonVoltar, buttonAlterarNumero, buttonRemoverNumero,
            buttonAlterarInteresses, buttonAlterarFotos, buttonExcluirConta,
            buttonDeslogar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();



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

        imageButtonAlterarNome = findViewById(R.id.imageButtonAlterarNome);
        imageButtonAlterarApelido = findViewById(R.id.imageButtonAlterarApelido);
        imageButtonAlterarGenero = findViewById(R.id.imageButtonAlterarGenero);
        imageButtonAlterarLink = findViewById(R.id.imageButtonAlterarLink);

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


        buttonVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void showBottomSheetDialog(String dadoAtual, String filho, String titulo) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(EditarPerfilActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_alterar_layout);

        LinearLayout alterarDado = bottomSheetDialog.findViewById(R.id.alterarLinearLayout);
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
            Toast.makeText(getApplicationContext(), "Logado pelo google", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Logado por email", Toast.LENGTH_SHORT).show();
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

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    nome = usuario.getNomeUsuario();
                    apelido = usuario.getApelidoUsuario();
                    arrayInteresse = usuario.getInteresses();
                    genero = usuario.getGeneroUsuario();
                    numero = usuario.getNumero();
                    //link = usuario.getLinkUsuario();
                    fotoPerfil = usuario.getMinhaFoto();
                    fundoPerfil = usuario.getMeuFundo();

                    //Criando adaptador para listview
                    adapterInteresse = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            arrayInteresse);

                    if (emailUsuario != null) {

                        try {
                            textViewNomeAtual.setText(nome);
                            textViewApelidoAtual.setText(apelido);
                            textViewGeneroAtual.setText(genero);
                            textViewNumeroAtual.setText(numero);
                            listaInteresses.setAdapter(adapterInteresse);

                            if (fotoPerfil != null) {
                                Glide.with(EditarPerfilActivity.this)
                                        .load(fotoPerfil)
                                        .placeholder(R.drawable.testewomamtwo)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageViewPerfilAlterar);
                            } else {
                                Glide.with(EditarPerfilActivity.this)
                                        .load(R.drawable.testewomamtwo)
                                        .placeholder(R.drawable.testewomamtwo)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .circleCrop()
                                        .into(imageViewPerfilAlterar);
                            }
                            if (fundoPerfil != null) {
                                Glide.with(EditarPerfilActivity.this)
                                        .load(fundoPerfil)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imageViewFundoPerfilAlterar);
                            } else {
                                Glide.with(EditarPerfilActivity.this)
                                        .load(R.drawable.placeholderuniverse)
                                        .placeholder(R.drawable.placeholderuniverse)
                                        .error(R.drawable.errorimagem)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .centerCrop()
                                        .into(imageViewFundoPerfilAlterar);
                            }
                            usuarioRef.removeEventListener(this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                } else if (snapshot == null) {
                    Toast.makeText(getApplicationContext(), " Nenhum dado localizado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public void onBackPressed() {

        // Método para bloquear o retorno.
        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.imageButtonAlterarNome: {
                Intent intent = new Intent(getApplicationContext(), NomeActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("alterarNome", nome);
                startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarApelido: {
                Intent intent = new Intent(getApplicationContext(), ApelidoActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("alterarApelido", apelido);
                startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarGenero: {
                Intent intent = new Intent(getApplicationContext(), GeneroActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("alterarGenero", genero);
                startActivity(intent);
                break;
            }

            case R.id.imageButtonAlterarLink: {
                try {
                    Toast.makeText(getApplicationContext(), "Clicado alterar link", Toast.LENGTH_SHORT).show();
                    //showBottomSheetDialog(link, "linkUsuario");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }

            case R.id.buttonAlterarInteresses: {
                Intent intent = new Intent(getApplicationContext(), InteresseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("alterarInteresses", "arrayInteresse");
                startActivity(intent);
                break;
            }

            case R.id.buttonAlterarFotos: {
                Intent intent = new Intent(getApplicationContext(), FotoPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("alterarFotos", "alterarFotos");
                startActivity(intent);
                break;
            }

            case R.id.buttonAlterarNumero: {
                Intent intent = new Intent(getApplicationContext(), NumeroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("vincularNumero", "vincularN");
                startActivity(intent);
                break;
            }

            case R.id.buttonRemoverNumero: {
                if (numero != null && !numero.equals("desvinculado")) {
                    alertaDesvinculacao();
                } else {
                    Toast.makeText(getApplicationContext(), "Não existe nenhum número de telefone vinculado a essa conta", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.buttonExcluirConta: {
                exibirExcluirDialog();
                break;
            }

            case R.id.buttonDeslogar:{
                deslogarUsuario();
                break;
            }
        }
    }

    private void alertaDesvinculacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deseja desvincular seu número de telefone?");
        builder.setMessage("Para sua segurança, aconselhamos que vincule posteriormente outro número a sua conta");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                autenticacao.getCurrentUser().unlink("phone").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            autenticacao.getCurrentUser().reload();
                            String emailUsuario = autenticacao.getCurrentUser().getEmail();
                            String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                            DatabaseReference numeroRef = firebaseRef.child("usuarios").child(idUsuario).child("numero");
                            numeroRef.setValue("desvinculado");
                            //Refresh activity
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                            Toast.makeText(getApplicationContext(), "Desvinculado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Erro ao desvincular", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                Toast.makeText(getApplicationContext(), "Logado pelo google", Toast.LENGTH_SHORT).show();

                if (acct != null) {
                    AuthCredential credentialGoogle = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                    user.reauthenticate(credentialGoogle).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                excluirDados();
                            } else {
                                Toast.makeText(EditarPerfilActivity.this, "Ocorreu um erro ao reautenticar usuário, deslogue" +
                                                " e logue na sua conta para que seja possível a exclusão" +
                                                "caso o erro persista entre em contato com o suporte!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            } else if (!verificarGoogle.equals("verdadeiro")) {
                Toast.makeText(getApplicationContext(), "Logado por email", Toast.LENGTH_SHORT).show();

                usuarioAtual.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Espere o processo ser concluído", Toast.LENGTH_SHORT).show();

                                    excluirDados();

                                } else {
                                    Toast.makeText(EditarPerfilActivity.this, "Dados inválidos, digite seu email e sua senha corretamente!",
                                            Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Foto de perfil excluida com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getApplicationContext(), "Ocorreu um erro ao excluir os arquivos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fundoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Fundo de perfil excluido com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getApplicationContext(), "Ocorreu um erro ao excluir os arquivos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Desvinculando
        autenticacao.getCurrentUser().unlink("phone").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Número de telefone desvinculado", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getApplicationContext(), "Erro ao desvincular", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Excluindo dados do firebase
        remocaoRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Dados excluidos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Ocorreu um erro ao excluir os dados, caso persista o erro entre em contato com o suporte!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Deletando usuario da autenticação
        usuarioAtual.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Conta excluida com sucesso!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Ocorreu um erro ao excluir a conta, tente novamente!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deslogarUsuario() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_ids))
                .requestEmail()
                .build();

        GoogleSignInClient mSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        FirebaseAuth.getInstance().signOut();
        mSignInClient.signOut();
        Intent intent = new Intent(getApplicationContext(), IntrodActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();

        //onBackPressed();
    }
}

