package com.example.ogima.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ogima.R;
import com.example.ogima.activity.parc.EditarPerfilParcActivity;
import com.example.ogima.activity.parc.ProfileParcActivity;
import com.example.ogima.adapter.AdapterFotosPerfilParc;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FormatarNomePesquisaUtils;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ParceirosFragment extends Fragment {

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

    public ParceirosFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    public interface salvarFotosCallback {
        void onConcluido(ArrayList<String> fotosConfiguradas);

        void onError(String message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_profile_parc, container, false);
        inicializandoComponentes(view);
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        //Configurando o progressDialog
        progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        usuarioParc = new Usuario();

        Bundle dados = getArguments();

        if (dados != null && dados.containsKey("usuarioParc")) {
            criacaoConta = true;
            usuarioParc = (Usuario) dados.getSerializable("usuarioParc");
            configGeral(usuarioParc);
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

        if (criacaoConta) {
            salvarDados();
        }

        return view;
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
            linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerViewFotos.setHasFixedSize(true);
            recyclerViewFotos.setLayoutManager(linearLayoutManager);

            if (recyclerViewFotos.getOnFlingListener() == null) {
                PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                pagerSnapHelper.attachToRecyclerView(recyclerViewFotos);
            }

            if (adapterFotosPerfilParc == null) {
                adapterFotosPerfilParc = new AdapterFotosPerfilParc(requireContext(),
                        usuarioParc.getFotosParc());
                recyclerViewFotos.setAdapter(adapterFotosPerfilParc);
                adapterFotosPerfilParc.notifyDataSetChanged();
            }
        }
    }

    private void salvarDados() {
        progressDialog.setMessage("Salvando dados, aguarde um momento...");
        if (!requireActivity().isFinishing()) {
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
        uploadPhotos(new ProfileParcActivity.salvarFotosCallback() {
            @Override
            public void onConcluido(ArrayList<String> fotosConfiguradas) {
                DatabaseReference salvarFotosRef = firebaseRef.child("usuarioParc")
                        .child(UsuarioUtils.recuperarIdUserAtual()).child("fotosParc");
                salvarFotosRef.setValue(fotosConfiguradas).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (progressDialog != null && !requireActivity().isFinishing()
                                && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        ToastCustomizado.toastCustomizadoCurto("CONCLUIDO",requireContext());
                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
        ToastCustomizado.toastCustomizadoCurto("TUDO UPADO 7", requireContext());
    }

    public void uploadPhotos(ProfileParcActivity.salvarFotosCallback callback) {
        ArrayList<String> storageUrls = new ArrayList<>();
        uploadNextPhoto(0, storageUrls, callback);
    }

    private void uploadNextPhoto(int index, ArrayList<String> storageUrls, ProfileParcActivity.salvarFotosCallback callback) {
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

    private void configGeral(Usuario usuario){
        GlideCustomizado.loadUrl(requireContext(),
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
                Intent intent = new Intent(requireContext(), EditarPerfilParcActivity.class);
                intent.putExtra("usuarioParc", usuario);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void inicializandoComponentes(View view) {
        imgViewFoto = view.findViewById(R.id.imgViewFotoPerfilParc);
        txtViewName = view.findViewById(R.id.txtViewNamePerfilParc);
        btnEditarPerfilParc = view.findViewById(R.id.btnEditarPerfilParc);
        linearLayoutHobbies = view.findViewById(R.id.linearLayoutHobbiesParc);
        recyclerViewFotos = view.findViewById(R.id.recyclerViewFotosPerfilParc);
    }
}
