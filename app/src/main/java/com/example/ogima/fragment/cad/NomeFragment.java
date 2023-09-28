package com.example.ogima.fragment.cad;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ogima.R;
import com.example.ogima.helper.AlphaNumericInputFilter;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.IrParaEdicaoDePerfil;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class NomeFragment extends Fragment {

    private EditText edtTxtNome;
    private TextView txtViewLimite;
    private final int MAX_LENGTH_NAME = 100;
    private final int MIN_LENGTH_NAME = 3;
    private Boolean limiteCaracteresPermitido = false;
    private String blockCharacterSet = "'!@#$%¨&*()_+-=[{]}/?|,.;:~^´`";
    private DataCadListener dataTransferListener;
    private FloatingActionButton fabProximo;
    private Usuario usuario;
    private String nomeEdit = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";

    public NomeFragment() {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cad_nome, container, false);
        inicializandoComponentes(view);
        configInicial();
        clickListeners();
        return view;
    }

    private void onButtonClicked() {
        String name = edtTxtNome.getText().toString();

        if (limiteCaracteresPermitido) {
            if (nomeEdit != null && !nomeEdit.isEmpty()) {
                String nomeFormatado = name.replaceAll("\\s+", " ");
                nomeFormatado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeFormatado);
                DatabaseReference atualizarNomeRef = firebaseRef.child("usuarios")
                        .child(idUsuario);
                if (nomeFormatado != null && !nomeFormatado.isEmpty()) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("nomeUsuario", nomeFormatado);
                    atualizarNomeRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            ToastCustomizado.toastCustomizadoCurto(getString(R.string.changed_name), requireContext());
                            IrParaEdicaoDePerfil.intentEdicao(requireActivity());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", getString(R.string.an_error_has_occurred),e.getMessage()), requireContext());
                        }
                    });
                }else{
                    IrParaEdicaoDePerfil.intentEdicao(requireActivity());
                }
                return;
            }

            if (dataTransferListener != null) {
                String nomeFormatado = name.replaceAll("\\s+", " ");
                nomeFormatado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeFormatado);
                if (nomeEdit != null && !nomeEdit.isEmpty()) {
                    usuario.setNomeUsuario(nomeFormatado);
                    dataTransferListener.onUsuario(usuario, "nome");
                }else{
                    usuario.setNomeUsuario(nomeFormatado);
                    dataTransferListener.onUsuario(usuario, "nome");
                }
            }
        }else{
            ToastCustomizado.toastCustomizado(getString(R.string.character_limit_reached, MIN_LENGTH_NAME, MAX_LENGTH_NAME), requireContext());
        }
    }

    private void limiteCaracteres() {
        InputFilter[] filtersDescricao = new InputFilter[1];
        filtersDescricao[0] = new InputFilter.LengthFilter(MAX_LENGTH_NAME);
        edtTxtNome.setFilters(filtersDescricao);
        edtTxtNome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                txtViewLimite.setText(String.format("%d %s %d", currentLength,"/",MAX_LENGTH_NAME));
                if (currentLength < MIN_LENGTH_NAME) {
                    limiteCaracteresPermitido = false;
                } else if (currentLength >= MIN_LENGTH_NAME) {
                    limiteCaracteresPermitido = true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void configInicial(){
        usuario = new Usuario();
        limiteCaracteres();
        edtTxtNome.setFilters(new InputFilter[]{new AlphaNumericInputFilter()});
        // Recuperar os argumentos
        Bundle args = getArguments();
        if (args != null && args.containsKey("edit")) {
            nomeEdit = args.getString("edit");
            edtTxtNome.setText(nomeEdit);
        }
    }

    private void clickListeners(){
        fabProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClicked();
            }
        });
    }

    private void inicializandoComponentes(View view) {
        edtTxtNome = view.findViewById(R.id.edtTxtNomeCad);
        txtViewLimite = view.findViewById(R.id.txtViewLimiteNomeCad);
        fabProximo = view.findViewById(R.id.fabParc);
    }
}