package com.example.ogima.fragment;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.ogima.R;
import com.example.ogima.activity.FindPeopleActivity;
import com.example.ogima.activity.ListaComunidadesActivityNEW;
import com.example.ogima.activity.ListagemGruposActivity;
import com.example.ogima.activity.LobbyChatRandomActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DialogUtils;
import com.example.ogima.helper.FirebaseRecuperarUsuario;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class SocialFragment extends Fragment{

    private String idUsuario = "";
    private ImageView imgViewProcurarGrupos, imgViewSocialUp;
    private ImageButton imgBtnSearchSocial;
    private Button btnProcurarGrupos, btnVerComunidades;
    private ImageSlider imageSliderSocial;
    private boolean epilepsia = true;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private ArrayList<SlideModel> slideModels = new ArrayList<>();
    private CardView cardViewSocial;

    private Button btnHomens, btnMulheres,
            btnTodos, btnEncontrarChats;
    private boolean homens = false, mulheres = false, todos = false;
    private String selecao = "";
    private SeekBar seekBarIdade;
    private TextView txtViewProgressIdade, txtViewProgressIdadeMax, txtViewSearchSocial;
    private int idadeMax = 100;
    private HashMap<String, Object> dadosFiltragem = new HashMap<>();
    private Button buttonEdit;
    private DialogUtils dialogUtils;

    public SocialFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amigos, container, false);
        inicializandoComponentes(view);
        UsuarioUtils.verificaEpilepsia(idUsuario, new UsuarioUtils.VerificaEpilepsiaCallback() {
            @Override
            public void onConcluido(boolean statusEpilepsia) {
                epilepsia = statusEpilepsia;
                configInicial();
                configSlider();
                clickListeners();
            }

            @Override
            public void onSemDado() {
                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), requireContext());
                requireActivity().finish();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                requireActivity().finish();
            }
        });
        return view;
    }

    private void configSlider() {
        //Configuração do slider
        slideModels.add(new SlideModel
                (R.drawable.banner_chat_random_final_v1, getString(R.string.chat_with_random_people),
                        null));

        slideModels.add(new SlideModel
                (R.drawable.banner_final_chat_comum, getString(R.string.information_second_slide),
                        null));

        //Setando o arrayList SlideModel no Slider
        imageSliderSocial.setImageList(slideModels, ScaleTypes.CENTER_CROP);

        //Ouvinte do slider
        imageSliderSocial.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
               exibirBottomSheet();
            }
        });
    }

    private void configInicial() {
        GlideCustomizado.loadGifPorDrawable(requireContext(), R.drawable.ic_gif_grupos_publicos,
                imgViewProcurarGrupos, android.R.color.transparent, epilepsia);
    }

    private void exibirBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireActivity());
        dialogUtils = new DialogUtils(bottomSheetDialog);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_filtro_random);

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
                ToastCustomizado.toastCustomizado(getString(R.string.error_displaying_search_filter), requireContext());
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
                        dadosFiltragem.put("idUsuario", idUsuario);
                        dadosFiltragem.put("idade", usuarioAtual.getIdade());

                        DatabaseReference dadosMatchmakingRef = firebaseRef.child("matchmaking")
                                .child(idUsuario);

                        dadosMatchmakingRef.setValue(dadosFiltragem).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_when_searching_for_random_chats), requireContext());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialogUtils.fecharBottomDialog();
                                Intent intent = new Intent(requireContext(), LobbyChatRandomActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onSemDados() {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.error_retrieving_user_data), requireContext());
                        requireActivity().finish();
                    }

                    @Override
                    public void onError(String message) {
                        ToastCustomizado.toastCustomizado(String.format("%s %s", getString(R.string.an_error_has_occurred), message), requireContext());
                        requireActivity().finish();
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
                updateIdadeMaxAtual(i);
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

    private void clickListeners() {
        cardViewSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaPesquisaDeUsuarios();
            }
        });
        imgBtnSearchSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaPesquisaDeUsuarios();
            }
        });
        txtViewSearchSocial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irParaPesquisaDeUsuarios();
            }
        });
        btnVerComunidades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Leva até a lista de comunidades
                Intent intent = new Intent(requireContext(), ListaComunidadesActivityNEW.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        imgViewProcurarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaGruposPreview();
            }
        });

        btnProcurarGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                irParaGruposPreview();
            }
        });
    }

    private void irParaGruposPreview() {
        Intent intent = new Intent(requireContext(), ListagemGruposActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
      /*
        Intent intent = new Intent(getActivity(), GruposPublicosActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
       */
    }

    private void dadosUserLogado(DadosUserAtualCallback callback) {
        FirebaseRecuperarUsuario.recuperaUsuarioCompleto(idUsuario, new FirebaseRecuperarUsuario.RecuperaUsuarioCompletoCallback() {
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
                .child(idUsuario);
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

    private void irParaPesquisaDeUsuarios(){
        Intent intent = new Intent(requireActivity(), FindPeopleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void inicializandoComponentes(View view) {
        imgViewProcurarGrupos = view.findViewById(R.id.imgViewProcurarGrupos);
        btnProcurarGrupos = view.findViewById(R.id.btnProcurarGrupos);
        imgViewSocialUp = view.findViewById(R.id.imgViewSocialUp);
        btnVerComunidades = view.findViewById(R.id.btnVerComunidades);
        imageSliderSocial = view.findViewById(R.id.imageSliderSocial);
        cardViewSocial = view.findViewById(R.id.cardViewSocial);
        txtViewSearchSocial = view.findViewById(R.id.txtViewSearchSocial);
        imgBtnSearchSocial = view.findViewById(R.id.imgBtnSearchSocial);
    }
}
