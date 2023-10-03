package com.example.ogima.fragment.cad;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.IntentEdicaoPerfilParc;
import com.example.ogima.helper.IrParaEdicaoDePerfil;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class GeneroFragment extends Fragment {

    private Button btnGHomem, btnGMulher,
            btnGOutro;
    private boolean homem = false, mulher = false, outro = false;
    private DataCadListener dataTransferListener;
    private Usuario usuario;
    private String selecao = "";
    private FloatingActionButton fabProximo;
    private String idUsuario = "";
    private String genero = "";
    private Button buttonEdit;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public GeneroFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DataCadListener) {
            dataTransferListener = (DataCadListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DataTransferListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataTransferListener = null;
    }

    private void onButtonClicked() {
        if (selecao != null && !selecao.isEmpty()) {
            if (genero != null && !genero.isEmpty()) {
                DatabaseReference atualizarRef = firebaseRef.child("usuarios")
                        .child(idUsuario);
                Map<String, Object> update = new HashMap<>();
                update.put("generoUsuario", selecao.toLowerCase(Locale.ROOT));
                atualizarRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto(getString(R.string.changed_gender), requireContext());
                        IrParaEdicaoDePerfil.intentEdicao(requireActivity());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastCustomizado.toastCustomizado(String.format("%s %s", R.string.an_error_has_occurred,e.getMessage()), requireContext());
                        IrParaEdicaoDePerfil.intentEdicao(requireActivity());
                    }
                });
                return;
            }
            if (dataTransferListener != null) {
                usuario.setGeneroUsuario(selecao);
                dataTransferListener.onUsuario(usuario, "genero");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genero, container, false);
        inicializandoComponentes(view);
        usuario = new Usuario();
        Bundle args = getArguments();
        if (args != null && args.containsKey("edit")) {
            genero = args.getString("edit").toLowerCase(Locale.ROOT);
            switch (genero) {
                case "homem":
                    buttonEdit = view.findViewById(R.id.btnGHomem);
                    break;
                case "mulher":
                    buttonEdit = view.findViewById(R.id.btnGMulher);
                    break;
                case "outro":
                    buttonEdit = view.findViewById(R.id.btnGOutro);
                    break;
            }
            aparenciaSelecao(buttonEdit, genero);
        }
        btnGHomem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!homem) {
                    aparenciaSelecao(btnGHomem, "homem");
                }
            }
        });
        btnGMulher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mulher) {
                    aparenciaSelecao(btnGMulher, "mulher");
                }
            }
        });
        btnGOutro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!outro) {
                    aparenciaSelecao(btnGOutro, "outro");
                }
            }
        });
        fabProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClicked();
            }
        });
        return view;
    }

    private void aparenciaSelecao(Button buttonSelecionado, String tipoSelecionado) {
        selecao = tipoSelecionado;
        switch (tipoSelecionado) {
            case "homem":
                homem = true;
                mulher = false;
                outro = false;
                desmarcarSelecao("homem");
                break;
            case "mulher":
                mulher = true;
                homem = false;
                outro = false;
                desmarcarSelecao("mulher");
                break;
            case "outro":
                outro = true;
                mulher = false;
                homem = false;
                desmarcarSelecao("outro");
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
                aparenciaDesmarcado(btnGMulher);
                aparenciaDesmarcado(btnGOutro);
                break;
            case "mulher":
                aparenciaDesmarcado(btnGHomem);
                aparenciaDesmarcado(btnGOutro);
                break;
            case "outro":
                aparenciaDesmarcado(btnGHomem);
                aparenciaDesmarcado(btnGMulher);
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

    public void setUserCad(Usuario usuarioCad) {
        usuario = usuarioCad;
    }

    private void inicializandoComponentes(View view) {
        btnGHomem = view.findViewById(R.id.btnGHomem);
        btnGMulher = view.findViewById(R.id.btnGMulher);
        btnGOutro = view.findViewById(R.id.btnGOutro);
        fabProximo = view.findViewById(R.id.fabParc);
    }
}