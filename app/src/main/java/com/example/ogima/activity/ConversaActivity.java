package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterContato;
import com.example.ogima.adapter.AdapterMensagem;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbarConversa;
    private ImageButton imgBtnBackConversa, imgButtonEnviarFotoChat;
    private Button btnTotalMensagensDestinatario;
    private ImageView imgViewFotoDestinatario, imgViewGifDestinatario;
    private TextView txtViewNomeDestinatario, txtViewNivelAmizadeDestinatario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;
    private Usuario usuarioDestinatario;
    private Contatos contatoDestinatario;
    private Chip chipInteresse01, chipInteresse02, chipInteresse03, chipInteresse04, chipInteresse05;
    private EditText edtTextMensagemChat;
    private FloatingActionButton fabEnviarMensagemChat;
    private RecyclerView recyclerMensagensChat;
    //Variáveis para data
    private DateFormat dateFormat;
    private Date date;
    private String localConvertido;
    private Locale current;
    private AdapterMensagem adapterMensagem;
    private List<Mensagem> listaMensagem = new ArrayList<>();
    private ChildEventListener childEventListener;
    private DatabaseReference recuperarMensagensRef;

    @Override
    protected void onStop() {
        super.onStop();
        recuperarMensagensRef.removeEventListener(childEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        buscarMensagens();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurando data de acordo com local do usuário.
        current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);

        Bundle dados = getIntent().getExtras();

        if (dados != null) {
            contatoDestinatario = (Contatos) dados.getSerializable("contato");
            usuarioDestinatario = (Usuario) dados.getSerializable("usuario");
            exibirDadosDestinatario();
            recuperarMensagensRef = firebaseRef.child("conversas")
                    .child(idUsuario).child(usuarioDestinatario.getIdUsuario());
        }

        fabEnviarMensagemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensagem();
            }
        });

        //Configurando recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagensChat.setLayoutManager(linearLayoutManager);
        recyclerMensagensChat.setHasFixedSize(true);
        if (adapterMensagem != null) {

        } else {
            adapterMensagem = new AdapterMensagem(getApplicationContext(), listaMensagem);
        }
        recyclerMensagensChat.setAdapter(adapterMensagem);
    }

    private void buscarMensagens() {

        childEventListener = recuperarMensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                listaMensagem.add(mensagem);
                adapterMensagem.notifyDataSetChanged();
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

    private void enviarMensagem() {
        if (!edtTextMensagemChat.getText().toString().isEmpty()) {
            String conteudoMensagem = edtTextMensagemChat.getText().toString();
            Mensagem mensagem = new Mensagem();
            mensagem.setTipoMensagem("texto");
            mensagem.setIdRemetente(idUsuario);
            mensagem.setIdDestinatario(usuarioDestinatario.getIdUsuario());
            mensagem.setConteudoMensagem(conteudoMensagem);

            if (localConvertido.equals("pt_BR")) {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                date = new Date();
                String novaData = dateFormat.format(date);
                mensagem.setDataMensagem(novaData);
                mensagem.setDataMensagemCompleta(date);
            } else {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                date = new Date();
                String novaData = dateFormat.format(date);
                mensagem.setDataMensagem(novaData);
                mensagem.setDataMensagemCompleta(date);
            }

            DatabaseReference salvarMensagem = firebaseRef.child("conversas");

            DatabaseReference verificaMensagemRef = firebaseRef
                    .child("contadorMensagem").child(idUsuario).child(usuarioDestinatario.getIdUsuario());

            verificaMensagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Mensagem mensagemContador = new Mensagem();
                        mensagemContador = snapshot.getValue(Mensagem.class);
                        ToastCustomizado.toastCustomizadoCurto("Diferente de nulo", getApplicationContext());
                        int totalMensagens = mensagemContador.getTotalMensagens() + 1;
                        verificaMensagemRef.child("totalMensagens").setValue(totalMensagens);
                    } else {
                        verificaMensagemRef.child("totalMensagens").setValue(1);
                    }
                    verificaMensagemRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference verificaMensagemDestinatarioRef = firebaseRef
                    .child("contadorMensagem").child(usuarioDestinatario.getIdUsuario()).child(idUsuario);

            verificaMensagemDestinatarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        Mensagem mensagemContador = new Mensagem();
                        mensagemContador = snapshot.getValue(Mensagem.class);
                        ToastCustomizado.toastCustomizadoCurto("Diferente de nulo", getApplicationContext());
                        int totalMensagens = mensagemContador.getTotalMensagens() + 1;
                        verificaMensagemDestinatarioRef.child("totalMensagens").setValue(totalMensagens);
                    } else {
                        verificaMensagemDestinatarioRef.child("totalMensagens").setValue(1);
                    }
                    verificaMensagemDestinatarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                    .push().setValue(mensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });

            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                    .push().setValue(mensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });
        }
    }

    private void exibirDadosDestinatario() {
        if (usuarioDestinatario != null) {
            if (usuarioDestinatario.getEpilepsia().equals("Sim")) {
                GlideCustomizado.montarGlideEpilepsia(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                        imgViewFotoDestinatario, android.R.color.transparent);
            } else {
                GlideCustomizado.montarGlide(getApplicationContext(), usuarioDestinatario.getMinhaFoto(),
                        imgViewFotoDestinatario, android.R.color.transparent);
            }
            if (usuarioDestinatario.getExibirApelido().equals("sim")) {
                txtViewNomeDestinatario.setText(usuarioDestinatario.getApelidoUsuario());
            } else {
                txtViewNomeDestinatario.setText(usuarioDestinatario.getNomeUsuario());
            }
            txtViewNivelAmizadeDestinatario.setText("Nível de amizade: " + contatoDestinatario.getNivelAmizade());
            GlideCustomizado.montarGlideFoto(getApplicationContext(), "https://media.giphy.com/media/9dtArMyxofHqXhziUk/giphy.gif",
                    imgViewGifDestinatario, android.R.color.transparent);

            btnTotalMensagensDestinatario.setText(contatoDestinatario.getTotalMensagens() + " Mensagens");
            preencherChipsInteresses();
        }
    }

    private void preencherChipsInteresses() {
        if (usuarioDestinatario.getInteresses().size() == 1) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
        } else if (usuarioDestinatario.getInteresses().size() == 2) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
        } else if (usuarioDestinatario.getInteresses().size() == 3) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
        } else if (usuarioDestinatario.getInteresses().size() == 4) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
            chipInteresse04.setText(usuarioDestinatario.getInteresses().get(3));
        } else if (usuarioDestinatario.getInteresses().size() == 5) {
            chipInteresse01.setText(usuarioDestinatario.getInteresses().get(0));
            chipInteresse02.setText(usuarioDestinatario.getInteresses().get(1));
            chipInteresse03.setText(usuarioDestinatario.getInteresses().get(2));
            chipInteresse04.setText(usuarioDestinatario.getInteresses().get(3));
            chipInteresse05.setText(usuarioDestinatario.getInteresses().get(4));
        }

        imgBtnBackConversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void inicializandoComponentes() {
        toolbarConversa = findViewById(R.id.toolbarConversa);
        imgBtnBackConversa = findViewById(R.id.imgBtnBackConversa);
        btnTotalMensagensDestinatario = findViewById(R.id.btnTotalMensagensDestinatario);
        imgViewFotoDestinatario = findViewById(R.id.imgViewFotoDestinatario);
        imgViewGifDestinatario = findViewById(R.id.imgViewGifDestinatario);
        txtViewNomeDestinatario = findViewById(R.id.txtViewNomeDestinatario);
        txtViewNivelAmizadeDestinatario = findViewById(R.id.txtViewNivelAmizadeDestinatario);
        chipInteresse01 = findViewById(R.id.chipInteresse01);
        chipInteresse02 = findViewById(R.id.chipInteresse02);
        chipInteresse03 = findViewById(R.id.chipInteresse03);
        chipInteresse04 = findViewById(R.id.chipInteresse04);
        chipInteresse05 = findViewById(R.id.chipInteresse05);
        edtTextMensagemChat = findViewById(R.id.edtTextMensagemChat);
        fabEnviarMensagemChat = findViewById(R.id.fabEnviarMensagemChat);
        imgButtonEnviarFotoChat = findViewById(R.id.imgButtonEnviarFotoChat);
        recyclerMensagensChat = findViewById(R.id.recyclerMensagensChat);
    }
}