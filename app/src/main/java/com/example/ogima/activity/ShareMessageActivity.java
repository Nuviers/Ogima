package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterContato;
import com.example.ogima.adapter.AdapterShareMessage;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;

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
    private HashSet<Usuario> listaContatoBuscada = new HashSet<>();

    @Override
    protected void onStart() {
        super.onStart();


    }

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_message);
        inicializandoComponentes();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

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
            adapterShareMessage = new AdapterShareMessage(listaContato, getApplicationContext(), txtViewContadorSelecao);
        }
        recyclerShare.setAdapter(adapterShareMessage);

        recuperarContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);

        buscarContatos(null);
        configuracaoSearchView();

    }

    private void inicializandoComponentes() {
        recyclerShare = findViewById(R.id.recyclerShare);
        searchViewShare = findViewById(R.id.searchViewShare);
        txtViewContadorSelecao = findViewById(R.id.txtViewContadorSelecao);
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
                                if (somenteFavorito != null) {
                                    if (contatos.getContatoFavorito().equals("sim")) {
                                        usuario.setContatoFavorito(contatos.getContatoFavorito());
                                        listaContato.add(usuario);
                                        adapterShareMessage.notifyDataSetChanged();
                                    }
                                } else {
                                    usuario.setContatoFavorito(contatos.getContatoFavorito());
                                    listaContato.add(usuario);
                                    adapterShareMessage.notifyDataSetChanged();
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
            String apelidoUsuario = usuario.getApelidoUsuario().toLowerCase(Locale.ROOT);
            if (nomeUsuario.startsWith(dadoDigitado) || apelidoUsuario.startsWith(dadoDigitado)) {
                listaContatoBuscada.add(usuario);
            }
        }
        atualizarListaContatoBuscado();
    }

    private void atualizarListaContatoBuscado() {
        adapterShareMessage = new AdapterShareMessage(listaContatoBuscada, getApplicationContext(), txtViewContadorSelecao);
        recyclerShare.setAdapter(adapterShareMessage);
        adapterShareMessage.notifyDataSetChanged();
    }

    private void listaContatoOriginal() {
        adapterShareMessage = new AdapterShareMessage(listaContato, getApplicationContext(), txtViewContadorSelecao);
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
}