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
import androidx.annotation.Nullable;

import com.example.ogima.R;
import com.example.ogima.fragment.RecupSmsFragment;
import com.example.ogima.model.Postagem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseError;
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
    private int interessesConcluidos = 0;

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

    public interface SalvarInteressesCallback {
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
                    ToastCustomizado.toastCustomizado(activity.getString(R.string.character_limit_reached, 0, Postagem.MAX_LENGTH_DESCRIPTION), context);
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

    public void preencherTopicoEdicao(AutoCompleteTextView autoCompleteTextView, LinearLayout linearLayout) {
        for (String interesse : getInteressesMarcadosComAssento()) {
            sugestoes.remove(interesse);
            adapter.notifyDataSetChanged();
            Chip chip = new Chip(context);
            chip.setText(interesse);
            // Configurar o ícone de remoção
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayout.removeView(chip);
                    sugestoes.add(interesse);
                    interessesMarcadosComAssento.remove(interesse);
                    adapter.notifyDataSetChanged();
                    adapter.add(interesse);
                }
            });
            // Adiciona ao layout vertical
            linearLayout.addView(chip);
            adapter.remove(interesse);
        }
    }

    public void prepararHashMap(String idUsuario, String idPostagem, String tipoPostagem, String urlPostagem, String descricao, PrepararHashMapPostCallback callback) {
        recuperarTimestampNegativo(new RecuperarTimeStampCallback() {
            @Override
            public void onRecuperado(long timestampNegativo, String data) {
                HashMap<String, Object> hashMapPost = new HashMap<>();
                String caminhoPostagem = "/postagens/" + idUsuario + "/" + idPostagem + "/";
                hashMapPost.put(caminhoPostagem + "idDonoPostagem", idUsuario);
                hashMapPost.put(caminhoPostagem + "idPostagem", idPostagem);
                hashMapPost.put(caminhoPostagem + "tipoPostagem", tipoPostagem);
                hashMapPost.put(caminhoPostagem + "totalViewsFotoPostagem", 0);
                if (!tipoPostagem.equals("texto")) {
                    hashMapPost.put(caminhoPostagem + "urlPostagem", urlPostagem);
                }
                if (descricao != null && !descricao.isEmpty()) {
                    hashMapPost.put(caminhoPostagem + "descricaoPostagem", descricao);
                }
                hashMapPost.put(caminhoPostagem + "listaInteressesPostagem/", getInteressesMarcadosComAssento());
                hashMapPost.put(caminhoPostagem + "timeStampNegativo", timestampNegativo);
                hashMapPost.put(caminhoPostagem + "dataPostagem", data);
                //Configuração para o nó interesses também.
                salvarHashMapPost(hashMapPost, idUsuario, idPostagem, new SalvarInteressesCallback() {
                    @Override
                    public void onSalvo() {
                        callback.onConcluido(hashMapPost);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void salvarHashMapPost(HashMap<String, Object> operacoes, String idUsuario, String idPostagem, SalvarInteressesCallback callback) {
        String caminhoInteresses = "/interessesPostagens/" + idPostagem + "/";
        int totalInteresses = getInteressesMarcadosComAssento().size();
        for (String interesse : getInteressesMarcadosComAssento()) {
            operacoes.put(caminhoInteresses + interesse, true);
            operacoes.put(caminhoInteresses + "idDonoPostagem", idUsuario);
            operacoes.put(caminhoInteresses + "idPostagem", idPostagem);
            interessesConcluidos++;
            if (interessesConcluidos == totalInteresses) {
                interessesConcluidos = 0;
                firebaseRef.updateChildren(operacoes, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {
                            callback.onSalvo();
                        } else {
                            callback.onError(String.valueOf(error.getCode()));
                        }
                    }
                });
            }
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
                progressDialog.setMessage("Publicando sua postagem, aguarde um momento....");
                break;
            case "config":
                progressDialog.setMessage("Ajustando mídia, aguarde um momento....");
                break;
            case "edicao":
                progressDialog.setMessage("Atualizando postagem, aguarde um momento....");
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
