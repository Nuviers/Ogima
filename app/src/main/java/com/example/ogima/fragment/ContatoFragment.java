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
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.activity.ChatInicioActivity;
import com.example.ogima.adapter.AdapterContato;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.OnChipGroupClearListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Contatos;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class ContatoFragment extends Fragment implements OnChipGroupClearListener {

    private ChipGroup chipGroupContato;
    private Chip chipContatoFavoritos, chipContatoAmigos, chipContatoSeguidores,
            chipContatoSeguindo;
    private HashSet<Usuario> listaContato = new HashSet<>();
    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private RecyclerView recyclerContato;
    private AdapterContato adapterContato;

    //Reajuste
    private ValueEventListener valueEventListenerUsuario;
    private ChildEventListener childEventListenerContato;
    private DatabaseReference recuperarContatosRef, verificaUsuarioRef;

    private DatabaseReference verificaAmigoRef;
    private ValueEventListener valueEventListenerAmigo;

    private SearchView searchViewContato;
    private HashSet<Usuario> listaContatoBuscada = new HashSet<>();

    public ContatoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        buscarAmigos();
        buscarContatos(null);

        //SearchViewChat
        searchViewContato.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewContato.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
                    listaContatoOriginal();
                }
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (chipGroupContato.getCheckedChipId() != -1) {
            chipGroupContato.clearCheck();
        }

        removerListeners();
        listaContato.clear();

        searchViewContato.setQuery("", false);
        searchViewContato.setIconified(true);
        searchViewContato.setOnQueryTextListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contato, container, false);

        inicializarComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        chipGroupContato.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                limparSearchContato();
                if (chipContatoFavoritos.isChecked()) {
                    buscarContatos("sim");
                } else {
                    buscarContatos(null);
                }
            }
        });

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerContato.setHasFixedSize(true);
        recyclerContato.setLayoutManager(linearLayoutManager);

        if (adapterContato != null) {

        } else {
            adapterContato = new AdapterContato(listaContato, getContext());
        }
        recyclerContato.setAdapter(adapterContato);

        verificaAmigoRef = firebaseRef.child("friends")
                .child(idUsuario);

        recuperarContatosRef = firebaseRef.child("contatos")
                .child(idUsuario);

        return view;
    }


    private void buscarAmigos() {

        if (valueEventListenerAmigo != null) {
            verificaAmigoRef.removeEventListener(valueEventListenerAmigo);
            valueEventListenerAmigo = null;
        }

        valueEventListenerAmigo = verificaAmigoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //somente se for amigo
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Usuario usuarioFriend = snapshot1.getValue(Usuario.class);

                        //ToastCustomizado.toastCustomizadoCurto("Id amigo " + usuarioFriend.getIdUsuario(), getContext());

                        //Verifica se o amigo existe nos contatos
                        DatabaseReference verificaContatoNovoRef = firebaseRef.child("contatos")
                                .child(idUsuario).child(usuarioFriend.getIdUsuario());

                        verificaContatoNovoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    //Já existe o contato
                                    Contatos contatos = snapshot.getValue(Contatos.class);
                                    //ToastCustomizado.toastCustomizadoCurto("Contato existe " + contatos.getIdContato(), getContext());
                                } else {
                                    //Adiciona o amigo aos contatos
                                    //ToastCustomizado.toastCustomizadoCurto("Não existe contato " + usuarioFriend.getIdUsuario(), getContext());

                                    //Verifica se existe conversa

                                    DatabaseReference verificaConversaRef = firebaseRef.child("contadorMensagens")
                                            .child(idUsuario).child(usuarioFriend.getIdUsuario());

                                    HashMap<String, Object> dadosContatoDestinatario = new HashMap<>();
                                    dadosContatoDestinatario.put("idContato", idUsuario);
                                    dadosContatoDestinatario.put("contatoFavorito", "não");

                                    verificaConversaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                //Existe uma conversa mesmo não sendo amigos
                                                Contatos contatosContador = snapshot.getValue(Contatos.class);
                                                //Recupera o total de mensagens
                                                dadosContatoDestinatario.put("totalMensagens", contatosContador.getTotalMensagens());
                                                if (contatosContador.getTotalMensagens() >= 10) {
                                                    dadosContatoDestinatario.put("nivelAmizade", "grandesAmigos");
                                                } else {
                                                    dadosContatoDestinatario.put("nivelAmizade", "Ternura");
                                                }
                                            } else {
                                                //Não existe conversa entre eles
                                                dadosContatoDestinatario.put("totalMensagens", 0);
                                                dadosContatoDestinatario.put("nivelAmizade", "Ternura");
                                            }
                                            verificaConversaRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    DatabaseReference adicionarAoDestinatarioRef = firebaseRef.child("contatos")
                                            .child(usuarioFriend.getIdUsuario()).child(idUsuario);
                                    adicionarAoDestinatarioRef.setValue(dadosContatoDestinatario);
                                    //

                                    DatabaseReference verificaConversaDestinatarioRef = firebaseRef.child("contadorMensagens")
                                            .child(usuarioFriend.getIdUsuario()).child(idUsuario);

                                    HashMap<String, Object> dadosContato = new HashMap<>();
                                    dadosContato.put("idContato", usuarioFriend.getIdUsuario());
                                    dadosContato.put("contatoFavorito", "não");

                                    verificaConversaDestinatarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.getValue() != null) {
                                                Contatos contatosDestinatario = snapshot.getValue(Contatos.class);
                                                if (contatosDestinatario.getTotalMensagens() >= 10) {
                                                    dadosContato.put("nivelAmizade", "grandesAmigos");
                                                } else {
                                                    dadosContato.put("nivelAmizade", "Ternura");
                                                }
                                                dadosContato.put("totalMensagens", contatosDestinatario.getTotalMensagens());
                                            } else {
                                                dadosContato.put("nivelAmizade", "Ternura");
                                                dadosContato.put("totalMensagens", 0);
                                            }
                                            verificaConversaDestinatarioRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    DatabaseReference adicionarAoAtualRef = firebaseRef.child("contatos")
                                            .child(idUsuario).child(usuarioFriend.getIdUsuario());

                                    adicionarAoAtualRef.setValue(dadosContato);

                                    //Ajustar esses dois dados
                                    //1 - verifica se existe o nó de conversa
                                    //2 - cria uma lógica de tantas mensagens é tal nivel de amizade
                                    //3 - colocar esses dados verificados ao nó caso somente o nó de contato.
                                    //não exista e se não tem nos dois nós coloca zerado o total e nivelAmizade ternura
                                }
                                verificaContatoNovoRef.removeEventListener(this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        adapterContato.notifyDataSetChanged();

        childEventListenerContato = recuperarContatosRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Contatos contatos = snapshot.getValue(Contatos.class);
                    if (contatos.getContatoFavorito().equals("sim")) {
                        chipContatoFavoritos.setVisibility(View.VISIBLE);
                    }
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
                                        adapterContato.notifyDataSetChanged();
                                    }
                                } else {
                                    usuario.setContatoFavorito(contatos.getContatoFavorito());
                                    listaContato.add(usuario);
                                    adapterContato.notifyDataSetChanged();
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
                DatabaseReference recuperaUsuarioExluidoRef = firebaseRef.child("usuarios")
                        .child(snapshot.getKey());
                recuperaUsuarioExluidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            Usuario usuarioExcluido = snapshot.getValue(Usuario.class);
                            adapterContato.removerItemConversa(usuarioExcluido);
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

    private void limparSearchContato() {
        searchViewContato.setQuery("", false);
    }

    private void atualizarListaContatoBuscado() {
        adapterContato = new AdapterContato(listaContatoBuscada, getContext());
        recyclerContato.setAdapter(adapterContato);
        adapterContato.notifyDataSetChanged();
    }

    private void listaContatoOriginal() {
        adapterContato = new AdapterContato(listaContato, getContext());
        recyclerContato.setAdapter(adapterContato);
        adapterContato.notifyDataSetChanged();
    }

    private void inicializarComponentes(View view) {
        chipGroupContato = view.findViewById(R.id.chipGroupContato);
        chipContatoFavoritos = view.findViewById(R.id.chipContatoFavoritos);
        chipContatoAmigos = view.findViewById(R.id.chipContatoAmigos);
        chipContatoSeguindo = view.findViewById(R.id.chipContatoSeguindo);
        chipContatoSeguidores = view.findViewById(R.id.chipContatoSeguidores);
        recyclerContato = view.findViewById(R.id.recyclerContato);
        searchViewContato = view.findViewById(R.id.searchViewContato);
    }

    private void removerListeners() {
        if (valueEventListenerAmigo != null) {
            verificaAmigoRef.removeEventListener(valueEventListenerAmigo);
            valueEventListenerAmigo = null;
        }
        if (childEventListenerContato != null) {
            recuperarContatosRef.removeEventListener(childEventListenerContato);
            childEventListenerContato = null;
        }
        if (valueEventListenerUsuario != null) {
            verificaUsuarioRef.removeEventListener(valueEventListenerUsuario);
            valueEventListenerUsuario = null;
        }
        if (adapterContato.listenerAdapterContato != null) {
            adapterContato.verificaContatoRef.removeEventListener(adapterContato.listenerAdapterContato);
            adapterContato.listenerAdapterContato = null;
        }

        if (adapterContato.listenerConversaContador != null) {
            adapterContato.verificaConversaContadorRef.removeEventListener(adapterContato.listenerConversaContador);
            adapterContato.listenerConversaContador = null;
        }
    }

    @Override
    public void onClearChipGroup() {
        chipGroupContato.clearCheck();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (searchViewContato != null) {
                searchViewContato.setQuery("", false);
                searchViewContato.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchViewContato.getWindowToken(), 0);
            }
        }
    }
}