package com.example.ogima.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterDailyShorts;
import com.example.ogima.adapter.AdapterFotosPerfilParc;
import com.example.ogima.adapter.AdapterFuncoesPostagem;
import com.example.ogima.adapter.AdapterInteracaoParc;
import com.example.ogima.adapter.AdapterLogicaFeed;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.OnSwipeListener;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.SwipeItemTouchListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioDiffDAO;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;

import java.util.ArrayList;
import java.util.List;

public class InteracoesParcFragment extends Fragment implements OnSwipeListener, AdapterInteracaoParc.RecuperaPosicaoAnterior, CardStackListener, AdapterInteracaoParc.retomarExibicao {

    private String idUsuario = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private RecyclerView recyclerViewInteracoes;
    private LinearLayoutManager linearLayoutManager;
    private AdapterInteracaoParc adapterInteracaoParc;
    private List<Usuario> listaUsuario = new ArrayList<>();
    private UsuarioDiffDAO usuarioDiffDAO;
    private CardStackView cardStackView;
    private CardStackLayoutManager layoutManager;

    @Override
    public void onRetomar() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutManager.setVisibleCount(2);
            }
        }, 500);
    }

    public interface TesteCallback{
        void onTeste(String outroTeste);
    }

    public InteracoesParcFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        //ToastCustomizado.toastCustomizadoCurto("onCardDragging", requireContext());
    }

    @Override
    public void onCardSwiped(Direction direction) {
        String message = "Cartão arrastado ";

        if (direction == Direction.Top) {
            message += "para cima";
        } else if (direction == Direction.Bottom) {
            message += "para baixo";
        } else if (direction == Direction.Left) {
            message += "para a esquerda";
        } else if (direction == Direction.Right) {
            message += "para a direita";
        } else {
            message += "em uma direção desconhecida";
        }
        ToastCustomizado.toastCustomizadoCurto("onCardSwiped " + message, requireContext());

        if (layoutManager.getTopPosition() == adapterInteracaoParc.getItemCount() - 1) {
            ToastCustomizado.toastCustomizado("PAGINAÇÃO",requireContext());
        }
    }

    @Override
    public void onCardRewound() {
        ToastCustomizado.toastCustomizadoCurto("onCardRewound", requireContext());
    }

    @Override
    public void onCardCanceled() {
        ToastCustomizado.toastCustomizadoCurto("onCardCanceled", requireContext());
    }

    @Override
    public void onCardAppeared(View view, int position) {
        ToastCustomizado.toastCustomizadoCurto("onCardAppeared", requireContext());
    }

    @Override
    public void onCardDisappeared(View view, int position) {
        ToastCustomizado.toastCustomizadoCurto("onCardDisappeared", requireContext());
    }

    public interface DadosUserAtualCallback{
        void onRecuperado(boolean epilepsia);
        void onSemDados();
        void onError(String message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_interacoes_parc, container, false);
        inicializandoComponentes(view);
        verificaEpilepsia(new DadosUserAtualCallback() {
            @Override
            public void onRecuperado(boolean epilepsia) {
                configRecyclerView(epilepsia);

                usuarioDiffDAO = new UsuarioDiffDAO(listaUsuario, adapterInteracaoParc);

                Query recuperarUsersRef = firebaseRef.child("usuarioParc")
                        .orderByChild("idUsuario");
                recuperarUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                usuarioDiffDAO.adicionarUsuario(snapshot1.getValue(Usuario.class));
                            }
                            adapterInteracaoParc.updateUsuarioList(listaUsuario);
                        }
                        recuperarUsersRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onSemDados() {

            }

            @Override
            public void onError(String message) {

            }
        });
        return view;
    }

    @Override
    public void onSwipeLeft(int position) {
        ToastCustomizado.toastCustomizadoCurto("Left",requireContext());
    }

    @Override
    public void onSwipeRight(int position) {
        ToastCustomizado.toastCustomizadoCurto("Right",requireContext());
    }


    private void configRecyclerView(boolean epilepsia){
        // Configurar gerenciador de layout e adaptador do CardStackView
        layoutManager = new CardStackLayoutManager(requireContext(), this);
        layoutManager.setStackFrom(StackFrom.None);
        layoutManager.setVisibleCount(2);
        layoutManager.setTranslationInterval(12.0f);
        layoutManager.setScaleInterval(0.95f);
        layoutManager.setSwipeThreshold(0.3f);
        layoutManager.setMaxDegree(0f);
        layoutManager.setMaxDegree(60.0f);
        layoutManager.setCanScrollVertical(false);
        layoutManager.setDirections(Direction.HORIZONTAL);
        adapterInteracaoParc = new AdapterInteracaoParc(requireContext(),
                listaUsuario, this, this, layoutManager);
        adapterInteracaoParc.setStatusEpilepsia(epilepsia);
        cardStackView.setLayoutManager(layoutManager);
        cardStackView.setAdapter(adapterInteracaoParc);

        cardStackView.setClickable(false);
    }

    private void configRecyclerViewORIGINAL(boolean epilepsia){
        if (linearLayoutManager == null) {

            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewInteracoes.setHasFixedSize(true);
            recyclerViewInteracoes.setLayoutManager(linearLayoutManager);

            if (adapterInteracaoParc == null) {
                adapterInteracaoParc = new AdapterInteracaoParc(requireContext(),
                        listaUsuario, this, this, layoutManager);
                SwipeItemTouchListener swipeItemTouchListener = new SwipeItemTouchListener(requireContext(), recyclerViewInteracoes, this);
                recyclerViewInteracoes.addOnItemTouchListener(swipeItemTouchListener);
                recyclerViewInteracoes.setAdapter(adapterInteracaoParc);
                adapterInteracaoParc.setStatusEpilepsia(epilepsia);
            }
        }
    }

    private void inicializandoComponentes(View view) {
        recyclerViewInteracoes = view.findViewById(R.id.recyclerViewInteracoesParc);
        cardStackView = view.findViewById(R.id.cardStackView);
    }

    @Override
    public void onPosicaoAnterior(int posicaoAnterior) {

    }

    private void verificaEpilepsia(DadosUserAtualCallback callback){
        DatabaseReference recupEpilepsiaRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("epilepsia");
        recupEpilepsiaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String epilepsia = snapshot.getValue(String.class);

                    if (epilepsia != null && !epilepsia.isEmpty()) {
                        if (epilepsia.equals("Sim")) {
                            callback.onRecuperado(true);
                        } else if (epilepsia.equals("Não")) {
                            callback.onRecuperado(false);
                        }
                    }else{
                        callback.onSemDados();
                    }
                }else{
                    callback.onSemDados();
                }
                recupEpilepsiaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}