package com.example.ogima.fragment.cad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.example.ogima.ui.cadastro.FotoPerfilActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.util.Locale;

public class EpilepsiaFragment extends Fragment {

    private Button btnSim, btnNao;
    private DataCadListener dataTransferListener;
    private Usuario usuario;
    private boolean epilepsia = false;
    private FloatingActionButton fabProximo;
    private String idUsuario = "";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private AlertDialog.Builder builder;

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

    public EpilepsiaFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_epilepsia, container, false);
        inicializandoComponentes(view);
        usuario = new Usuario();
        builder = new AlertDialog.Builder(requireContext());
        clickListeners();
        return view;
    }

    private void desmarcarSelecao(boolean status) {
        if (status) {
            aparenciaDesmarcado(btnNao);
        } else {
            aparenciaDesmarcado(btnSim);
        }
    }

    private void aparenciaSelecao(Button buttonSelecionado, boolean status) {
        epilepsia = status;
        if (status) {
            desmarcarSelecao(true);
        } else {
            desmarcarSelecao(false);
        }
        String hexText = "#BE0310FF"; // Substitua pelo seu código de cor
        String hexBackground = "#402BFF"; // Substitua pelo seu código de cor
        int colorBackground = Color.parseColor(hexBackground);
        int colorText = Color.parseColor(hexText);
        buttonSelecionado.setTextColor(colorText);
        ViewCompat.setBackgroundTintList(buttonSelecionado, ColorStateList.valueOf(colorBackground));
    }

    private void aparenciaDesmarcado(Button buttonDesmarcado) {
        String hexText = "#9E000000"; // Substitua pelo seu código de cor
        String hexBackground = "#65000000"; // Substitua pelo seu código de cor
        int colorBackground = Color.parseColor(hexBackground);
        int colorText = Color.parseColor(hexText);
        buttonDesmarcado.setTextColor(colorText);
        ViewCompat.setBackgroundTintList(buttonDesmarcado, ColorStateList.valueOf(colorBackground));
    }

    private void exibirAlertDialog(){
        if (epilepsia) {
            builder.setTitle(getResources().getString(R.string.choose_epilepsy,getString(R.string.yes)));
        } else {
            builder.setTitle(getResources().getString(R.string.choose_epilepsy,getString(R.string.no)));
        }
        builder.setMessage(getString(R.string.confirm_epilepsy_choice));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onButtonClicked();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onButtonClicked() {
        if (dataTransferListener != null) {
            usuario.setStatusEpilepsia(epilepsia);
            dataTransferListener.onUsuario(usuario, "epilepsia");
        }
    }

    public void setUserCad(Usuario usuarioCad) {
        usuario = usuarioCad;
    }

    private void clickListeners() {
        btnSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aparenciaSelecao(btnSim, true);
            }
        });

        btnNao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aparenciaSelecao(btnNao, false);
            }
        });

        fabProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exibirAlertDialog();
            }
        });
    }

    private void inicializandoComponentes(View view) {
        btnSim = view.findViewById(R.id.btnSimEpilepsia);
        btnNao = view.findViewById(R.id.btnNaoEpilepsia);
        fabProximo = view.findViewById(R.id.fabParc);
    }
}