package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import java.util.HashMap;
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
    private LinearLayout linearInfosDestinatario;
    private ImageView imgViewRecolherInfo, imgViewExpandirInfo;

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

    @RequiresApi(api = Build.VERSION_CODES.Q)
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

        PopupMenu popupMenu = new PopupMenu(getApplicationContext(),imgButtonEnviarFotoChat);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_anexo, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.anexoCamera:
                        ToastCustomizado.toastCustomizadoCurto("Câmera",getApplicationContext());
                        return true;
                    case R.id.anexoGaleria:
                        ToastCustomizado.toastCustomizadoCurto("Galeria",getApplicationContext());
                        return true;
                    case R.id.anexoDocumento:
                        ToastCustomizado.toastCustomizadoCurto("Documento",getApplicationContext());
                        return true;
                    case R.id.anexoGif:
                        ToastCustomizado.toastCustomizadoCurto("Gif",getApplicationContext());
                        return true;
                }
                return false;
            }
        });

        imgButtonEnviarFotoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });

        imgViewRecolherInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearInfosDestinatario.setVisibility(View.GONE);
                imgViewExpandirInfo.setVisibility(View.VISIBLE);
            }
        });

        imgViewExpandirInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgViewExpandirInfo.setVisibility(View.GONE);
                linearInfosDestinatario.setVisibility(View.VISIBLE);
            }
        });

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
            HashMap<String, Object> dadosMensagem = new HashMap<>();
            dadosMensagem.put("tipoMensagem","texto");
            dadosMensagem.put("idRemetente",idUsuario);
            dadosMensagem.put("idDestinatario",usuarioDestinatario.getIdUsuario());
            dadosMensagem.put("conteudoMensagem",conteudoMensagem);

            if (localConvertido.equals("pt_BR")) {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                date = new Date();
                String novaData = dateFormat.format(date);
                dadosMensagem.put("dataMensagem", novaData);
                dadosMensagem.put("dataMensagemCompleta", date);
            } else {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                date = new Date();
                String novaData = dateFormat.format(date);
                dadosMensagem.put("dataMensagem", novaData);
                dadosMensagem.put("dataMensagemCompleta", date);
            }

            DatabaseReference salvarMensagem = firebaseRef.child("conversas");

            salvarMensagem.child(idUsuario).child(usuarioDestinatario.getIdUsuario())
                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ToastCustomizado.toastCustomizadoCurto("Enviado com sucesso", getApplicationContext());
                                atualizarContador();
                                edtTextMensagemChat.setText("");
                            }
                        }
                    });

            salvarMensagem.child(usuarioDestinatario.getIdUsuario()).child(idUsuario)
                    .push().setValue(dadosMensagem).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private void atualizarContador() {
        DatabaseReference verificaContadorRef = firebaseRef.child("contadorMensagens")
                .child(idUsuario)
                .child(usuarioDestinatario.getIdUsuario());
        DatabaseReference verificaContadorDestinatarioRef = firebaseRef.child("contadorMensagens")
                .child(usuarioDestinatario.getIdUsuario())
                .child(idUsuario);
        verificaContadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Mensagem mensagem1 = snapshot.getValue(Mensagem.class);
                    ToastCustomizado.toastCustomizadoCurto("Total " + mensagem1.getTotalMensagens(), getApplicationContext());
                    verificaContadorRef.child("totalMensagens").setValue(mensagem1.getTotalMensagens() + 1);
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(mensagem1.getTotalMensagens() + 1);
                }else{
                    ToastCustomizado.toastCustomizadoCurto("primeiro", getApplicationContext());
                    verificaContadorRef.child("totalMensagens").setValue(1);
                    verificaContadorDestinatarioRef.child("totalMensagens").setValue(1);
                }
                verificaContadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        linearInfosDestinatario = findViewById(R.id.linearInfosDestinatario);
        imgViewRecolherInfo = findViewById(R.id.imgViewRecolherInfo);
        imgViewExpandirInfo = findViewById(R.id.imgViewExpandirInfo);
    }
}