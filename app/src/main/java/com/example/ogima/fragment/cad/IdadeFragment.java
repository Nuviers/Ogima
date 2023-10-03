package com.example.ogima.fragment.cad;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.helper.DataCadListener;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.NtpTimestampRepository;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class IdadeFragment extends Fragment {

    private String dataNascimento = "";
    private TextView edtTxtDataCad;
    private DataCadListener dataTransferListener;
    private Usuario usuario;
    private int idade = -1;
    private FloatingActionButton fabProximo;
    private static int MAX_AGE = 150;
    private static int MIN_AGE = 13;
    private String localConvertido;
    private int anoAtual = -1;

    private interface AnoAtualCallback{
        void onRecuperado(int ano);
        void onError(String message);
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
        View view = inflater.inflate(R.layout.fragment_idade, container, false);
        inicializandoComponentes(view);
        usuario = new Usuario();
        configLocal();
        recuperarAnoAtual(new AnoAtualCallback() {
            @Override
            public void onRecuperado(int ano) {
                anoAtual = ano;
                clickListeners();
            }

            @Override
            public void onError(String message) {
                ToastCustomizado.toastCustomizadoCurto(String.format("%s %s", R.string.an_error_has_occurred, message), requireContext());
                requireActivity().finish();
            }
        });
        return view;
    }

    private void openDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                formatarData(day, month, year);
            }
        }, 2023, 0, 1);
        datePickerDialog.show();
    }

    private void formatarData(int dia, int mes, int ano) {

        int mesAjustado = mes + 1;
        String diaFormatado = String.format("%02d", dia);
        String mesFormatado = String.format("%02d", mesAjustado);

        if (localConvertido.equals("pt_BR")) {
            dataNascimento = String.format("%s%s%s%s%d", diaFormatado,"/",mesFormatado,"/",ano);
        } else {
            dataNascimento = String.format("%d%s%s%s%s", ano,"/",mesFormatado,"/",diaFormatado);
        }
        edtTxtDataCad.setText(dataNascimento);

        int calculoIdade = anoAtual - ano;
        if (calculoIdade < MIN_AGE) {
            idade = -1;
            dataNascimento = "";
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.minimum_age_limit_for_registration, MIN_AGE), requireContext());
            return;
        }
        if (calculoIdade > MAX_AGE) {
            idade = -1;
            dataNascimento = "";
            ToastCustomizado.toastCustomizadoCurto(getString(R.string.invalid_date), requireContext());
            return;
        }
        idade = calculoIdade;
    }

    public void setUserCad(Usuario usuarioCad) {
        usuario = usuarioCad;
    }

    private void configLocal(){
        Locale current = getResources().getConfiguration().locale;
        localConvertido = localConvertido.valueOf(current);

        if (localConvertido.equals("pt_BR")) {
            edtTxtDataCad.setHint("dd/mm/yyyy");
        } else {
            edtTxtDataCad.setHint("yyyy/mm/dd");
        }
    }

    private void onButtonClicked(){
        if (dataTransferListener != null
                && idade != -1) {
            usuario.setIdade(idade);
            usuario.setDataNascimento(dataNascimento);
            dataTransferListener.onUsuario(usuario, "idade");
        }
    }

    private void recuperarAnoAtual(AnoAtualCallback callback){
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(requireContext(), new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(timestamps);
                        int anoFormatado = calendar.get(Calendar.YEAR);
                        callback.onRecuperado(anoFormatado);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    private void clickListeners(){
        edtTxtDataCad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
        fabProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClicked();
            }
        });
    }

    private void inicializandoComponentes(View view){
        edtTxtDataCad = view.findViewById(R.id.txtViewDataCad);
        fabProximo = view.findViewById(R.id.fabParc);
    }
}