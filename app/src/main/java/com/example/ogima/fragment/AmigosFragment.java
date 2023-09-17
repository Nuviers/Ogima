package com.example.ogima.fragment;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.ogima.R;
import com.example.ogima.activity.ComunidadePostagensActivity;
import com.example.ogima.activity.ConvitesComunidadeActivity;
import com.example.ogima.activity.EndLobbyActivity;
import com.example.ogima.activity.GruposPublicosActivity;
import com.example.ogima.activity.ListaComunidadesActivity;
import com.example.ogima.activity.LobbyChatRandomActivity;
import com.example.ogima.activity.PersonProfileActivity;
import com.example.ogima.activity.TesteComDiffGrupoActivity;
import com.example.ogima.adapter.AdapterFindPeoples;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.RecyclerItemClickListener;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioFirebase;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.helper.VerificaEpilpesia;
import com.example.ogima.helper.VisitarPerfilSelecionado;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class AmigosFragment extends Fragment {

    private SearchView searchViewFindPeoples;
    private RecyclerView recyclerViewFindPeoples;
    private List<Usuario> listaUsuarios;
    private DatabaseReference usuarioRef;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario;
    private AdapterFindPeoples adapterFindPeoples;
    private String idUsuarioAlvo;
    private String emailUsuarioAtual, idUsuarioLogado;
    private ValueEventListener valueEventListener;
    private Handler handler = new Handler();
    //Recursos para exibição dos banner dos chats aleatórios
    private ImageSlider imageSliderAmigos;
    private ArrayList<SlideModel> imagensSlider = new ArrayList<>();

    private LinearLayout linearLayoutRandomFriends;
    private Button btnVerComunidades;

    private ImageView imgViewProcurarGrupos;
    private Button btnProcurarGrupos;

    private Button btnHomens, btnMulheres,
            btnTodos, btnEncontrarChats;
    private boolean homens = false, mulheres = false, todos = false;
    private String selecao = "";
    private SeekBar seekBarIdade;
    private TextView txtViewProgressIdade, txtViewProgressIdadeMax;
    private int idadeMax = 18;
    private HashMap<String, Object> dadosFiltragem = new HashMap<>();
    private Button buttonEdit;

    public interface DadosUserAtualCallback {
        void onRecuperado(Usuario usuarioAtual);

        void onSemDados();

        void onError(String message);
    }

    public interface FiltrosPreDefinidosCallback {
        void onExistem(Usuario filtroPreDefinido);

        void onNaoExistem();

        void onError(String message);
    }

    public AmigosFragment() {
        // Required empty public constructor
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuarioLogado = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        searchViewFindPeoples.setQuery("", false);
        searchViewFindPeoples.setIconified(true);

        listaUsuarios.clear();
    }

    @Override
    public void onStart() {
        super.onStart();

        //Configuração do searchview
        searchViewFindPeoples.setQueryHint(getString(R.string.hintSearchViewPeople));
        searchViewFindPeoples.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            //Analisa o que foi enviado pelo usuário ao confirmar envio.
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //Analisa o que foi digitado em tempo real.
            @Override
            public boolean onQueryTextChange(String newText) {
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                }
                if (newText != null && !newText.isEmpty()) {
                    String dadoDigitado = Normalizer.normalize(newText, Normalizer.Form.NFD);
                    dadoDigitado = dadoDigitado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
                    String dadoDigitadoOk = dadoDigitado.toUpperCase(Locale.ROOT);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pesquisarPessoas(dadoDigitadoOk);
                        }
                    }, 400);
                }
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        listaUsuarios.clear();
        searchViewFindPeoples.setQuery("", false);
        searchViewFindPeoples.setIconified(true);
        searchViewFindPeoples.setOnQueryTextListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_amigos, container, false);
        inicializandoComponentes(view);

        //Configurações iniciais

        usuarioRef = firebaseRef.child("usuarios");

        //Configuração do slider
        imagensSlider.add(new SlideModel
                (R.drawable.banner_chat_random_final_v1, "Chats com pessoas aleatórias contendo duas categorias (Comum e às cegas)",
                        null));

        imagensSlider.add(new SlideModel
                (R.drawable.banner_final_chat_comum, "Chat comum - Sua aparência será exibida",
                        null));

        imagensSlider.add(new SlideModel
                (R.drawable.banner_final_chat_as_cegas, "Chat às cegas - Sua aparência será revelada somente quando os dois usuários quiserem se revelar",
                        null));

        //Setando o arrayList SlideModel no Slider
        imageSliderAmigos.setImageList(imagensSlider, ScaleTypes.CENTER_CROP);

        //Ouvinte do slider
        imageSliderAmigos.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                if (i == 0) {
                    ToastCustomizado.toastCustomizadoCurto("Zero", getContext());
                    exibirBottomSheet();
                }
                if (i == 1) {
                    ToastCustomizado.toastCustomizadoCurto("Um", getContext());
                }
                if (i == 2) {
                    ToastCustomizado.toastCustomizadoCurto("Dois", getContext());
                }
            }
        });

        //Configuração do recyclerview
        recyclerViewFindPeoples.setHasFixedSize(true);
        recyclerViewFindPeoples.setLayoutManager(new LinearLayoutManager(getActivity()));
        listaUsuarios = new ArrayList<>();
        // Tava aqui o config do adapter
        adapterFindPeoples = new AdapterFindPeoples(listaUsuarios, getActivity());
        recyclerViewFindPeoples.setAdapter(adapterFindPeoples);
        //

        recyclerViewFindPeoples.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        // aqui tinha o clique do recycler


        btnVerComunidades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Leva até a lista de comunidades
                Intent intent = new Intent(getContext(), ListaComunidadesActivity.class);
                //Intent intent = new Intent(getContext(), TesteComDiffGrupoActivity.class);
                startActivity(intent);
            }
        });

        //Oculta a interface e exibe o recycler caso o foco seja o search e caso não seja,
        // será o efeito contrário..
        searchViewFindPeoples.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    recyclerViewFindPeoples.setVisibility(View.VISIBLE);
                    linearLayoutRandomFriends.setVisibility(View.GONE);
                } else {
                    recyclerViewFindPeoples.setVisibility(View.GONE);
                    linearLayoutRandomFriends.setVisibility(View.VISIBLE);
                }
            }
        });

        VerificaEpilpesia.verificarEpilpesiaExibeGifLocal(getContext(),
                R.drawable.ic_gif_grupos_publicos, imgViewProcurarGrupos);

        imgViewProcurarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaGruposPublicos();
            }
        });

        btnProcurarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaGruposPublicos();
            }
        });

        return view;
    }

    private void recuperarPessoa(String idPessoa) {

        DatabaseReference recuperarValor = firebaseRef.child("usuarios")
                .child(idPessoa);

        recuperarValor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);
                    if (listaUsuarios != null && listaUsuarios.size() > 0
                            && listaUsuarios.contains(usuarioRecuperado.getIdUsuario())) {
                    } else {
                        listaUsuarios.add(usuarioRecuperado);
                    }
                    adapterFindPeoples.notifyDataSetChanged();
                } else {

                }
                recuperarValor.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerViewFindPeoples.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewFindPeoples,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        VisitarPerfilSelecionado.visitarPerfilSelecionadoPerson(requireContext(),
                                usuarioSelecionado.getIdUsuario());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }));
    }

    private void pesquisarPessoas(String s) {
        //Limpar lista
        emailUsuarioAtual = autenticacao.getCurrentUser().getEmail();

        try {
            listaUsuarios.clear();
            adapterFindPeoples.notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        //Pesquisar usuário caso o campo digitado não esteja vazio.
        if (s.length() > 0) {
            DatabaseReference searchUsuarioRef = usuarioRef;
            Query query = searchUsuarioRef.orderByChild("nomeUsuarioPesquisa")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            try {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Limpa a lista.
                        listaUsuarios.clear();

                        if (snapshot.getValue() == null) {

                        } else {
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                //Verifica se é o usuário logado, caso seja oculte ele da lista
                                Usuario usuarioQuery = snap.getValue(Usuario.class);
                                idUsuarioAlvo = usuarioQuery.getIdUsuario();

                                DatabaseReference verificaNome = firebaseRef.child("usuarios")
                                        .child(idUsuarioAlvo);

                                verificaNome.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshotVerifica) {
                                        if (snapshotVerifica.getValue() != null) {
                                            Usuario usuarioReceptNome = snapshotVerifica.getValue(Usuario.class);

                                            if (idUsuarioLogado.equals(usuarioReceptNome.getIdUsuario())) {

                                            } else {
                                                recuperarPessoa(usuarioReceptNome.getIdUsuario());
                                                //listaUsuarios.add(usuarioRecept);
                                                //adapterFindPeoples.notifyDataSetChanged();
                                            }
                                        }
                                        verificaNome.removeEventListener(this);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        query.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                listaUsuarios.clear();
                adapterFindPeoples.notifyDataSetChanged();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private void inicializandoComponentes(View view) {
        searchViewFindPeoples = view.findViewById(R.id.searchViewFindPeoples);
        recyclerViewFindPeoples = view.findViewById(R.id.recyclerFindPeoples);
        imageSliderAmigos = view.findViewById(R.id.imageSliderAmigos);
        linearLayoutRandomFriends = view.findViewById(R.id.linearLayoutRandomFriends);
        btnVerComunidades = view.findViewById(R.id.btnVerComunidades);
        imgViewProcurarGrupos = view.findViewById(R.id.imgViewProcurarGrupos);
        btnProcurarGrupos = view.findViewById(R.id.btnProcurarGrupos);
    }

    public void animacaoShimmer() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    recyclerViewFindPeoples.setVisibility(View.VISIBLE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 1200);
    }

    private void irParaGruposPublicos() {
        Intent intent = new Intent(getActivity(), GruposPublicosActivity.class);
        startActivity(intent);
    }

    private void exibirBottomSheet() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireActivity());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_filtro_random); // Defina o layout personalizado

        btnHomens = bottomSheetDialog.findViewById(R.id.btnHomensRandom);
        btnMulheres = bottomSheetDialog.findViewById(R.id.btnMulheresRandom);
        btnTodos = bottomSheetDialog.findViewById(R.id.btnTodosRandom);
        btnEncontrarChats = bottomSheetDialog.findViewById(R.id.btnEncontrarChatsRandom);
        seekBarIdade = bottomSheetDialog.findViewById(R.id.seekBarFiltroChatRandom);
        txtViewProgressIdade = bottomSheetDialog.findViewById(R.id.txtViewProgressIdade);
        txtViewProgressIdadeMax = bottomSheetDialog.findViewById(R.id.txtViewProgressIdadeMax);

        clickListenerSheetDialog();
        configSeekBar();

        verificarFiltrosPreDefinidos(new FiltrosPreDefinidosCallback() {
            @Override
            public void onExistem(Usuario filtroPreDefinido) {
                idadeMax = filtroPreDefinido.getIdadeMaxDesejada();
                selecao = filtroPreDefinido.getGeneroDesejado();

                seekBarIdade.setProgress(idadeMax);

                switch (selecao) {
                    case "homem":
                        buttonEdit = bottomSheetDialog.findViewById(R.id.btnHomensRandom);
                        break;
                    case "mulher":
                        buttonEdit = bottomSheetDialog.findViewById(R.id.btnMulheresRandom);
                        break;
                    case "todos":
                        buttonEdit = bottomSheetDialog.findViewById(R.id.btnTodosRandom);
                        break;
                }
                aparenciaSelecao(buttonEdit, selecao);
            }

            @Override
            public void onNaoExistem() {

            }

            @Override
            public void onError(String message) {

            }
        });

        bottomSheetDialog.show();
    }

    private void clickListenerSheetDialog() {
        btnHomens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!homens) {
                    aparenciaSelecao(btnHomens, "homem");
                }
            }
        });
        btnMulheres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mulheres) {
                    aparenciaSelecao(btnMulheres, "mulher");
                }
            }
        });
        btnTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!todos) {
                    aparenciaSelecao(btnTodos, "todos");
                }
            }
        });

        btnEncontrarChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dadosUserLogado(new DadosUserAtualCallback() {
                    @Override
                    public void onRecuperado(Usuario usuarioAtual) {
                        if (selecao != null && !selecao.isEmpty()) {
                            dadosFiltragem.put("generoDesejado", selecao);
                        } else {
                            dadosFiltragem.put("generoDesejado", "todos");
                        }
                        dadosFiltragem.put("idadeMaxDesejada", idadeMax);
                        dadosFiltragem.put("generoUsuario", usuarioAtual.getGeneroUsuario().toLowerCase(Locale.ROOT));
                        dadosFiltragem.put("idUsuario", idUsuarioLogado);
                        dadosFiltragem.put("idade", usuarioAtual.getIdade());

                        DatabaseReference dadosMatchmakingRef = firebaseRef.child("matchmaking")
                                .child(idUsuarioLogado);

                        dadosMatchmakingRef.setValue(dadosFiltragem).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ToastCustomizado.toastCustomizadoCurto("Ocorreu um erro ao procurar chats, tente novamente mais tarde", requireContext());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent intent = new Intent(requireContext(), LobbyChatRandomActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                requireActivity().finish();
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
            }
        });
    }

    private void aparenciaSelecao(Button buttonSelecionado, String tipoSelecionado) {
        selecao = tipoSelecionado;
        switch (tipoSelecionado) {
            case "homem":
                homens = true;
                mulheres = false;
                todos = false;
                desmarcarSelecao("homem");
                break;
            case "mulher":
                mulheres = true;
                homens = false;
                todos = false;
                desmarcarSelecao("mulher");
                break;
            case "todos":
                todos = true;
                mulheres = false;
                homens = false;
                desmarcarSelecao("todos");
                break;
        }
        String hexText = "#BE0310FF"; // Substitua pelo seu código de cor
        String hexBackground = "#402BFF"; // Substitua pelo seu código de cor
        int colorBackground = Color.parseColor(hexBackground);
        int colorText = Color.parseColor(hexText);
        buttonSelecionado.setTextColor(colorText);
        ViewCompat.setBackgroundTintList(buttonSelecionado, ColorStateList.valueOf(colorBackground));
    }

    private void desmarcarSelecao(String tipoSelecionado) {
        switch (tipoSelecionado) {
            case "homem":
                aparenciaDesmarcado(btnMulheres);
                aparenciaDesmarcado(btnTodos);
                break;
            case "mulher":
                aparenciaDesmarcado(btnHomens);
                aparenciaDesmarcado(btnTodos);
                break;
            case "todos":
                aparenciaDesmarcado(btnHomens);
                aparenciaDesmarcado(btnMulheres);
                break;
        }
    }

    private void aparenciaDesmarcado(Button buttonDesmarcado) {
        String hexText = "#9E000000"; // Substitua pelo seu código de cor
        String hexBackground = "#65000000"; // Substitua pelo seu código de cor
        int colorBackground = Color.parseColor(hexBackground);
        int colorText = Color.parseColor(hexText);
        buttonDesmarcado.setTextColor(colorText);
        ViewCompat.setBackgroundTintList(buttonDesmarcado, ColorStateList.valueOf(colorBackground));
    }

    private void configSeekBar() {
        seekBarIdade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int idadeAtual = i;
                updateIdadeMaxAtual(idadeAtual);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateIdadeMaxAtual(int idade) {
        if (idade < 18) {
            idadeMax = 18;
        } else {
            idadeMax = idade;
        }
        txtViewProgressIdade.setText(String.valueOf(idadeMax));
    }

    private void dadosUserLogado(DadosUserAtualCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuarioLogado, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia, ArrayList<String> listaIdAmigos, ArrayList<String> listaIdSeguindo, String fotoUsuario, String fundoUsuario) {
                callback.onRecuperado(usuarioAtual);
            }

            @Override
            public void onSemDados() {
                callback.onSemDados();
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    private void verificarFiltrosPreDefinidos(FiltrosPreDefinidosCallback callback) {
        DatabaseReference dadosMatchmakingRef = firebaseRef.child("matchmaking")
                .child(idUsuarioLogado);
        dadosMatchmakingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onExistem(snapshot.getValue(Usuario.class));
                } else {
                    callback.onNaoExistem();
                }
                dadosMatchmakingRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
