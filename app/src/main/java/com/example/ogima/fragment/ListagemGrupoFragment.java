package com.example.ogima.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.AddGroupUsersActivity;
import com.example.ogima.adapter.AdapterChatGrupo;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GrupoDAO;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListagemGrupoFragment extends Fragment {

    private String emailUsuario, idUsuario;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private RecyclerView recyclerChat;
    private AdapterChatGrupo adapterChatGrupo;
    private SearchView searchViewChat;
    private Button btnCadastroGrupo;
    private ImageButton imgButtonCadastroGrupo;
    private ChildEventListener childEventListener;
    private DatabaseReference grupoRef;

    private GrupoDAO grupoDAO;
    private List<Grupo> listaGrupos = new ArrayList<>();

    private HashMap<String, Integer> hashMapGrupoPesquisa = new HashMap<>();
    private List<Grupo> listaGruposPesquisa = new ArrayList<>();

    private int position;

    private Boolean pesquisaAtivada = false;

    private Snackbar snackbarLimiteGrupo;

    private final static int MAX_MY_GROUPS = 5;

    public ListagemGrupoFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

        recuperarGrupos();

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
                    pesquisaAtivada = true;
                    listaGruposPesquisa.clear();
                    hashMapGrupoPesquisa.clear();
                    pesquisarGrupos(dadoDigitadoFormatado);
                    adapterChatGrupo = new AdapterChatGrupo(getContext(), listaGruposPesquisa);
                    recyclerChat.setAdapter(adapterChatGrupo);
                    //ToastCustomizado.toastCustomizadoCurto("Com filtro", getContext());
                } else {
                    pesquisaAtivada = false;
                    listaGruposPesquisa.clear();
                    hashMapGrupoPesquisa.clear();
                    recuperarGrupos();
                    adapterChatGrupo = new AdapterChatGrupo(getContext(), grupoDAO.listarGrupos());
                    recyclerChat.setAdapter(adapterChatGrupo);
                    adapterChatGrupo.notifyDataSetChanged();
                    //ToastCustomizado.toastCustomizadoCurto("Sem filtro", getContext());
                }
                return true;
            }
        });
    }

    private void pesquisarGrupos(String dadoDigitadoFormatado) {

        for (Grupo grupoPesquisado : grupoDAO.listarGrupos()) {
            String nomeGrupo = grupoPesquisado.getNomeGrupo().toLowerCase();

            if (nomeGrupo.startsWith(dadoDigitadoFormatado)) {
                if (!hashMapGrupoPesquisa.containsKey(grupoPesquisado.getIdGrupo())) {
                    listaGruposPesquisa.add(grupoPesquisado);
                    hashMapGrupoPesquisa.put(grupoPesquisado.getIdGrupo(), listaGruposPesquisa.size() - 1);
                    position = listaGruposPesquisa.indexOf(grupoPesquisado);
                    adapterChatGrupo.notifyItemInserted(position);
                } else {
                    listaGruposPesquisa.set(hashMapGrupoPesquisa.get(grupoPesquisado.getIdGrupo()), grupoPesquisado);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        removerListener();

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
        View view = inflater.inflate(R.layout.fragment_chat_grupo, container, false);
        inicializarComponentes(view);

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);

        grupoDAO = new GrupoDAO(listaGrupos, getContext());

        //Configurações do recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerChat.setHasFixedSize(true);
        recyclerChat.setLayoutManager(linearLayoutManager);

        if (adapterChatGrupo != null) {

        } else {
            adapterChatGrupo = new AdapterChatGrupo(getContext(), grupoDAO.listarGrupos());
        }
        recyclerChat.setAdapter(adapterChatGrupo);

        imgButtonCadastroGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrarGrupo();
            }
        });

        /*
        btnCadastroGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrarGrupo();
            }
        });

         */

        return view;
    }

    private void limparSearchChat() {
        searchViewChat.setQuery("", false);
    }

    private void cadastrarGrupo() {
        FirebaseRecuperarUsuario.recuperaUsuario(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeAjustado, Boolean epilepsia) {
                if (usuarioAtual.getIdMeusGrupos() != null
                        && usuarioAtual.getIdMeusGrupos().size() >= MAX_MY_GROUPS) {
                    snackbarLimiteGrupo = Snackbar.make(imgButtonCadastroGrupo, "Limite de criação de grupos atingido, por favor exclua um deles para que seja possível criar um novo grupo", Snackbar.LENGTH_LONG);
                    View snackbarView = snackbarLimiteGrupo.getView();
                    TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    textView.setMaxLines(5); // altera o número máximo de linhas exibidas
                    snackbarLimiteGrupo.show();
                    ToastCustomizado.toastCustomizado("Você já atingiu o limite de grupos que são 5 grupos por usuário, por favor exclua um deles para que seja possível a criação de um novo grupo", getContext());
                } else {
                    Intent intent = new Intent(getContext(), AddGroupUsersActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void recuperarGrupos() {

        grupoRef = firebaseRef.child("grupos");

        childEventListener = grupoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo grupo = snapshot.getValue(Grupo.class);
                    if (grupo != null && grupo.getParticipantes() != null
                            && !grupo.getParticipantes().isEmpty()
                            && grupo.getParticipantes().contains(idUsuario)) {
                        //Somente traz grupos onde o usuário atual é participante
                        grupoDAO.adicionarGrupo(grupo, adapterChatGrupo);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.getValue() != null) {
                    Grupo grupoAlterado = snapshot.getValue(Grupo.class);
                    grupoDAO.atualizarGrupo(grupoAlterado, adapterChatGrupo);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Grupo grupoRemovido = snapshot.getValue(Grupo.class);
                if (grupoRemovido.getParticipantes().contains(idUsuario)) {
                    //Somente notifica a exclusão de grupos que o usuário atual
                    //é participante.
                    grupoDAO.removerGrupo(grupoRemovido, adapterChatGrupo);

                    if (pesquisaAtivada) {
                        if (hashMapGrupoPesquisa.containsKey(grupoRemovido.getIdGrupo())) {
                            int posicao = hashMapGrupoPesquisa.get(grupoRemovido.getIdGrupo());
                            if (posicao != -1) {
                                listaGruposPesquisa.remove(posicao);
                                hashMapGrupoPesquisa.remove(grupoRemovido.getIdGrupo());
                                adapterChatGrupo.notifyItemRemoved(posicao);

                                // Atualiza as posições dos usuários no HashMap após a remoção
                                for (int i = posicao; i < listaGruposPesquisa.size(); i++) {
                                    Grupo grupoAtualizado = listaGruposPesquisa.get(i);
                                    hashMapGrupoPesquisa.put(grupoAtualizado.getIdGrupo(), i);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void inicializarComponentes(View view) {
        recyclerChat = view.findViewById(R.id.recyclerViewLstChatGrupo);
        searchViewChat = view.findViewById(R.id.searchViewLstChatGrupo);
        imgButtonCadastroGrupo = view.findViewById(R.id.imgButtonCadastroGrupo);
        //**btnCadastroGrupo = view.findViewById(R.id.btnCadastroGrupo);
        imgButtonCadastroGrupo = view.findViewById(R.id.imgButtonCadastroGrupo);
    }

    private void removerListener() {
        if (childEventListener != null) {
            grupoRef.removeEventListener(childEventListener);
            childEventListener = null;
        }

        if (adapterChatGrupo.valueEventListenerLastMsg != null) {
            adapterChatGrupo.verificaUltimaMsgRef.removeEventListener(adapterChatGrupo.valueEventListenerLastMsg);
            adapterChatGrupo.valueEventListenerLastMsg = null;
        }
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