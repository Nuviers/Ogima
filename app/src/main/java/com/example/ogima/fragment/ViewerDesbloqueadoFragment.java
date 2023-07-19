package com.example.ogima.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterProfileViews;
import com.example.ogima.adapter.AdapterViewersDesbloqueados;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewerDesbloqueadoFragment extends Fragment implements AdapterViewersDesbloqueados.AnimacaoIntent, AdapterViewersDesbloqueados.RecuperaPosicaoAnterior {

    private String idUsuario, emailUsuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private RecyclerView recyclerView;
    private AdapterViewersDesbloqueados adapterViewers;
    private List<Usuario> listaViewers = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private long lastTimestamp = -1;
    private static int PAGE_SIZE = 10; // mudar para 10
    private int mCurrentPosition = -1;
    //isso impede de chamar dados quando já exitem dados que estão sendo carregados.
    private boolean isLoading = false;
    //Flag para indicar se o usuário está interagindo com o scroll.
    private boolean isScrolling = false;
    private Set<String> idsUsuarios = new HashSet<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private Query queryInicial, queryLoadMore;
    private ChildEventListener childListenerInicio, childListenerLoadMore;
    private boolean primeiroCarregamento = true;
    private RecyclerView.OnScrollListener scrollListener;
    private ProgressDialog progressDialog;
    private int qntMore = 0;
    private boolean existemDados = false;

    public ViewerDesbloqueadoFragment() {
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (primeiroCarregamento) {
            configRecycler();
            usuarioDiffDAO = new UsuarioDiffDAO(listaViewers, adapterViewers);
            setLoading(true);
            recuperarDadosIniciais();
            configPaginacao();
            primeiroCarregamento = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapterViewers != null && linearLayoutManager != null
                && mCurrentPosition == -1) {
            mCurrentPosition = linearLayoutManager.findFirstVisibleItemPosition();
            //ToastCustomizado.toastCustomizadoCurto("Find " + mCurrentPosition, getApplicationContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // rola o RecyclerView para a posição salva
        if (mCurrentPosition != -1 &&
                listaViewers != null && listaViewers.size() > 0
                && linearLayoutManager != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Atraso de 100 millissegundos para renderizar o recyclerview
                    recyclerView.scrollToPosition(mCurrentPosition);
                }
            }, 100);
        }
        mCurrentPosition = -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (childListenerInicio != null) {
            queryInicial.removeEventListener(childListenerInicio);
            childListenerInicio = null;
        }

        if (childListenerLoadMore != null) {
            queryLoadMore.removeEventListener(childListenerLoadMore);
            childListenerLoadMore = null;
        }

        usuarioDiffDAO.limparListaUsuarios();
        idsUsuarios.clear();

        mCurrentPosition = -1;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_position", mCurrentPosition);
    }

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt("current_position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewer_desbloqueado, container, false);
        inicializandoComponentes(view);
        dadosUserAtual();
        return view;
    }

    private void dadosUserAtual() {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                adapterViewers.setStatusEpilepsia(epilepsia);
            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String mensagem) {

            }
        });
    }

    private void configRecycler() {
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        if (adapterViewers == null) {
            adapterViewers = new AdapterViewersDesbloqueados(requireContext(),
                    listaViewers, this, this);
        }

        recyclerView.setAdapter(adapterViewers);
    }

    private void recuperarDadosIniciais() {

        if (listaViewers != null && listaViewers.size() >= 1) {
            return;
        }

        queryInicial = firebaseRef.child("profileViews")
                .child(idUsuario).orderByChild("viewLiberada")
                .equalTo(true);

        queryInicial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Usuario usuarioInicial = snapshot1.getValue(Usuario.class);
                        adicionarViewer(usuarioInicial);
                        lastTimestamp = usuarioInicial.getTimeStampView();
                    }
                }
                queryInicial.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastTimestamp = -1;
            }
        });
    }

    private void adicionarViewer(Usuario usuario) {
        //ToastCustomizado.toastCustomizadoCurto("Inicio",getApplicationContext());

        if (listaViewers != null && listaViewers.size() >= 1) {
            return;
        }

        usuarioDiffDAO.adicionarUsuario(usuario);
        idsUsuarios.add(usuario.getIdUsuario());
        adapterViewers.updateViewersList(listaViewers);
        existemDados = true;
        setLoading(false);
        //ToastCustomizado.toastCustomizado("Size lista: " + listaUsuarios.size(), getApplicationContext());
    }

    private void configPaginacao() {

        if (recyclerView != null) {
            isScrolling = true;
            scrollListener = new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        isScrolling = true;
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (linearLayoutManager != null) {
                        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                if (isLoading()) {
                                    return;
                                }

                                int totalItemCount = linearLayoutManager.getItemCount();

                                if (isScrolling && lastVisibleItemPosition == totalItemCount - 1) {

                                    isScrolling = false;

                                    //*progressBarLoading.setVisibility(View.VISIBLE);

                                    setLoading(true);

                                    qntMore = 0;

                                    // o usuário rolou até o final da lista, exibe mais cinco itens
                                    carregarMaisDados();
                                }
                            }
                        }, 100);
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
    }

    private void carregarMaisDados() {
        if (existemDados) {
            if (lastTimestamp == -1) {
                return;
            }

            if (listaViewers != null && qntMore >= 10) {
                PAGE_SIZE += qntMore;
                setLoading(false);
                return;
            }

            setLoading(true);

            ToastCustomizado.toastCustomizado("LOAD MORE", requireContext());

            queryLoadMore = firebaseRef.child("profileViews")
                    .child(idUsuario).orderByChild("viewLiberada")
                    .equalTo(true).limitToFirst(PAGE_SIZE + qntMore);

            queryLoadMore.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.getValue() != null) {
                                Usuario usuarioMore = snapshot1.getValue(Usuario.class);
                                List<Usuario> newUsuario = new ArrayList<>();
                                long key = usuarioMore.getTimeStampView();
                                //*ToastCustomizado.toastCustomizadoCurto("existe " + key, getApplicationContext());
                                if (lastTimestamp != -1 && key != -1 && key != lastTimestamp) {
                                    newUsuario.add(usuarioMore);
                                    lastTimestamp = key;
                                }

                                // Remove a última chave usada
                                if (newUsuario.size() > PAGE_SIZE) {
                                    newUsuario.remove(0);
                                }

                                if (lastTimestamp != -1) {
                                    adicionarMaisDados(newUsuario);
                                }
                            }else{
                                qntMore++;
                            }
                        }
                    } else {
                        qntMore++;
                    }
                    queryLoadMore.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    lastTimestamp = -1;
                }
            });
        }
    }

    private void adicionarMaisDados(List<Usuario> newUsuario) {

        if (listaViewers != null && qntMore >= 10) {
            PAGE_SIZE += qntMore;
            setLoading(false);
            return;
        }

        if (newUsuario != null && newUsuario.size() >= 1) {
            usuarioDiffDAO.carregarMaisUsuario(newUsuario, idsUsuarios);
            Usuario usuarioComparator = new Usuario(true, false);
            Collections.sort(listaViewers, usuarioComparator);
            adapterViewers.updateViewersList(listaViewers);
            ToastCustomizado.toastCustomizadoCurto("Mais dados", requireContext());
            setLoading(false);
        }
    }

    private boolean isLoading() {
        return isLoading;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
    }

    private void inicializandoComponentes(View view){
        recyclerView = view.findViewById(R.id.recyclerViewDesbloqueados);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {
        if (posicaoAnterior != -1) {
            ToastCustomizado.toastCustomizado("Position anterior: " + posicaoAnterior, requireContext());
            mCurrentPosition = posicaoAnterior;
        }
    }

    @Override
    public void onExecutarAnimacao() {
        requireActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void ordenarLista() {
        Collections.sort(listaViewers, new Comparator<Usuario>() {
            @Override
            public int compare(Usuario usuario1, Usuario usuario2) {
                // Ordenar em ordem crescente com base no timeStampCompra
                return Long.compare(usuario1.getTimeStampView(), usuario2.getTimeStampView());
            }
        });
    }
}