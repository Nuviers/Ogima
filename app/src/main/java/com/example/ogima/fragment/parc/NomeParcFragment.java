package com.example.ogima.fragment.parc;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.AlphaNumericInputFilter;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class NomeParcFragment extends Fragment {

    private EditText edtTextNomeParc;
    private TextView txtViewLimiteNomeParc;
    private final int MAX_LENGTH_NAME = 100;
    private final int MIN_LENGTH_NAME = 3;
    private Boolean limiteCaracteresPermitido = false;
    private String blockCharacterSet = "'!@#$%¨&*()_+-=[{]}/?|,.;:~^´`";
    private DataTransferListener dataTransferListener;
    private FloatingActionButton fabParc;
    private Usuario usuarioParc;
    private String nomeEdit = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";

    public NomeParcFragment() {
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
        String name = edtTextNomeParc.getText().toString();

        if (limiteCaracteresPermitido) {
            if (nomeEdit != null) {
                String nomeFormatado = name.replaceAll("\\s+", " ");
                nomeFormatado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeFormatado);
                DatabaseReference atualizarNomeRef = firebaseRef.child("usuarioParc")
                        .child(idUsuario);
                if (nomeFormatado != null && !nomeFormatado.isEmpty()) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("nomeParc", nomeFormatado);
                    atualizarNomeRef.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            ToastCustomizado.toastCustomizadoCurto("Nome alterado com sucesso!", requireContext());
                        }
                    });
                }
                return;
            }

            if (dataTransferListener != null) {
                String nomeFormatado = name.replaceAll("\\s+", " ");
                nomeFormatado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeFormatado);
                if (nomeEdit != null && !nomeEdit.isEmpty()) {
                    usuarioParc.setNomeParc(nomeFormatado);
                    dataTransferListener.onUsuarioParc(usuarioParc, "nome");
                }else{
                    usuarioParc.setNomeParc(nomeFormatado);
                    dataTransferListener.onUsuarioParc(usuarioParc, "nome");
                }
            }
        }else{
            ToastCustomizado.toastCustomizado("Limite de caractères não permitido, ele deve ser entre " + MIN_LENGTH_NAME+"-"+MAX_LENGTH_NAME, requireContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nome_parc, container, false);
        inicializandoComponentes(view);
        usuarioParc = new Usuario();
        limiteCaracteres();
        edtTextNomeParc.setFilters(new InputFilter[]{new AlphaNumericInputFilter()});
        // Recuperar os argumentos
        Bundle args = getArguments();
        if (args != null && args.containsKey("edit")) {
            nomeEdit = args.getString("edit");
            edtTextNomeParc.setText(nomeEdit);
            // Faça algo com o valor recebido
        }
        fabParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClicked();
            }
        });
        return view;
    }

    private void limiteCaracteres() {

        InputFilter[] filtersDescricao = new InputFilter[1];
        filtersDescricao[0] = new InputFilter.LengthFilter(MAX_LENGTH_NAME); // Define o limite máximo de 2.000 caracteres.
        edtTextNomeParc.setFilters(filtersDescricao);


        edtTextNomeParc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();

                txtViewLimiteNomeParc.setText(currentLength + "/" + MAX_LENGTH_NAME);

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

    private void inicializandoComponentes(View view) {
        edtTextNomeParc = view.findViewById(R.id.edtTextNomeParc);
        txtViewLimiteNomeParc = view.findViewById(R.id.txtViewLimiteNomeParc);
        fabParc = view.findViewById(R.id.fabParc);
    }
}