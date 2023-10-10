package com.example.ogima.helper;

import static com.luck.picture.lib.thread.PictureThreadUtils.runOnUiThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ogima.R;
import com.example.ogima.fragment.RecupSmsFragment;
import com.example.ogima.model.Postagem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PostUtils {

    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Activity activity;
    private Context context;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> sugestoes;
    private String[] interesses;
    private List<String> interessesMarcados = new ArrayList<>();
    public List<String> interessesMarcadosComAssento = new ArrayList<>();

    public PostUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;

        interesses = activity.getResources().getStringArray(R.array.interests_array);
        sugestoes = new ArrayList<>(Arrays.asList(interesses));
    }

    public List<String> getInteressesMarcadosComAssento() {
        return interessesMarcadosComAssento;
    }

    public void setInteressesMarcadosComAssento(List<String> interessesMarcadosComAssento) {
        this.interessesMarcadosComAssento = interessesMarcadosComAssento;
    }

    public interface RecuperarTimeStampCallback {
        void onRecuperado(long timestampNegativo, String data);

        void onError(String message);
    }

    public interface PrepararHashMapPostCallback {
        void onConcluido(HashMap<String, Object> hashMapPost);

        void onError(String message);
    }

    public interface SalvarHashMapNoFirebaseCallback {
        void onSalvo();

        void onError(String message);
    }

    public interface SalvarInteressesCallback{
        void onSalvo();
        void onError(String message);
    }

    public void limitarCaracteresDescricao(EditText edtTextDescricao, TextView txtViewLimite) {
        InputFilter[] filtersDescricao = new InputFilter[1];
        filtersDescricao[0] = new InputFilter.LengthFilter(Postagem.MAX_LENGTH_DESCRIPTION);
        edtTextDescricao.setFilters(filtersDescricao);

        edtTextDescricao.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int currentLength = charSequence.length();

                txtViewLimite.setText(String.format("%d%s%d", currentLength, "/", Postagem.MAX_LENGTH_DESCRIPTION));

                if (currentLength >= Postagem.MAX_LENGTH_DESCRIPTION) {
                    ToastCustomizado.toastCustomizadoCurto("Limite de caracteres excedido!", context);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void configurarTopicos(AutoCompleteTextView autoCompleteTextView, LinearLayout linearLayout) {
        adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, sugestoes);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String keyword = (String) adapterView.getItemAtPosition(i);
                String keywordSemAssento = FormatarNomePesquisaUtils.removeAcentuacao((String) adapterView.getItemAtPosition(i));
                // Limpa o campo de texto
                autoCompleteTextView.setText("");
                // Remove a palavra da lista de sugestões
                sugestoes.remove(keyword);
                interessesMarcados.add(keywordSemAssento);
                interessesMarcadosComAssento.add(keyword);
                // Atualiza o adapter para refletir as sugestões atualizadas
                adapter.notifyDataSetChanged();
                // Criar um novo Chip
                Chip chip = new Chip(context);
                chip.setText(keyword);
                // Configurar o ícone de remoção
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        linearLayout.removeView(chip);
                        sugestoes.add(keyword);
                        interessesMarcados.remove(keywordSemAssento);
                        interessesMarcadosComAssento.remove(keyword);
                        adapter.notifyDataSetChanged();
                        adapter.add(keyword);
                    }
                });
                // Adiciona ao layout vertical
                linearLayout.addView(chip);
                adapter.remove(keyword);
            }
        });
    }

    public void prepararHashMap(String idUsuario, String idPostagem, String tipoPostagem, String urlPostagem, String descricao, PrepararHashMapPostCallback callback) {
        HashMap<String, Object> hashMapPost = new HashMap<>();
        hashMapPost.put("idDonoPostagem", idUsuario);
        hashMapPost.put("idPostagem", idPostagem);
        hashMapPost.put("tipoPostagem", tipoPostagem);
        hashMapPost.put("totalViewsFotoPostagem", 0);
        hashMapPost.put("urlPostagem", urlPostagem);
        if (descricao != null && !descricao.isEmpty()) {
            hashMapPost.put("descricaoPostagem", descricao);
        }
        hashMapPost.put("listaInteressesPostagem", getInteressesMarcadosComAssento());

        recuperarTimestampNegativo(new RecuperarTimeStampCallback() {
            @Override
            public void onRecuperado(long timestampNegativo, String data) {
                hashMapPost.put("timeStampNegativo", timestampNegativo);
                hashMapPost.put("dataPostagem", data);
                callback.onConcluido(hashMapPost);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void salvarHashMapNoFirebase(DatabaseReference reference, HashMap<String, Object> hashMapPost, SalvarHashMapNoFirebaseCallback callback) {
        reference.setValue(hashMapPost).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callback.onSalvo();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void salvarInteresses(DatabaseReference reference, String idUsuario, String idPostagem, SalvarInteressesCallback callback){
        HashMap<String, Object> dadosInteresse = new HashMap<>();
        for (String interesse : getInteressesMarcadosComAssento()) {
            dadosInteresse.put(interesse, true);
            dadosInteresse.put("idDonoPostagem", idUsuario);
            dadosInteresse.put("idPostagem", idPostagem);
            reference.setValue(dadosInteresse).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    callback.onSalvo();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            });
        }
    }

    private void recuperarTimestampNegativo(RecuperarTimeStampCallback callback) {
        NtpTimestampRepository ntpTimestampRepository = new NtpTimestampRepository();
        ntpTimestampRepository.getNtpTimestamp(context, new NtpTimestampRepository.NtpTimestampCallback() {
            @Override
            public void onSuccess(long timestamps, String dataFormatada) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long timestampNegativo = -1 * timestamps;
                        callback.onRecuperado(timestampNegativo, dataFormatada);
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

    public String retornarIdRandom(DatabaseReference reference) {
        return reference.push().getKey();
    }

    public void exibirProgressDialog(ProgressDialog progressDialog, String tipoMensagem) {
        switch (tipoMensagem) {
            case "upload":
                progressDialog.setMessage("Publicando sua postagem, aguarde um momento...");
                break;
            case "config":
                progressDialog.setMessage("Ajustando mídia, aguarde um momento...");
                break;
        }
        if (!activity.isFinishing()) {
            progressDialog.show();
        }
    }

    public void ocultarProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && !activity.isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
