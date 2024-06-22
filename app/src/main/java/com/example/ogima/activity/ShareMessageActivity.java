package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.ogima.R;
import com.example.ogima.adapter.AdapterShareMessage;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.MidiaUtils;
import com.example.ogima.helper.SalvarArquivoLocalmente;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class ShareMessageActivity extends AppCompatActivity {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Mensagem mensagemCompartilhada;

    private HashSet<Usuario> listaContato = new HashSet<>();
    private RecyclerView recyclerShare;
    private TextView txtViewContadorSelecao;
    private AdapterShareMessage adapterShareMessage;
    private ValueEventListener valueEventListenerUsuario;
    private ChildEventListener childEventListenerContato;
    private DatabaseReference recuperarContatosRef, verificaUsuarioRef;
    private SearchView searchViewShare;
    private Button btnShareMessage;
    private HashSet<Usuario> listaContatoBuscada = new HashSet<>();
    private HashSet<Usuario> listaUsuariosSelecionados;

    private StorageReference storageRef;
    private StorageReference imagemRef;
    private ProgressDialog progressDialog;

    //Variáveis para data
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;

    private DatabaseReference salvarMensagemRef, verificaContadorRef,
            verificaContadorDestinatarioRef;

    private SalvarArquivoLocalmente salvarArquivoLocalmente;
    private String caminhoImagem, nomeArquivo, duracao;

    private String nomeRandomico = UUID.randomUUID().toString();
    private StorageReference storageArquivoRef;
    private HashMap<String, Object> dadosMensagem = new HashMap<>();
    private String extensaoArquivo;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        removerListeners();
        listaContato.clear();

        searchViewShare.setQuery("", false);
        searchViewShare.setIconified(true);
        if (searchViewShare.getOnFocusChangeListener() != null) {
            searchViewShare.setOnQueryTextListener(null);
        }

        // Descarta o ProgressDialog se ele ainda estiver sendo exibido
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_message);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        salvarArquivoLocalmente = new SalvarArquivoLocalmente(getApplicationContext());

        //Configurando data de acordo com local do usuário.
        current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        salvarMensagemRef = firebaseRef.child("conversas");

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            mensagemCompartilhada = (Mensagem) dados.getSerializable("mensagemCompartilhada");
        }

        if (mensagemCompartilhada != null) {
            //ToastCustomizado.toastCustomizadoCurto("Conteudo " + mensagemCompartilhada.getConteudoMensagem(), getApplicationContext());
        }

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerShare.setHasFixedSize(true);
        recyclerShare.setLayoutManager(linearLayoutManager);

        if (adapterShareMessage != null) {

        } else {
            adapterShareMessage = new AdapterShareMessage(listaContato, getApplicationContext(), txtViewContadorSelecao, btnShareMessage);
        }
        recyclerShare.setAdapter(adapterShareMessage);

        recuperarContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);

        buscarContatos(null);
        configuracaoSearchView();

        btnShareMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaUsuariosSelecionados = adapterShareMessage.usuariosSelecionados();
                exibirLista();
            }
        });

    }

    private void inicializandoComponentes() {
        recyclerShare = findViewById(R.id.recyclerShare);
        searchViewShare = findViewById(R.id.searchViewShare);
        txtViewContadorSelecao = findViewById(R.id.txtViewContadorSelecao);
        btnShareMessage = findViewById(R.id.btnShareMessage);
    }

    private void buscarContatos(String somenteFavorito) {

        if (childEventListenerContato != null) {
            recuperarContatosRef.removeEventListener(childEventListenerContato);
            childEventListenerContato = null;
        }

        if (valueEventListenerUsuario != null) {
            verificaUsuarioRef.removeEventListener(valueEventListenerUsuario);
            valueEventListenerUsuario = null;
        }

        //Adicionado listaContato.clear() para a lista não duplicar quando
        //for adicionado novos dados, caso ocorra algum erro verificar essa linha de código. VVVV
        listaContato.clear();
        adapterShareMessage.notifyDataSetChanged();

        childEventListenerContato = recuperarContatosRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Contatos contatos = snapshot.getValue(Contatos.class);
                    //Caso exista algum contato
                    verificaUsuarioRef = firebaseRef.child("usuarios")
                            .child(contatos.getIdContato());
                    valueEventListenerUsuario = verificaUsuarioRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario usuario = snapshot.getValue(Usuario.class);
                                if (!usuario.getIdUsuario().equals(mensagemCompartilhada.getIdDestinatario())) {
                                    if (somenteFavorito != null) {
                                        if (contatos.isContatoFavorito()) {
                                            usuario.setContatoFavorito(contatos.isContatoFavorito());
                                            listaContato.add(usuario);
                                            adapterShareMessage.notifyDataSetChanged();
                                        }
                                    } else {
                                        usuario.setContatoFavorito(contatos.isContatoFavorito());
                                        listaContato.add(usuario);
                                        adapterShareMessage.notifyDataSetChanged();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configuracaoSearchView() {
        searchViewShare.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewShare.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Chamado somente quando o usuário confirma o envio do texto.
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Chamado a cada mudança
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    String dadoDigitadoFormatado = dadoDigitado.toLowerCase(Locale.ROOT);
                    pesquisarContatos(dadoDigitadoFormatado);
                } else {
                    if (listaContatoBuscada != null) {
                        listaContatoBuscada.clear();
                    }
                    if (listaContato != null) {
                        listaContatoOriginal();
                    }
                }
                return true;
            }
        });
    }

    private void pesquisarContatos(String dadoDigitado) {
        if (listaContatoBuscada != null) {
            listaContatoBuscada.clear();
        }
        for (Usuario usuario : listaContato) {
            String nomeUsuario = usuario.getNomeUsuario().toLowerCase(Locale.ROOT);
            if (nomeUsuario.startsWith(dadoDigitado)) {
                listaContatoBuscada.add(usuario);
            }
        }
        atualizarListaContatoBuscado();
    }

    private void atualizarListaContatoBuscado() {
        adapterShareMessage = new AdapterShareMessage(listaContatoBuscada, getApplicationContext(), txtViewContadorSelecao, btnShareMessage);
        recyclerShare.setAdapter(adapterShareMessage);
        adapterShareMessage.notifyDataSetChanged();
    }

    private void listaContatoOriginal() {
        adapterShareMessage = new AdapterShareMessage(listaContato, getApplicationContext(), txtViewContadorSelecao, btnShareMessage);
        recyclerShare.setAdapter(adapterShareMessage);
        adapterShareMessage.notifyDataSetChanged();
    }

    private void removerListeners() {
        if (childEventListenerContato != null) {
            recuperarContatosRef.removeEventListener(childEventListenerContato);
            childEventListenerContato = null;
        }
        if (valueEventListenerUsuario != null) {
            verificaUsuarioRef.removeEventListener(valueEventListenerUsuario);
            valueEventListenerUsuario = null;
        }
        if (adapterShareMessage.listenerAdapterContato != null) {
            adapterShareMessage.verificaContatoRef.removeEventListener(adapterShareMessage.listenerAdapterContato);
            adapterShareMessage.listenerAdapterContato = null;
        }

        if (adapterShareMessage.listenerConversaContador != null) {
            adapterShareMessage.verificaConversaContadorRef.removeEventListener(adapterShareMessage.listenerConversaContador);
            adapterShareMessage.listenerConversaContador = null;
        }
    }

    private void exibirLista() {
        if (listaUsuariosSelecionados != null) {
            if (listaUsuariosSelecionados.size() >= 1) {
                compartilharMensagem();
            } else {
                ToastCustomizado.toastCustomizadoCurto("Necessário selecionar pelo menos um usuário para compartilhar!", getApplicationContext());
            }
        }
    }

    private void compartilharMensagem() {

        if (!isFinishing()) {
            progressDialog.setMessage("Compartilhando mensagem, por favor aguarde...");
            progressDialog.show();
        }

        //Verificar permissões antes de qualquer coisa.

        caminhoImagem = mensagemCompartilhada.getConteudoMensagem();

        if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.IMAGE)) {
            salvarArquivoLocalmente.transformarImagemEmFile(caminhoImagem, new SalvarArquivoLocalmente.SalvarArquivoCallback() {
                @Override
                public void onFileSaved(File file) {
                    // Imagem salva temporariamente.
                    funcoesCompartilhamento(file);
                }

                @Override
                public void onSaveFailed(Exception e) {
                    // tratar a falha ao salvar o arquivo
                    progressDialog.dismiss();
                    ToastCustomizado.toastCustomizadoCurto("Falha ao compartilhar o arquivo, tente novamente", getApplicationContext());
                }
            });
        } else if (!mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)
                && !mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.IMAGE)) {
            salvarArquivoLocalmente.transformarMidiaEmFile(caminhoImagem, new SalvarArquivoLocalmente.SalvarArquivoCallback() {
                @Override
                public void onFileSaved(File file) {
                    funcoesCompartilhamento(file);
                }

                @Override
                public void onSaveFailed(Exception e) {
                    progressDialog.dismiss();
                    ToastCustomizado.toastCustomizadoCurto("Falha ao compartilhar o arquivo, tente novamente", getApplicationContext());
                }
            });

        } else if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)) {
            funcoesCompartilhamento(null);
        }
    }

    private void fazerUploadDoArquivo(String idDestinatario, File arquivoTemporario) {

        if (!mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)) {
            nomeArquivo = arquivoTemporario.getName();
            duracao = formatarTimer(obterDuracaoAudio(arquivoTemporario));
        }

        dadosMensagem.put("tipoMensagem", mensagemCompartilhada.getTipoMensagem());
        dadosMensagem.put("idRemetente", idUsuario);
        dadosMensagem.put("idDestinatario", idDestinatario);
        switch (mensagemCompartilhada.getTipoMensagem()) {
            case MidiaUtils.IMAGE:
                storageArquivoRef = storageRef.child("mensagens").child("fotos").child(idUsuario).child(idDestinatario).child("foto" + nomeRandomico + ".jpeg");
                extensaoArquivo = ".jpg";
                break;
            case MidiaUtils.GIF:
                //Gif não é upada no storage, continuar assim.
                extensaoArquivo = ".gif";
                break;
            case MidiaUtils.VIDEO:
                storageArquivoRef = storageRef.child("mensagens").child("videos").child(idUsuario).child(idDestinatario).child("video" + nomeRandomico + ".mp4");
                extensaoArquivo = ".mp4";
                break;
            case MidiaUtils.DOCUMENT:
                storageArquivoRef = storageRef.child("mensagens").child("documentos").child(idUsuario).child(idDestinatario).child(nomeArquivo);
                dadosMensagem.put("nomeDocumento", nomeArquivo);
                dadosMensagem.put("tipoArquivo", getMimeType(arquivoTemporario));
                break;
            case MidiaUtils.MUSIC:
                storageArquivoRef = storageRef.child("mensagens").child("musicas").child(idUsuario).child(idDestinatario).child(nomeArquivo);
                dadosMensagem.put("nomeDocumento", nomeArquivo);
                dadosMensagem.put("duracaoMusica", duracao);
                break;
            case MidiaUtils.AUDIO:
                storageArquivoRef = storageRef.child("mensagens").child("audios").child(idUsuario).child(idDestinatario).child(nomeArquivo);
                dadosMensagem.put("duracaoMusica", duracao);
                break;
        }

        if (!mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)) {
            UploadTask uploadTask = storageArquivoRef.putFile(Uri.fromFile(arquivoTemporario));
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageArquivoRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            ToastCustomizado.toastCustomizadoCurto("Compartilhado com sucesso" , getApplicationContext());
                            arquivoTemporario.delete(); // Exclui o arquivo temporário
                            Uri url = task.getResult();
                            String urlMensagem = url.toString();
                            dadosMensagem.put("conteudoMensagem", urlMensagem);
                            obterNomeData();
                            enviarMensagem(idDestinatario);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ToastCustomizado.toastCustomizadoCurto("Erro ao compartilhar, tente novamente mais tarde", getApplicationContext());
                    arquivoTemporario.delete(); // Exclui o arquivo temporário
                    progressDialog.dismiss();
                }
            });
        } else {
            //Gif
            dadosMensagem.put("conteudoMensagem", mensagemCompartilhada.getConteudoMensagem());
            obterNomeData();
            enviarMensagem(idDestinatario);
        }
    }

    private void obterNomeData() {
        if (localConvertido.equals("pt_BR")) {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
            date = new Date();
            String novaData = dateFormat.format(date);
            dadosMensagem.put("dataMensagem", novaData);
            dadosMensagem.put("dataMensagemCompleta", date);
            String dataNome = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");

            if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.AUDIO)) {
                dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
            } else if (!mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.MUSIC)
                    && !mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.DOCUMENT)) {
                dadosMensagem.put("nomeDocumento", replaceAll + extensaoArquivo);
            }

        } else {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
            date = new Date();
            String novaData = dateFormat.format(date);
            dadosMensagem.put("dataMensagem", novaData);
            dadosMensagem.put("dataMensagemCompleta", date);
            String dataNome = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String replaceAll = dataNome.replaceAll("[\\-\\+\\.\\^:,]", "");

            if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.AUDIO)) {
                dadosMensagem.put("nomeDocumento", "audio" + replaceAll + ".mp3");
            } else if (!mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.MUSIC)
                    && !mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.DOCUMENT)) {
                dadosMensagem.put("nomeDocumento", replaceAll + extensaoArquivo);
            }
        }
    }

    private void enviarMensagem(String idDestinatario) {
        salvarMensagemRef.child(idUsuario).child(idDestinatario)
                .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                            atualizarContador(idDestinatario);
                            progressDialog.dismiss();
                        } else {
                            //ToastCustomizado.toastCustomizadoCurto("Erro ao enviar mensagem", getApplicationContext());
                            progressDialog.dismiss();
                        }
                    }
                });

        salvarMensagemRef.child(idDestinatario).child(idUsuario)
                .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)) {
                                ToastCustomizado.toastCustomizadoCurto("Compartilhado com sucesso" , getApplicationContext());
                            }
                            finish();
                        }else{
                            if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)) {
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro a compartilhar, tente novamente mais tarde" , getApplicationContext());
                            }
                            finish();
                        }
                    }
                });
    }

    private void atualizarContador(String idDestinatario) {

        verificaContadorRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario)
                .child(idDestinatario);

        verificaContadorDestinatarioRef = firebaseRef.child("contadorMensagens")
                .child(idDestinatario)
                .child(idUsuario);

        verificaContadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagem1 = snapshot.getValue(Mensagem.class);
                    verificaContadorRef.child("totalMensagens").setValue(mensagem1.getTotalMensagens() + 1);
                } else {
                    verificaContadorRef.child("totalMensagens").setValue(1);
                }
                verificaContadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        verificaContadorDestinatarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagemDestinatario = snapshot.getValue(Mensagem.class);
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(mensagemDestinatario.getTotalMensagens() + 1);
                } else {
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(1);
                }
                verificaContadorDestinatarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String getMimeType(File file) {
        String mimeType = null;
        try {
            URLConnection connection = new URL("file://" + file.getAbsolutePath()).openConnection();
            mimeType = connection.getContentType();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mimeType;
    }

    private String formatarTimer(long milliSeconds) {
        String timerString = "";
        String secondString;

        int hours = (int) (milliSeconds / (1000 * 60 * 60));
        int minutes = (int) (milliSeconds % (1000 * 60 * 60) / (1000 * 60));
        int seconds = (int) (milliSeconds % (1000 * 60 * 60) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }

        if (seconds < 10) {
            secondString = "0" + seconds;
        } else {
            secondString = "" + seconds;
        }

        timerString = timerString + minutes + ":" + secondString;

        return timerString;
    }

    private int obterDuracaoAudio(File file) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaPlayer.release();
        }
        return 0;
    }

    private void funcoesCompartilhamento(File file) {
        if (listaUsuariosSelecionados != null) {
            for (Usuario usuarioDestinatario : listaUsuariosSelecionados) {
                String idDestinatario = usuarioDestinatario.getIdUsuario();
                nomeRandomico = UUID.randomUUID().toString();
                //Faz o upload e o envio da mensagem
                if (mensagemCompartilhada.getTipoMensagem().equals(MidiaUtils.GIF)) {
                    fazerUploadDoArquivo(idDestinatario, null);
                }else{
                    fazerUploadDoArquivo(idDestinatario, file);
                }
            }
        }
    }
}