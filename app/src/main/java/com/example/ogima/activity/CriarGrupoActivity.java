package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.parser.ColorParser;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterParticipantesGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class CriarGrupoActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarCadastroGrupo;
    private ImageButton imgBtnBackCadastroGrupo;
    private RecyclerView recyclerParticipantesGrupo;
    private HashSet<String> listaParticipantesSelecionados;
    private AdapterParticipantesGrupo adapterParticipantesGrupo;

    private EditText edtTextNomeGrupo, edtTextDescricaoGrupo;
    private ImageView imgViewNovoGrupo, imgViewSelecionarFotoGrupo;
    private TextView txtViewLimiteNomeGrupo, txtViewLimiteDescricaoGrupo;
    private Button btnDefinirTopicosGrupo, btnGrupoPublico, btnGrupoParticular, btnCriarGrupo;

    private final int MAX_LENGTH_NAME = 100;
    private final int MIN_LENGTH_NAME = 10;
    private final int MAX_LENGTH_DESCRIPTION = 500;
    private final int MIN_LENGTH_DESCRIPTION = 50;
    private Boolean limiteCaracteresPermitido = false;
    private Boolean limiteTopicosPermitido = false;
    private Boolean grupoPublico = false;
    private Boolean grupoParticular = false;

    private String[] topicosGrupo = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design", "Outros"};
    //Verifica quais dos tópicos foram selecionados.
    private final boolean[] checkedItems = new boolean[topicosGrupo.length];
    private final ArrayList<String> topicosSelecionados = new ArrayList<>();
    private ArrayList<String> idsGruposAtuais = new ArrayList<>();

    //Limitador de seleção de tópicos
    private final int MAX_LENGTH_TOPICOS = 15;
    private final int MIN_LENGTH_TOPICOS = 1;

    private ArrayList<String> participantes = new ArrayList<>();

    //Verifição de permissões necessárias

    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();

    private final String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private VerificaTamanhoArquivo verificaTamanhoArquivo = new VerificaTamanhoArquivo();

    private static final int SELECAO_GALERIA = 200;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ProgressDialog progressDialog;
    private StorageReference imagemRef;
    private StorageReference storageRef;

    private Bitmap imagemSelecionada;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private Grupo grupo = new Grupo();

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
            listaParticipantesSelecionados = (HashSet<String>) dados.get("listaParticipantes");
            participantes.addAll(listaParticipantesSelecionados);
            //Verificar essa linha onde adiciona o usuário fundador como participante também
            //não foi testado ainda.
            participantes.add(idUsuario);
            //
            grupo.setParticipantes(participantes);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        configuracaoRecyclerView();

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        configuracaoClickListener();

        limitadorCaracteresGrupo();
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

        btnDefinirTopicosGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exibirTopicos();
            }
        });

        imgViewSelecionarFotoGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validar permissões necessárias para escolha do wallpaper.
                solicitaPermissoes("galeria");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    selecionarFoto();
                }
            }
        });

        btnGrupoPublico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aparenciaOriginalBtn("publico");

                desativarBtn("particular");

                grupo.setGrupoPublico(true);
            }
        });

        btnGrupoParticular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aparenciaOriginalBtn("particular");

                desativarBtn("publico");

                grupo.setGrupoPublico(false);
            }
        });

        btnCriarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verificaLimiteCaracteres()) {
                    grupo.setIdSuperAdmGrupo(idUsuario);
                    grupo.setNomeGrupo(edtTextNomeGrupo.getText().toString());
                    grupo.setDescricaoGrupo(edtTextDescricaoGrupo.getText().toString());
                    //Somente prossegue se o limite de caracteres estiver dentro do permitido.
                    if (verificaLimiteTopicos()) {
                        //Se o limite de tópicos estiver dentro do permitido, prossegue.
                        grupo.setTopicos(topicosSelecionados);
                        if (visibilidadePublica() || visibilidadeParticular()) {
                            //Se foi selecionado a privacidade do grupo como público ou particular,
                            //prosseguir com o salvamento da foto e salvamento dos dados.
                            salvarImagemGrupo();
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Selecione qual será a privacidade do seu grupo", getApplicationContext());
                        }
                    }
                }
            }
        });
    }

    private void limitadorCaracteresGrupo() {

        edtTextNomeGrupo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();

                txtViewLimiteNomeGrupo.setText(currentLength + "/" + MAX_LENGTH_NAME);

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

                txtViewLimiteDescricaoGrupo.setText(currentLength + "/" + MAX_LENGTH_DESCRIPTION);

                if (currentLength >= MAX_LENGTH_DESCRIPTION) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private boolean verificaLimiteCaracteres() {
        if (edtTextNomeGrupo.length() > MAX_LENGTH_NAME || edtTextDescricaoGrupo.length() > MAX_LENGTH_DESCRIPTION) {
            ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else if (edtTextNomeGrupo.length() < MIN_LENGTH_NAME) {
            ToastCustomizado.toastCustomizadoCurto("Necessário que o nome tenha no minímo " + MIN_LENGTH_NAME + " caracteres", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else if (edtTextDescricaoGrupo.length() < MIN_LENGTH_DESCRIPTION) {
            ToastCustomizado.toastCustomizadoCurto("Necessário que a descrição tenha no minímo " + MIN_LENGTH_DESCRIPTION + " caracteres", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else {
            limiteCaracteresPermitido = true;
        }
        return limiteCaracteresPermitido;
    }

    private void exibirTopicos() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Define o título do dialog
        builder.setTitle("Selecione seus topicos");

        builder.setMultiChoiceItems(topicosGrupo, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                // Aqui você pode salvar as seleções do usuário em um array ou em outro local
                // Verifica se o limite de seleção foi atingido
                int count = 0;
                for (boolean checkedItem : checkedItems) {
                    if (checkedItem) {
                        count++;
                    }
                }

                if (count > MAX_LENGTH_TOPICOS) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de tópicos excedido", getApplicationContext());
                }
            }
        });

        // Define o botão OK do dialog e a ação que será executada quando o usuário clicar nele
        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Este método é chamado quando o botão "OK" é clicado
                // Aqui você pode percorrer a lista de hobbies e verificar quais itens foram selecionados
                topicosSelecionados.clear();
                for (int j = 0; j < checkedItems.length; j++) {
                    if (checkedItems[j]) {
                        topicosSelecionados.add(topicosGrupo[j]);
                    }
                }
                if (topicosSelecionados != null) {

                    btnDefinirTopicosGrupo.setText("Definir tópicos " + topicosSelecionados.size() + "/" + "15");

                    ToastCustomizado.toastCustomizadoCurto("Topicos " + topicosSelecionados.size(), getApplicationContext());

                    for (int e = 0; e < checkedItems.length; e++) {
                        if (checkedItems[e]) {
                            // ToastCustomizado.toastCustomizadoCurto("Tópico: " + topicosGrupo[e], getApplicationContext());
                        }
                    }

                    verificaLimiteTopicos();

                }
            }
        }).setNegativeButton("Desmarcar todos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (topicosSelecionados != null) {

                    topicosSelecionados.clear();

                    btnDefinirTopicosGrupo.setText("Definir tópicos " + topicosSelecionados.size() + "/" + "15");

                    ToastCustomizado.toastCustomizadoCurto("Topicos " + topicosSelecionados.size(), getApplicationContext());

                    Arrays.fill(checkedItems, false);
                }

                dialogInterface.cancel();
            }
        });
        // Cria o dialog e o exibe na tela
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean verificaLimiteTopicos() {
        if (topicosSelecionados != null) {
            if (topicosSelecionados.size() > MAX_LENGTH_TOPICOS) {
                limiteTopicosPermitido = false;
                ToastCustomizado.toastCustomizadoCurto("Limite de tópicos excedido", getApplicationContext());
            } else if (topicosSelecionados.size() < MIN_LENGTH_TOPICOS) {
                limiteTopicosPermitido = false;
                ToastCustomizado.toastCustomizadoCurto("Selecione pelo menos um tópico", getApplicationContext());
            } else {
                limiteTopicosPermitido = true;
            }
        }
        return limiteTopicosPermitido;
    }

    private void solicitaPermissoes(String permissao) {
        //Verifica quais permissões falta a ser solicitadas, caso alguma seja negada, exibe um toast.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, CriarGrupoActivity.this, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, CriarGrupoActivity.this);
            }
        }
    }

    private void selecionarFoto() {
        //Passando a intenção de selecionar uma foto pela galeria
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Verificando se a intenção foi atendida com sucesso
        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA) {

            String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
            destinoArquivo += ".jpg";
            final Uri localImagemFotoSelecionada = data.getData();

            if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM, localImagemFotoSelecionada, getApplicationContext())) {
                // Procede com o upload do arquivo
                //*Chamando método responsável pela estrutura do U crop
                openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK || requestCode == 101 && resultCode == RESULT_OK) {

            try {

                if (baos != null) {
                    baos.reset();
                }

                Uri imagemCortada = UCrop.getOutput(data);
                Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemCortada);
                imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

                imagemSelecionada = imagemBitmap;

                exibirImagemSelecionada();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exibirImagemSelecionada() {
        GlideCustomizado.montarGlideCircularBitmap(getApplicationContext(),
                imagemSelecionada, imgViewNovoGrupo, android.R.color.transparent);
    }

    private void salvarImagemGrupo() {
        try {
            //Recupera dados da imagem para o firebase
            byte[] dadosImagem = baos.toByteArray();

            baos.close();

            progressDialog.setMessage("Salvando dados do grupo, aguarde um momento...");
            progressDialog.show();

            imagemRef = storageRef.child("grupos")
                    .child("imagemGrupo")
                    .child(grupo.getIdGrupo())
                    .child("imagem" + grupo.getIdGrupo() + ".jpeg");
            //Verificando progresso do upload
            UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            Uri url = task.getResult();
                            String urlNewPostagem = url.toString();
                            grupo.setFotoGrupo(urlNewPostagem);
                            /*
                            GlideCustomizado.montarGlide(getApplicationContext(),
                                    urlNewPostagem, imgViewNovoGrupo, android.R.color.transparent);
                             */
                            DatabaseReference grupoRef = firebaseRef.child("grupos").child(grupo.getIdGrupo());
                            grupoRef.setValue(grupo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Salva o grupo nos dados do usuário fundador.
                                    //Funcionando em ambas situações, quando não existe nenhum
                                    //grupo ainda e quando existe já algum grupo anterior criado
                                    //pelo fundador. (Colocar algum retorno boolean para saber
                                    //quando é o momento de ir para a activity do grupo).
                                    salvarMeuGrupo();
                                }
                            });
                            progressDialog.dismiss();
                        }
                    });
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean visibilidadePublica() {
        return grupoPublico;
    }

    private boolean visibilidadeParticular() {
        return grupoParticular;
    }

    private void desativarBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            btnGrupoPublico.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoPublico.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnGrupoPublico.setTextColor(getResources().getColor(android.R.color.black));

            grupoPublico = false;
            grupoParticular = true;
        } else {
            btnGrupoParticular.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoParticular.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnGrupoParticular.setTextColor(getResources().getColor(android.R.color.black));

            grupoParticular = false;
            grupoPublico = true;
        }
    }

    private void aparenciaOriginalBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            String corBackground = "#4CAF50";
            btnGrupoPublico.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoPublico.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnGrupoPublico.setTextColor(Color.WHITE);
        } else {
            String corBackground = "#005488";
            btnGrupoParticular.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnGrupoParticular.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnGrupoParticular.setTextColor(Color.WHITE);
        }
    }

    private void salvarMeuGrupo() {

        DatabaseReference meusDadosRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        meusDadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioGrupo = snapshot.getValue(Usuario.class);
                    if (usuarioGrupo.getIdMeusGrupos() != null) {
                        idsGruposAtuais = usuarioGrupo.getIdMeusGrupos();
                        idsGruposAtuais.add(grupo.getIdGrupo());
                        meusDadosRef.child("idMeusGrupos").setValue(idsGruposAtuais).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                finish();
                            }
                        });
                    } else {
                        idsGruposAtuais.add(grupo.getIdGrupo());
                        meusDadosRef.child("idMeusGrupos").setValue(idsGruposAtuais).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                finish();
                            }
                        });
                    }
                }
                meusDadosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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


    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(CriarGrupoActivity.this);
    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar foto");

        options.setCircleDimmedLayer(true);
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }
}