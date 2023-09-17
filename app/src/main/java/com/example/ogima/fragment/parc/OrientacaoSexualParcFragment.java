package com.example.ogima.fragment.parc;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.IntentEdicaoPerfilParc;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class OrientacaoSexualParcFragment extends Fragment {

    private Button btnHeterossexualParc, btnGayParc,
            btnLesbicaParc, btnBissexualParc;
    private boolean heterossexual = false, gay = false, lesbica = false,
            bissexual = false;
    private DataTransferListener dataTransferListener;
    private Usuario usuario;
    private String selecao = "";
    private FloatingActionButton fabParc;
    private String orientacaoEdit = "";
    private String idUsuario = "";
    private Button buttonEdit;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public OrientacaoSexualParcFragment() {
        // Required empty public constructor
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DataTransferListener) {
            dataTransferListener = (DataTransferListener) context;
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
            if (orientacaoEdit != null && !orientacaoEdit.isEmpty()) {
                DatabaseReference atualizarNomeRef = firebaseRef.child("usuarioParc")
                        .child(idUsuario);
                Map<String, Object> update = new HashMap<>();
                update.put("orientacaoSexual", selecao.toLowerCase(Locale.ROOT));
                atualizarNomeRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Orientação sexual alterada com sucesso!", requireContext());
                        IntentEdicaoPerfilParc.irParaEdicao(requireContext(), idUsuario);
                    }
                });
                return;
            }

            if (dataTransferListener != null) {
                usuario.setOrientacaoSexual(selecao);
                dataTransferListener.onUsuarioParc(usuario, "orientacao");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orientacao_sexual, container, false);
        inicializandoComponentes(view);
        usuario = new Usuario();
        Bundle args = getArguments();
        if (args != null && args.containsKey("edit")) {
            orientacaoEdit = args.getString("edit").toLowerCase(Locale.ROOT);
            switch (orientacaoEdit) {
                case "heterossexual":
                    buttonEdit = btnHeterossexualParc;
                    break;
                case "gay":
                    buttonEdit = btnGayParc;
                    break;
                case "lesbica":
                    buttonEdit = btnLesbicaParc;
                    break;
                case "bissexual":
                    buttonEdit = btnBissexualParc;
                    break;
            }
            aparenciaSelecao(buttonEdit, orientacaoEdit);
        }
        btnHeterossexualParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!heterossexual) {
                    aparenciaSelecao(btnHeterossexualParc, "heterossexual");
                }
            }
        });
        btnGayParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!gay) {
                    aparenciaSelecao(btnGayParc, "gay");
                }
            }
        });
        btnLesbicaParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!lesbica) {
                    aparenciaSelecao(btnLesbicaParc, "lesbica");
                }
            }
        });
        btnBissexualParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bissexual) {
                    aparenciaSelecao(btnBissexualParc, "bissexual");
                }
            }
        });
        fabParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClicked();
            }
        });
        return view;
    }

    private void inicializandoComponentes(View view) {
        btnHeterossexualParc = view.findViewById(R.id.btnHeterossexualParc);
        btnGayParc = view.findViewById(R.id.btnGayParc);
        btnLesbicaParc = view.findViewById(R.id.btnLesbicaParc);
        btnBissexualParc = view.findViewById(R.id.btnBissexualParc);
        fabParc = view.findViewById(R.id.fabParc);
    }

    public void setName(Usuario usuarioParc) {
        usuario = usuarioParc;
    }

    private void aparenciaSelecao(Button buttonSelecionado, String tipoSelecionado) {
        selecao = tipoSelecionado;
        switch (tipoSelecionado) {
            case "heterossexual":
                heterossexual = true;
                gay = false;
                lesbica = false;
                bissexual = false;
                desmarcarSelecao("heterossexual");
                break;
            case "gay":
                gay = true;
                heterossexual = false;
                lesbica = false;
                bissexual = false;
                desmarcarSelecao("gay");
                break;
            case "lesbica":
                lesbica = true;
                gay = false;
                heterossexual = false;
                bissexual = false;
                desmarcarSelecao("lesbica");
                break;
            case "bissexual":
                bissexual = true;
                lesbica = false;
                gay = false;
                heterossexual = false;
                desmarcarSelecao("bissexual");
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
            case "heterossexual":
                aparenciaDesmarcado(btnGayParc);
                aparenciaDesmarcado(btnLesbicaParc);
                aparenciaDesmarcado(btnBissexualParc);
                break;
            case "gay":
                aparenciaDesmarcado(btnHeterossexualParc);
                aparenciaDesmarcado(btnLesbicaParc);
                aparenciaDesmarcado(btnBissexualParc);
                break;
            case "lesbica":
                aparenciaDesmarcado(btnHeterossexualParc);
                aparenciaDesmarcado(btnGayParc);
                aparenciaDesmarcado(btnBissexualParc);
                break;
            case "bissexual":
                aparenciaDesmarcado(btnHeterossexualParc);
                aparenciaDesmarcado(btnGayParc);
                aparenciaDesmarcado(btnLesbicaParc);
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
}