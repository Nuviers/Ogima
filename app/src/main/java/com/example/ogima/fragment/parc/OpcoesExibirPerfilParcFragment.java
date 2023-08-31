package com.example.ogima.fragment.parc;

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
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.giphy.sdk.analytics.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class OpcoesExibirPerfilParcFragment extends Fragment {

    private Button btnHomensParc, btnMulheresParc,
            btnTodosParc;
    private boolean homens = false, mulheres = false, todos = false;
    private DataTransferListener dataTransferListener;
    private Usuario usuario;
    private String selecao = "";
    private FloatingActionButton fabParc;
    private String idUsuario = "";
    private String exibirParaEdit = "";
    private Button buttonEdit;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

    public OpcoesExibirPerfilParcFragment() {
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
            if (exibirParaEdit != null && !exibirParaEdit.isEmpty()) {
                DatabaseReference atualizarNomeRef = firebaseRef.child("usuarioParc")
                        .child(idUsuario);
                Map<String, Object> update = new HashMap<>();
                update.put("exibirPerfilPara", selecao.toLowerCase(Locale.ROOT));
                atualizarNomeRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ToastCustomizado.toastCustomizadoCurto("Exibição alvo alterado com sucesso!", requireContext());
                    }
                });
                return;
            }
            if (dataTransferListener != null) {
                usuario.setExibirPerfilPara(selecao);
                dataTransferListener.onUsuarioParc(usuario, "exibirPara");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opcoes_exibir_perfil_parc, container, false);
        inicializandoComponentes(view);
        usuario = new Usuario();
        Bundle args = getArguments();
        if (args != null && args.containsKey("edit")) {
            exibirParaEdit = args.getString("edit").toLowerCase(Locale.ROOT);
            switch (exibirParaEdit) {
                case "homens":
                    buttonEdit = view.findViewById(R.id.btnHomensParc);
                    break;
                case "mulheres":
                    buttonEdit = view.findViewById(R.id.btnMulheresParc);
                    break;
                case "todos":
                    buttonEdit = view.findViewById(R.id.btnTodosParc);
                    break;
            }
            aparenciaSelecao(buttonEdit, exibirParaEdit);
        }
        btnHomensParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!homens) {
                    aparenciaSelecao(btnHomensParc, "homens");
                }
            }
        });
        btnMulheresParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mulheres) {
                    aparenciaSelecao(btnMulheresParc, "mulheres");
                }
            }
        });
        btnTodosParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!todos) {
                    aparenciaSelecao(btnTodosParc, "todos");
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
        btnHomensParc = view.findViewById(R.id.btnHomensParc);
        btnMulheresParc = view.findViewById(R.id.btnMulheresParc);
        btnTodosParc = view.findViewById(R.id.btnTodosParc);
        fabParc = view.findViewById(R.id.fabParc);
    }

    public void setName(Usuario usuarioParc) {
        usuario = usuarioParc;
    }

    private void aparenciaSelecao(Button buttonSelecionado, String tipoSelecionado) {
        selecao = tipoSelecionado;
        switch (tipoSelecionado) {
            case "homens":
                homens = true;
                mulheres = false;
                todos = false;
                desmarcarSelecao("homens");
                break;
            case "mulheres":
                mulheres = true;
                homens = false;
                todos = false;
                desmarcarSelecao("mulheres");
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
            case "homens":
                aparenciaDesmarcado(btnMulheresParc);
                aparenciaDesmarcado(btnTodosParc);
                break;
            case "mulheres":
                aparenciaDesmarcado(btnHomensParc);
                aparenciaDesmarcado(btnTodosParc);
                break;
            case "todos":
                aparenciaDesmarcado(btnHomensParc);
                aparenciaDesmarcado(btnMulheresParc);
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