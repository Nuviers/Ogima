package com.example.ogima.fragment;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterChat;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.OnChipGroupClearListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
import com.example.ogima.model.Mensagem;
import com.example.ogima.model.Usuario;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements OnChipGroupClearListener {

    private ChipGroup chipGroupChat;
    private Chip chipChatFavoritos, chipChatAmigos, chipChatSeguidores, chipChatSeguindo;
    private List<Usuario> listaChat = new ArrayList<>();
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerChat;
    private AdapterChat adapterChat;

    private DatabaseReference verificaConversasRef, recuperaUsuarioRef,
            recuperaDataMensagemRef;

    //Filtragem Favorito
    private HashSet<Usuario> listaChatSemDuplicatas = new HashSet<>();

    private DatabaseReference filtroFavoritoRef, filtroAmigoRef,
            filtroSeguidorRef, filtroSeguindoRef;

    private ChildEventListener childListenerConversa;

    private SearchView searchViewChat;
    private List<Usuario> listaConversaBuscada = new ArrayList<>();

    public ChatFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
        recuperaConversas(null);


        //SearchViewChat
        searchViewChat.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewChat.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                    pesquisarConversas(dadoDigitadoFormatado);
                } else {
                    if (listaConversaBuscada != null) {
                        listaConversaBuscada.clear();
                    }
                    if (listaChat != null) {
                        listaChatSemBusca();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        //Caso algum chip esteja marcado, ele vai desmarcar todos.
        if (chipGroupChat.getCheckedChipId() != -1) {
            chipGroupChat.clearCheck();
        }

        removerListeners();

        listaChat.clear();
        listaChatSemDuplicatas.clear();

        searchViewChat.setQuery("", false);
        searchViewChat.setIconified(true);
        if (searchViewChat.getOnFocusChangeListener() != null) {
            searchViewChat.setOnQueryTextListener(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        inicializarComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerChat.setLayoutManager(linearLayoutManager);
        recyclerChat.setHasFixedSize(true);

        if (adapterChat != null) {

        } else {
            adapterChat = new AdapterChat(listaChat, getContext());
        }
        recyclerChat.setAdapter(adapterChat);

        chipGroupChat.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                limparSearchChat();
                if (chipChatFavoritos.isChecked()) {
                    recuperaConversas("favoritos");
                    //ToastCustomizado.toastCustomizado("Check Favoritos ", getContext());
                } else if (chipChatAmigos.isChecked()) {
                    recuperaConversas("amigos");
                    //ToastCustomizado.toastCustomizado("Check Amigos ", getContext());
                } else if (chipChatSeguidores.isChecked()) {
                    recuperaConversas("seguidores");
                    //ToastCustomizado.toastCustomizado("Check Seguidores ", getContext());
                } else if (chipChatSeguindo.isChecked()) {
                    recuperaConversas("seguindo");
                    //ToastCustomizado.toastCustomizado("Check Seguindo ", getContext());
                } else {
                    recuperaConversas(null);
                }
            }
        });

        verificaConversasRef = firebaseRef.child("conversas")
                .child(idUsuario);

        return view;
    }

    private void recuperaConversas(String filtragem) {

        if (childListenerConversa != null) {
            verificaConversasRef.removeEventListener(childListenerConversa);
            childListenerConversa = null;
        }

        listaChat.clear();
        listaChatSemDuplicatas.clear();
        adapterChat.notifyDataSetChanged();

        childListenerConversa = verificaConversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String idDestinatario = snapshot.getKey();
                if (idDestinatario != null) {
                    if (filtragem != null) {
                        dadosUsuario(idDestinatario, filtragem);
                    } else {
                        dadosUsuario(idDestinatario, null);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                DatabaseReference recuperaUsuarioExluidoRef = firebaseRef.child("usuarios")
                        .child(snapshot.getKey());
                recuperaUsuarioExluidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioExcluido = snapshot.getValue(Usuario.class);
                            adapterChat.removerItemConversa(usuarioExcluido);
                        }
                        recuperaUsuarioExluidoRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void dadosUsuario(String idUsuarioChat, String filtragem) {

        recuperaUsuarioRef = firebaseRef.child("usuarios")
                .child(idUsuarioChat);
        recuperaUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    recuperaDataMensagemRef = firebaseRef.child("conversas")
                            .child(idUsuario).child(usuario.getIdUsuario());
                    recuperaDataMensagemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapTeste : snapshot.getChildren()) {
                                Mensagem mensagemTeste = snapTeste.getValue(Mensagem.class);
                                usuario.setDataMensagemCompleta(mensagemTeste.getDataMensagemCompleta());
                            }

                            if (filtragem != null) {
                                if (filtragem.equals("favoritos")) {
                                    filtraFavorito(usuario, usuario.getIdUsuario());
                                } else if (filtragem.equals("amigos")) {
                                    filtrarAmigos(usuario, usuario.getIdUsuario());
                                } else if (filtragem.equals("seguidores")) {
                                    filtrarSeguidores(usuario, usuario.getIdUsuario());
                                } else if (filtragem.equals("seguindo")) {
                                    filtrarSeguindo(usuario, usuario.getIdUsuario());
                                }
                            } else {
                                adicionarNovosDados(listaChatSemDuplicatas, usuario);
                            }
                            recuperaDataMensagemRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                recuperaUsuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filtraFavorito(Usuario usuarioRecebido, String idDestinatario) {

        filtroFavoritoRef = firebaseRef.child("contatos")
                .child(idUsuario).child(idDestinatario);

        filtroFavoritoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Contatos contatoFavorito = snapshot.getValue(Contatos.class);
                    //Verifica se é favorito
                    if (contatoFavorito.isContatoFavorito()) {
                        adicionarNovosDados(listaChatSemDuplicatas, usuarioRecebido);
                    }
                }
                filtroFavoritoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filtrarAmigos(Usuario usuarioRecebido, String idDestinatario) {

        filtroAmigoRef = firebaseRef.child("friends")
                .child(idUsuario).child(idDestinatario);

        filtroAmigoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioAmigo = snapshot.getValue(Usuario.class);
                    if (idDestinatario.equals(usuarioAmigo.getIdUsuario())) {
                        adicionarNovosDados(listaChatSemDuplicatas, usuarioRecebido);
                    }
                }
                filtroAmigoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void filtrarSeguidores(Usuario usuarioRecebido, String idDestinatario) {

        filtroSeguidorRef = firebaseRef.child("seguidores")
                .child(idUsuario).child(idDestinatario);

        filtroSeguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //Caso o id do usuário atual esteja nesse child então logo
                    //usuário atual é seguidor desse usuárioSeguidor
                    adicionarNovosDados(listaChatSemDuplicatas, usuarioRecebido);
                }
                filtroSeguidorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void filtrarSeguindo(Usuario usuarioRecebido, String idDestinatario) {

        filtroSeguindoRef = firebaseRef.child("seguindo")
                .child(idUsuario).child(idDestinatario);

        filtroSeguindoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    adicionarNovosDados(listaChatSemDuplicatas, usuarioRecebido);
                }
                filtroSeguindoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void pesquisarConversas(String dadoDigitado) {

        if (listaConversaBuscada != null) {
            listaConversaBuscada.clear();
        }

        for (Usuario usuario : listaChat) {
            String nomeUsuario = usuario.getNomeUsuario().toLowerCase(Locale.ROOT);
            if (nomeUsuario.startsWith(dadoDigitado)) {
                listaConversaBuscada.add(usuario);
            }
        }
        atualizarListaBuscada();
    }


    private void adicionarNovosDados(HashSet<Usuario> listaSemDuplicatas, Usuario usuarioNovo) {
        listaSemDuplicatas.clear();
        listaChat.add(usuarioNovo);
        listaSemDuplicatas.addAll(listaChat);
        listaChat.clear();
        adapterChat.adicionarUsuario(listaSemDuplicatas);
    }

    private void removerListeners() {

        if (childListenerConversa != null) {
            verificaConversasRef.removeEventListener(childListenerConversa);
            childListenerConversa = null;
        }

        if (adapterChat.listenerContadorMsgRef != null) {
            adapterChat.contadorMsgRef.removeEventListener(adapterChat.listenerContadorMsgRef);
            adapterChat.listenerContadorMsgRef = null;
        }

        if (adapterChat.listenerMensagensAdapterChat != null) {
            adapterChat.mensagensAdapterChatRef.removeEventListener(adapterChat.listenerMensagensAdapterChat);
            adapterChat.listenerMensagensAdapterChat = null;
        }
    }

    private void limparSearchChat() {
        searchViewChat.setQuery("", false);
    }

    private void atualizarListaBuscada() {
        adapterChat = new AdapterChat(listaConversaBuscada, getActivity());
        recyclerChat.setAdapter(adapterChat);
        adapterChat.notifyDataSetChanged();
    }

    private void listaChatSemBusca() {
        adapterChat = new AdapterChat(listaChat, getActivity());
        recyclerChat.setAdapter(adapterChat);
        adapterChat.notifyDataSetChanged();
    }

    @Override
    public void onClearChipGroup() {
        chipGroupChat.clearCheck();
    }

    private void inicializarComponentes(View view) {
        chipGroupChat = view.findViewById(R.id.chipGroupChat);
        chipChatFavoritos = view.findViewById(R.id.chipChatFavoritos);
        chipChatAmigos = view.findViewById(R.id.chipChatAmigos);
        chipChatSeguindo = view.findViewById(R.id.chipChatSeguindo);
        chipChatSeguidores = view.findViewById(R.id.chipChatSeguidores);
        recyclerChat = view.findViewById(R.id.recyclerChat);
        searchViewChat = view.findViewById(R.id.searchViewChat);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (searchViewChat != null) {
                searchViewChat.setQuery("", false);
                searchViewChat.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchViewChat.getWindowToken(), 0);
            }
        }
    }
}