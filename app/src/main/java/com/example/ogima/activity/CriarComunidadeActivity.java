package com.example.ogima.activity;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterParticipantesComunidade;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.SolicitaPermissoes;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.VerificaTamanhoArquivo;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Convite;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CriarComunidadeActivity extends AppCompatActivity {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Toolbar toolbarCadastroComunidade;
    private ImageButton imgBtnBackCadastroComunidade;
    private RecyclerView recyclerParticipantesComunidade;
    private HashSet<String> listaParticipantesSelecionados;
    private AdapterParticipantesComunidade adapterParticipantesComunidade;

    private EditText edtTextNomeComunidade, edtTextDescricaoComunidade;
    private ImageView imgViewNovoComunidade, imgViewSelecionarFotoComunidade;
    private TextView txtViewLimiteNomeComunidade, txtViewLimiteDescricaoComunidade, txtTituloCriarComunidade;
    private Button btnDefinirTopicosComunidade, btnComunidadePublico, btnComunidadeParticular, btnCriarComunidade;

    private final int MAX_LENGTH_NAME = 100;
    private final int MIN_LENGTH_NAME = 10;
    private final int MAX_LENGTH_DESCRIPTION = 500;
    private final int MIN_LENGTH_DESCRIPTION = 50;
    private Boolean limiteCaracteresPermitido = false;
    private Boolean limiteTopicosPermitido = false;
    private Boolean comunidadePublico = false;
    private Boolean comunidadeParticular = false;

    private String[] topicosComunidade = {"Leitura", "Cinema", "Esportes", "Artesanato", "Fotografia", "Culinária", "Viagens", "Música", "Dança", "Teatro", "Jogos", "Animais", "Moda", "Beleza", "Esportes Radicais", "Ciência", "Política", "História", "Geografia", "Idiomas", "Tecnologia", "Natureza", "Filosofia", "Religião", "Medicina", "Educação", "Negócios", "Marketing", "Arquitetura", "Design"};
    //Verifica quais dos tópicos foram selecionados.
    private final boolean[] checkedItems = new boolean[topicosComunidade.length];
    private final ArrayList<String> topicosSelecionados = new ArrayList<>();
    private ArrayList<String> idsComunidadesAtuais = new ArrayList<>();

    //Limitador de seleção de tópicos
    private final int MAX_LENGTH_TOPICOS = 15;
    private final int MIN_LENGTH_TOPICOS = 1;

    private ArrayList<String> participantes = new ArrayList<>();
    private ArrayList<String> convites = new ArrayList<>();

    //Verifição de permissões necessárias

    private SolicitaPermissoes solicitaPermissoes = new SolicitaPermissoes();

    private final String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private VerificaTamanhoArquivo verificaTamanhoArquivo = new VerificaTamanhoArquivo();

    private static final int SELECAO_GALERIA = 200;
    private static final int SELECAO_GALERIA_FUNDO = 202;
    private final String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";
    private ProgressDialog progressDialog;
    private StorageReference imagemRef;
    private StorageReference storageRef;

    private Bitmap imagemSelecionada;
    private Bitmap fundoSelecionado;
    private Comunidade comunidade = new Comunidade();

    private List<Usuario> listaEdicaoParticipantes;
    private Comunidade comunidadeEdicao;
    private Convite conviteEdicao;
    private Boolean edicaoComunidade = false;
    private Boolean alterarFotoComunidade = false;
    private Boolean alterarFundoComunidade = false;
    private String idComunidade;
    private Boolean comunidadePublica = false;

    private Convite convite = new Convite();
    private DatabaseReference enviarConviteRef;

    private ImageView imgViewFundoComunidade;
    private ImageButton imgBtnSelecionarFundoComunidade;

    private boolean selecionadoFoto = false;
    private boolean selecionadoFundo = false;
    private StorageReference fundoRef;

    private byte[] dadosImagem;
    private byte[] dadosFundo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_comunidade);

        inicializarComponentes();

        toolbarCadastroComunidade.setTitle("");
        setSupportActionBar(toolbarCadastroComunidade);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        configuracaoClickListener();

        limitadorCaracteresComunidade();

        Bundle dados = getIntent().getExtras();

        if (dados != null) {

            if (dados.containsKey("comunidadePublica")) {
                comunidadePublica = dados.getBoolean("comunidadePublica");
            }

            if (dados.containsKey("comunidadeEdicao")) {

                //Alterando dados da comunidade existente.

                comunidadeEdicao = (Comunidade) dados.getSerializable("comunidadeEdicao");

                if (comunidadeEdicao != null && comunidadeEdicao.getComunidadePublica()) {
                    recyclerParticipantesComunidade.setVisibility(View.GONE);
                    comunidadePublica = true;
                } else {
                    comunidadePublica = false;
                }

                listaEdicaoParticipantes = (List<Usuario>) dados.getSerializable("listaEdicaoParticipantes");
                listaParticipantesSelecionados = new HashSet<>();
                listaParticipantesSelecionados.addAll(comunidadeEdicao.getSeguidores());
                participantes.addAll(comunidadeEdicao.getSeguidores());
                comunidade.setSeguidores(participantes);

                if (comunidadePublica != null && comunidadePublica.equals(false)) {
                    configuracaoRecyclerView();
                }

                txtTituloCriarComunidade.setText("Editar comunidade");

                idComunidade = comunidadeEdicao.getIdComunidade();
                edicaoComunidade = true;

                comunidade.setIdComunidade(comunidadeEdicao.getIdComunidade());

                edtTextNomeComunidade.setText(comunidadeEdicao.getNomeComunidade());
                edtTextDescricaoComunidade.setText(comunidadeEdicao.getDescricaoComunidade());

                if (comunidadeEdicao.getComunidadePublica()) {
                    btnComunidadePublico.performClick();
                } else {
                    btnComunidadeParticular.performClick();
                }

                if (comunidadeEdicao != null
                        && comunidadeEdicao.getFundoComunidade() != null) {
                    comunidade.setFundoComunidade(comunidadeEdicao.getFundoComunidade());
                }

                if (comunidadeEdicao != null
                        && comunidadeEdicao.getFotoComunidade() != null) {
                    comunidade.setFotoComunidade(comunidadeEdicao.getFotoComunidade());
                }

                GlideCustomizado.montarGlide(getApplicationContext(),
                        comunidadeEdicao.getFotoComunidade(), imgViewNovoComunidade, android.R.color.transparent);

                GlideCustomizado.montarGlideFoto(getApplicationContext(),
                        comunidadeEdicao.getFundoComunidade(), imgViewFundoComunidade, android.R.color.transparent);

                if (comunidadeEdicao.getTopicos() != null) {
                    if (comunidadeEdicao.getTopicos().size() > 0) {
                        btnDefinirTopicosComunidade.setText("Definir tópicos " + comunidadeEdicao.getTopicos().size() + "/" + "15");
                    }
                }

                btnCriarComunidade.setText("Salvar edições");

            } else {
                //Nova comunidade

                idComunidade = comunidade.getIdComunidade();
                edicaoComunidade = false;

                if (comunidadePublica != null && comunidadePublica) {
                    recyclerParticipantesComunidade.setVisibility(View.GONE);
                    btnComunidadePublico.performClick();
                    btnComunidadeParticular.setVisibility(View.GONE);
                    btnComunidadePublico.setVisibility(View.VISIBLE);
                    participantes.add(idUsuario);
                    comunidade.setSeguidores(participantes);
                } else {
                    btnComunidadeParticular.performClick();
                    btnComunidadePublico.setVisibility(View.GONE);
                    btnComunidadeParticular.setVisibility(View.VISIBLE);
                    listaParticipantesSelecionados = (HashSet<String>) dados.get("listaParticipantes");
                    if (listaParticipantesSelecionados != null
                            && listaParticipantesSelecionados.size() > 0) {
                        participantes.add(idUsuario);
                        comunidade.setSeguidores(participantes);
                        convites.addAll(listaParticipantesSelecionados);
                        configuracaoRecyclerView();
                    } else {
                        participantes.add(idUsuario);
                        comunidade.setSeguidores(participantes);
                    }
                }

                txtTituloCriarComunidade.setText("Criar comunidade");

                btnCriarComunidade.setText("Criar comunidade");
            }
        }
    }

    private void enviarConvites() {

        if (!edicaoComunidade) {
            if (convites != null && convites.size() > 0) {
                for (String idDestinatario : convites) {
                    enviarConviteRef = firebaseRef.child("convitesComunidade")
                            .child(idDestinatario).child(idComunidade);
                    String idRandomicoConvite = enviarConviteRef.push().getKey();
                    convite.setIdConvite(idRandomicoConvite);
                    convite.setIdDestinatario(idDestinatario);
                    convite.setIdComunidade(comunidade.getIdComunidade());
                    convite.setIdRemetente(idUsuario);
                    HashMap<String, Object> timestampNow = new HashMap<>();
                    timestampNow.put("timeStampConvite", ServerValue.TIMESTAMP);
                    convite.setTimeStampConvite(timestampNow);
                    enviarConviteRef.setValue(convite).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            ToastCustomizado.toastCustomizado("Convites enviados com sucesso", getApplicationContext());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastCustomizado.toastCustomizado("Ocorreu um erro ao enviar os convites " + e.getMessage(), getApplicationContext());
                        }
                    });
                }
            }
        }
    }

    private void configuracaoRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerParticipantesComunidade.setLayoutManager(linearLayoutManager);
        recyclerParticipantesComunidade.setHasFixedSize(true);

        if (adapterParticipantesComunidade != null) {

        } else {
            adapterParticipantesComunidade = new AdapterParticipantesComunidade(listaParticipantesSelecionados, getApplicationContext(), null, false, null);
        }
        recyclerParticipantesComunidade.setAdapter(adapterParticipantesComunidade);

    }

    private void configuracaoClickListener() {

        imgBtnBackCadastroComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnDefinirTopicosComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exibirTopicos();
            }
        });

        imgViewSelecionarFotoComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Validar permissões necessárias para escolha do wallpaper.
                solicitaPermissoes("galeria");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    selecionarFoto();
                }
            }
        });

        imgBtnSelecionarFundoComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitaPermissoes("galeria");
                if (!solicitaPermissoes.exibirPermissaoNegada) {
                    selecionarFundo();
                }
            }
        });

        btnComunidadePublico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aparenciaOriginalBtn("publico");

                desativarBtn("particular");

                comunidade.setComunidadePublica(true);
            }
        });

        btnComunidadeParticular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                aparenciaOriginalBtn("particular");

                desativarBtn("publico");

                comunidade.setComunidadePublica(false);
            }
        });

        btnCriarComunidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verificaLimiteCaracteres()) {
                    comunidade.setIdSuperAdmComunidade(idUsuario);
                    comunidade.setNomeComunidade(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(edtTextNomeComunidade.getText().toString()));
                    comunidade.setDescricaoComunidade(edtTextDescricaoComunidade.getText().toString());
                    //Somente prossegue se o limite de caracteres estiver dentro do permitido.
                    if (verificaLimiteTopicos()) {
                        //Se o limite de tópicos estiver dentro do permitido, prossegue.
                        comunidade.setTopicos(topicosSelecionados);
                        if (visibilidadePublica() || visibilidadeParticular()) {
                            //Se foi selecionado a privacidade do comunidade como público ou particular,
                            //prosseguir com o salvamento da foto e salvamento dos dados.
                            salvarImagemComunidade();
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Selecione qual será a privacidade da sua comunidade", getApplicationContext());
                        }
                    }
                }
            }
        });
    }

    private void salvarFundoComunidade() {

        ToastCustomizado.toastCustomizadoCurto("Fundo", getApplicationContext());

        try {
            //Recupera dados da imagem para o firebase

            if (alterarFundoComunidade) {

                progressDialog.setMessage("Salvando dados da comunidade, aguarde um momento...");
                progressDialog.show();

                fundoRef = storageRef.child("comunidades")
                        .child("fundoComunidade")
                        .child(idComunidade)
                        .child("fundo" + idComunidade + ".jpeg");
                //Verificando progresso do upload
                UploadTask uploadTask = fundoRef.putBytes(dadosFundo);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fundoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Uri url = task.getResult();
                                String urlNewPostagem = url.toString();
                                comunidade.setFundoComunidade(urlNewPostagem);
                                DatabaseReference comunidadeRef = firebaseRef.child("comunidades").child(idComunidade);
                                comunidadeRef.setValue(comunidade).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        DatabaseReference comunidadeRef = firebaseRef.child("comunidades").child(idComunidade);
                                        comunidadeRef.setValue(comunidade).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                enviarConvites();
                                                //Salva o comunidade nos dados do usuário fundador.
                                                //Funcionando em ambas situações, quando não existe nenhum
                                                //comunidade ainda e quando existe já algum comunidade anterior criado
                                                //pelo fundador. (Colocar algum retorno boolean para saber
                                                //quando é o momento de ir para a activity do comunidade).
                                                salvarMeuComunidade();
                                            }
                                        });
                                    }
                                });
                                progressDialog.dismiss();
                            }
                        });
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void limitadorCaracteresComunidade() {

        edtTextNomeComunidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();

                txtViewLimiteNomeComunidade.setText(currentLength + "/" + MAX_LENGTH_NAME);

                if (currentLength >= MAX_LENGTH_NAME) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edtTextDescricaoComunidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int currentLength = charSequence.length();

                txtViewLimiteDescricaoComunidade.setText(currentLength + "/" + MAX_LENGTH_DESCRIPTION);

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
        if (edtTextNomeComunidade.length() > MAX_LENGTH_NAME || edtTextDescricaoComunidade.length() > MAX_LENGTH_DESCRIPTION) {
            ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else if (edtTextNomeComunidade.length() < MIN_LENGTH_NAME) {
            ToastCustomizado.toastCustomizadoCurto("Necessário que o nome tenha no minímo " + MIN_LENGTH_NAME + " caracteres", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else if (edtTextDescricaoComunidade.length() < MIN_LENGTH_DESCRIPTION) {
            ToastCustomizado.toastCustomizadoCurto("Necessário que a descrição tenha no minímo " + MIN_LENGTH_DESCRIPTION + " caracteres", getApplicationContext());
            limiteCaracteresPermitido = false;
        } else {
            limiteCaracteresPermitido = true;
        }
        return limiteCaracteresPermitido;
    }

    private void exibirTopicos() {

        if (edicaoComunidade) {
            for (int i = 0; i < topicosComunidade.length; i++) {
                String topicosAnteriores = topicosComunidade[i];
                if (comunidadeEdicao.getTopicos().contains(topicosAnteriores)) {
                    checkedItems[i] = true;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Define o título do dialog
        builder.setTitle("Selecione seus topicos");

        builder.setMultiChoiceItems(topicosComunidade, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
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
                        topicosSelecionados.add(topicosComunidade[j]);
                    }
                }
                if (topicosSelecionados != null) {

                    btnDefinirTopicosComunidade.setText("Definir tópicos " + topicosSelecionados.size() + "/" + "15");

                    ToastCustomizado.toastCustomizadoCurto("Topicos " + topicosSelecionados.size(), getApplicationContext());

                    for (int e = 0; e < checkedItems.length; e++) {
                        if (checkedItems[e]) {
                            // ToastCustomizado.toastCustomizadoCurto("Tópico: " + topicosComunidade[e], getApplicationContext());
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

                    btnDefinirTopicosComunidade.setText("Definir tópicos " + topicosSelecionados.size() + "/" + "15");

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
                if (edicaoComunidade) {
                    if (comunidadeEdicao.getTopicos().size() > 0) {
                        topicosSelecionados.addAll(comunidadeEdicao.getTopicos());
                        limiteTopicosPermitido = true;
                    } else {
                        limiteTopicosPermitido = false;
                        ToastCustomizado.toastCustomizadoCurto("Selecione pelo menos um tópico", getApplicationContext());
                    }
                } else {
                    limiteTopicosPermitido = false;
                    ToastCustomizado.toastCustomizadoCurto("Selecione pelo menos um tópico", getApplicationContext());
                }
            } else {
                limiteTopicosPermitido = true;
            }
        }
        return limiteTopicosPermitido;
    }

    private void solicitaPermissoes(String permissao) {
        //Verifica quais permissões falta a ser solicitadas, caso alguma seja negada, exibe um toast.
        if (!solicitaPermissoes.verificaPermissoes(permissoesNecessarias, CriarComunidadeActivity.this, permissao)) {
            if (permissao != null) {
                solicitaPermissoes.tratarResultadoPermissoes(permissao, CriarComunidadeActivity.this);
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

    private void selecionarFundo() {
        //Passando a intenção de selecionar uma foto pela galeria
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //Verificando se a intenção foi atendida com sucesso
        if (i.resolveActivity(getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(i, SELECAO_GALERIA_FUNDO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA) {

            alterarFotoComunidade = true;
            selecionadoFoto = true;

            String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
            destinoArquivo += ".jpg";
            final Uri localImagemFotoSelecionada = data.getData();

            if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM, localImagemFotoSelecionada, getApplicationContext())) {
                // Procede com o upload do arquivo
                //*Chamando método responsável pela estrutura do U crop
                openCropActivity(localImagemFotoSelecionada, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
            }

        } else if (resultCode == RESULT_OK && requestCode == SELECAO_GALERIA_FUNDO) {

            alterarFundoComunidade = true;
            selecionadoFundo = true;

            String destinoArquivo = SAMPLE_CROPPED_IMG_NAME;
            destinoArquivo += ".jpg";
            final Uri localImagemFundo = data.getData();

            if (verificaTamanhoArquivo.verificaLimiteMB(MAX_FILE_SIZE_IMAGEM, localImagemFundo, getApplicationContext())) {
                // Procede com o upload do arquivo
                //*Chamando método responsável pela estrutura do U crop
                openCropActivityFundo(localImagemFundo, Uri.fromFile(new File(getCacheDir(), destinoArquivo)));
            }

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE || requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK || requestCode == 101 && resultCode == RESULT_OK) {

            try {

                if (selecionadoFoto) {

                    alterarFotoComunidade = true;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    Uri imagemCortada = UCrop.getOutput(data);
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemCortada);
                    imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    imagemSelecionada = imagemBitmap;

                    dadosImagem = baos.toByteArray();

                    exibirImagemSelecionada();
                    selecionadoFoto = false;
                }

                if (selecionadoFundo) {

                    alterarFundoComunidade = true;

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    Uri fundoCortado = UCrop.getOutput(data);
                    Bitmap fundoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fundoCortado);
                    fundoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    fundoSelecionado = fundoBitmap;

                    dadosFundo = baos.toByteArray();
                    exibirFundoSelecionado();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exibirImagemSelecionada() {
        GlideCustomizado.montarGlideCircularBitmap(getApplicationContext(),
                imagemSelecionada, imgViewNovoComunidade, android.R.color.transparent);
    }

    private void exibirFundoSelecionado() {
        GlideCustomizado.montarGlideBitmap(getApplicationContext(),
                fundoSelecionado, imgViewFundoComunidade, android.R.color.transparent);
    }

    private void salvarImagemComunidade() {
        try {
            //Recupera dados da imagem para o firebase

            if (alterarFotoComunidade || alterarFundoComunidade) {

                progressDialog.setMessage("Salvando dados da comunidade, aguarde um momento...");
                progressDialog.show();

                if (alterarFotoComunidade) {
                    imagemRef = storageRef.child("comunidades")
                            .child("imagemComunidade")
                            .child(idComunidade)
                            .child("imagem" + idComunidade + ".jpeg");
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
                                    comunidade.setFotoComunidade(urlNewPostagem);

                                    if (selecionadoFundo) {
                                        salvarFundoComunidade();
                                    } else {
                                        DatabaseReference comunidadeRef = firebaseRef.child("comunidades").child(idComunidade);
                                        comunidadeRef.setValue(comunidade).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                enviarConvites();
                                                //Salva o comunidade nos dados do usuário fundador.
                                                //Funcionando em ambas situações, quando não existe nenhum
                                                //comunidade ainda e quando existe já algum comunidade anterior criado
                                                //pelo fundador. (Colocar algum retorno boolean para saber
                                                //quando é o momento de ir para a activity do comunidade).
                                                salvarMeuComunidade();
                                            }
                                        });
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                    //Edição somente de fundo.
                } else if (comunidadeEdicao != null &&
                        alterarFundoComunidade && selecionadoFundo) {
                    salvarFundoComunidade();
                }
            } else {
                //Sem edições na foto e no fundo, salvar outros campos caso tenham sido alterados.

                DatabaseReference comunidadeRef = firebaseRef.child("comunidades").child(idComunidade);
                comunidadeRef.setValue(comunidade).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        enviarConvites();
                        //Salva o comunidade nos dados do usuário fundador.
                        //Funcionando em ambas situações, quando não existe nenhum
                        //comunidade ainda e quando existe já algum comunidade anterior criado
                        //pelo fundador. (Colocar algum retorno boolean para saber
                        //quando é o momento de ir para a activity do comunidade).
                        salvarMeuComunidade();
                    }
                });
                progressDialog.dismiss();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean visibilidadePublica() {
        return comunidadePublico;
    }

    private boolean visibilidadeParticular() {
        return comunidadeParticular;
    }

    private void desativarBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            btnComunidadePublico.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadePublico.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnComunidadePublico.setTextColor(getResources().getColor(android.R.color.black));

            comunidadePublico = false;
            comunidadeParticular = true;
        } else {
            btnComunidadeParticular.setBackgroundResource(R.drawable.background_caixa_texto);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadeParticular.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            btnComunidadeParticular.setTextColor(getResources().getColor(android.R.color.black));

            comunidadeParticular = false;
            comunidadePublico = true;
        }
    }

    private void aparenciaOriginalBtn(String buttonRecebido) {
        if (buttonRecebido.equals("publico")) {
            String corBackground = "#4CAF50";
            btnComunidadePublico.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadePublico.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnComunidadePublico.setTextColor(Color.WHITE);
        } else {
            String corBackground = "#005488";
            btnComunidadeParticular.setBackgroundResource(R.drawable.estilo_background_inicio);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnComunidadeParticular.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corBackground)));
            }
            btnComunidadeParticular.setTextColor(Color.WHITE);
        }
    }

    private void salvarMeuComunidade() {

        if (comunidadeEdicao != null) {
            //Edição concluída
            ToastCustomizado.toastCustomizadoCurto("Edição != null", getApplicationContext());
            //Caso seja edição, ele não salva novamente o id do comunidade em usuários.
            Intent intent = new Intent(getApplicationContext(), DetalhesComunidadeActivity.class);
            intent.putExtra("comunidadeAtual", comunidade);
            startActivity(intent);
            finish();
        } else {
            //Nova comunidade.

            DatabaseReference meusDadosRef = firebaseRef.child("usuarios")
                    .child(idUsuario);

            meusDadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Usuario usuarioComunidade = snapshot.getValue(Usuario.class);
                        if (usuarioComunidade.getIdMinhasComunidades() != null) {
                            idsComunidadesAtuais = usuarioComunidade.getIdMinhasComunidades();
                            idsComunidadesAtuais.add(idComunidade);
                            meusDadosRef.child("idMinhasComunidades").setValue(idsComunidadesAtuais).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Intent intent = new Intent(getApplicationContext(), DetalhesComunidadeActivity.class);
                                    intent.putExtra("comunidadeAtual", comunidade);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            ToastCustomizado.toastCustomizadoCurto("Sem ids", getApplicationContext());

                            idsComunidadesAtuais.add(idComunidade);
                            meusDadosRef.child("idMinhasComunidades").setValue(idsComunidadesAtuais).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Intent intent = new Intent(getApplicationContext(), DetalhesComunidadeActivity.class);
                                    intent.putExtra("comunidadeAtual", comunidade);
                                    startActivity(intent);
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
    }

    private void inicializarComponentes() {
        toolbarCadastroComunidade = findViewById(R.id.toolbarCadastroComunidade);
        imgBtnBackCadastroComunidade = findViewById(R.id.imgBtnBackCadastroComunidade);
        recyclerParticipantesComunidade = findViewById(R.id.recyclerParticipantesComunidade);
        edtTextNomeComunidade = findViewById(R.id.edtTextNomeComunidade);
        imgViewNovoComunidade = findViewById(R.id.imgViewNovoComunidade);
        txtViewLimiteNomeComunidade = findViewById(R.id.txtViewLimiteNomeComunidade);
        btnDefinirTopicosComunidade = findViewById(R.id.btnDefinirTopicosComunidade);
        imgViewSelecionarFotoComunidade = findViewById(R.id.imgViewSelecionarFotoComunidade);
        edtTextDescricaoComunidade = findViewById(R.id.edtTextDescricaoComunidade);
        txtViewLimiteDescricaoComunidade = findViewById(R.id.txtViewLimiteDescricaoComunidade);
        btnComunidadePublico = findViewById(R.id.btnComunidadePublico);
        btnComunidadeParticular = findViewById(R.id.btnComunidadeParticular);
        btnCriarComunidade = findViewById(R.id.btnCriarComunidade);
        txtTituloCriarComunidade = findViewById(R.id.txtTituloCriarComunidade);

        imgViewFundoComunidade = findViewById(R.id.imgViewFundoComunidade);
        imgBtnSelecionarFundoComunidade = findViewById(R.id.imgBtnSelecionarFundoComunidade);
    }


    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(CriarComunidadeActivity.this);
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

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivityFundo(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptionsFundo())
                .start(CriarComunidadeActivity.this);
    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptionsFundo() {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionQuality(70);

        options.withMaxResultSize(510, 612);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar fundo");

        options.setCircleDimmedLayer(false);
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }
}