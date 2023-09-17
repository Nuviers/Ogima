package com.example.ogima.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.adapter.AdapterDailyShortsSelecao;
import com.example.ogima.adapter.AdapterFotosPerfilParc;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileParcActivity extends AppCompatActivity {

    private ImageView imgViewFoto;
    private TextView txtViewName;
    private Usuario usuarioParc;
    private Button btnEditarPerfilParc;
    private LinearLayout linearLayoutHobbies;
    private RecyclerView recyclerViewFotos;
    private LinearLayoutManager linearLayoutManager;
    private AdapterFotosPerfilParc adapterFotosPerfilParc;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private StorageReference storageRef;
    private int contadorFotos = 0;
    private ProgressDialog progressDialog;
    private String idUsuario = "";
    private boolean criacaoConta = false;

    public ProfileParcActivity() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    public interface salvarFotosCallback {
        void onConcluido(ArrayList<String> fotosConfiguradas);

        void onError(String message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_parc);
        inicializandoComponentes();

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        //Configurando o progressDialog
        progressDialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        usuarioParc = new Usuario();

        Bundle dados = getIntent().getExtras();

        if (dados != null && dados.containsKey("usuarioParc")) {
            criacaoConta = true;
            usuarioParc = (Usuario) dados.getSerializable("usuarioParc");
            salvarDados();
        }else{
            ParceiroUtils.recuperarDados(idUsuario, new ParceiroUtils.RecuperarUserParcCallback() {
                @Override
                public void onRecuperado(Usuario usuario, String nome, String orientacao, String exibirPerfilPara, String idUserParc, ArrayList<String> listaHobbies, ArrayList<String> listaFotos, ArrayList<String> listaIdsAEsconder) {
                    usuarioParc = usuario;
                    configGeral(usuarioParc);
                }

                @Override
                public void onSemDados() {

                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void exibirHobbies() {
        // Adiciona um chip para cada hobby
        for (String hobby : usuarioParc.getListaInteressesParc()) {
            Chip chip = new Chip(linearLayoutHobbies.getContext());
            chip.setText(hobby);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.DKGRAY));
            chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 4, 8, 4); // Define o espaçamento entre os chips
            chip.setLayoutParams(params);
            chip.setClickable(false);
            linearLayoutHobbies.addView(chip);
        }
    }

    private void configRecyclerView() {
        if (linearLayoutManager == null) {
            for (String foto : usuarioParc.getFotosParc()) {
                Log.d("fotoParc", foto);
            }
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewFotos.setHasFixedSize(true);
            recyclerViewFotos.setLayoutManager(linearLayoutManager);

            if (recyclerViewFotos.getOnFlingListener() == null) {
                PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                pagerSnapHelper.attachToRecyclerView(recyclerViewFotos);
            }

            if (adapterFotosPerfilParc == null) {
                adapterFotosPerfilParc = new AdapterFotosPerfilParc(getApplicationContext(),
                        usuarioParc.getFotosParc());
                recyclerViewFotos.setAdapter(adapterFotosPerfilParc);
                adapterFotosPerfilParc.notifyDataSetChanged();
            }
        }
    }

    private void salvarDados() {
        progressDialog.setMessage("Salvando dados, aguarde um momento...");
        if (!isFinishing()) {
            progressDialog.show();
        }
        HashMap<String, Object> dadosParc = new HashMap<>();
        DatabaseReference usuarioParcRef = firebaseRef.child("usuarioParc")
                .child(UsuarioUtils.recuperarIdUserAtual());
        dadosParc.put("nomeParc", usuarioParc.getNomeParc());
        dadosParc.put("exibirPerfilPara", usuarioParc.getExibirPerfilPara());
        dadosParc.put("orientacaoSexual", usuarioParc.getOrientacaoSexual());
        dadosParc.put("listaInteressesParc", usuarioParc.getListaInteressesParc());
        dadosParc.put("idsEsconderParc", usuarioParc.getIdsEsconderParc());
        dadosParc.put("idUsuario", UsuarioUtils.recuperarIdUserAtual());
        usuarioParcRef.setValue(dadosParc);
        uploadPhotos(new salvarFotosCallback() {
            @Override
            public void onConcluido(ArrayList<String> fotosConfiguradas) {
                DatabaseReference salvarFotosRef = firebaseRef.child("usuarioParc")
                        .child(UsuarioUtils.recuperarIdUserAtual()).child("fotosParc");
                salvarFotosRef.setValue(fotosConfiguradas).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (progressDialog != null && !isFinishing()
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        configGeral(usuarioParc);
                        ToastCustomizado.toastCustomizadoCurto("CONCLUIDO",getApplicationContext());
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
        ToastCustomizado.toastCustomizadoCurto("TUDO UPADO 7", getApplicationContext());
    }

    public void uploadPhotos(salvarFotosCallback callback) {
        ArrayList<String> storageUrls = new ArrayList<>();
        uploadNextPhoto(0, storageUrls, callback);
    }

    private void uploadNextPhoto(int index, ArrayList<String> storageUrls, salvarFotosCallback callback) {
        if (index < usuarioParc.getFotosParc().size()) {
            Uri uri = Uri.parse(usuarioParc.getFotosParc().get(index));

            String nomeRandomico = UUID.randomUUID().toString();
            StorageReference imagemRef = storageRef.child("parceiros")
                    .child("imagens")
                    .child(UsuarioUtils.recuperarIdUserAtual())
                    .child("imagem" + nomeRandomico + ".jpeg");

            imagemRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imagemRef.getDownloadUrl().addOnSuccessListener(uriResult -> {
                            storageUrls.add(uriResult.toString());
                            uploadNextPhoto(index + 1, storageUrls, callback);
                        });
                    })
                    .addOnFailureListener(e -> {
                        uploadNextPhoto(index + 1, storageUrls, callback);
                    });
        } else {
            callback.onConcluido(storageUrls);
        }
    }

    private void salvarFotosV2(salvarFotosCallback callback) {
        ArrayList<String> listaFotos = new ArrayList<>();
        AtomicInteger uploadCounter = new AtomicInteger(0);
        for (String uri : usuarioParc.getFotosParc()) {
            String nomeRandomico = UUID.randomUUID().toString();
            StorageReference imagemRef = storageRef.child("parceiros")
                    .child("imagens")
                    .child(UsuarioUtils.recuperarIdUserAtual())
                    .child("imagem" + nomeRandomico + ".jpeg");
            UploadTask uploadTask = imagemRef.putFile(Uri.parse(uri));
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onError(e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imagemRef.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onError(e.getMessage());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            listaFotos.add(uri.toString());
                            int count = uploadCounter.incrementAndGet();
                            if (count == usuarioParc.getFotosParc().size()) {
                                callback.onConcluido(listaFotos);
                            }
                        }
                    });
                }
            });
        }
    }

    private void configGeral(Usuario usuario){
        GlideCustomizado.loadUrl(getApplicationContext(),
                usuario.getFotosParc().get(0).toString(),
                imgViewFoto,
                android.R.color.transparent,
                GlideCustomizado.CIRCLE_CROP, false, true);
        txtViewName.setText(FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuario.getNomeParc()));
        exibirHobbies();
        configRecyclerView();
        btnEditarPerfilParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileParcActivity.this, EditarPerfilParcActivity.class);
                intent.putExtra("usuarioParc", usuario);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void inicializandoComponentes() {
        imgViewFoto = findViewById(R.id.imgViewFotoPerfilParc);
        txtViewName = findViewById(R.id.txtViewNamePerfilParc);
        btnEditarPerfilParc = findViewById(R.id.btnEditarPerfilParc);
        linearLayoutHobbies = findViewById(R.id.linearLayoutHobbiesParc);
        recyclerViewFotos = findViewById(R.id.recyclerViewFotosPerfilParc);
    }
}