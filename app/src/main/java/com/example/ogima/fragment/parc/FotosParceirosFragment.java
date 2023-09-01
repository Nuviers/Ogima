package com.example.ogima.fragment.parc;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.IntentEdicaoPerfilParc;
import com.example.ogima.helper.ParceiroUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.example.ogima.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DateUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FotosParceirosFragment extends Fragment implements View.OnClickListener {

    private DataTransferListener dataTransferListener;
    private FloatingActionButton fabParc;
    private Usuario usuario;
    private ImageView imgViewFtParc1, imgViewFtParc2,
            imgViewFtParc3, imgViewFtParc4;
    private ImageButton imgBtnFtParc1, imgBtnFtParc2,
            imgBtnFtParc3, imgBtnFtParc4;
    private Button btnContinuarFtParc;
    private ArrayList<String> fotos = new ArrayList<>(4);
    private String tipoMidiaPermissao = null;
    private PictureSelectorStyle selectorStyle;
    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private Uri uriSelecionada = null;
    private String letra = "";
    private ProgressDialog progressDialog;
    private int posicaoSelecionada = -1;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario = "";
    private boolean edicao = false;
    private ArrayList<String> fotosEdicao = new ArrayList<>(4);
    private ArrayList<String> urlsARemover = new ArrayList<>();
    private ArrayList<String> fotosPreview = new ArrayList<>();
    private char currentLetter = 'A';
    private StorageReference storageRef;
    private ArrayList<String> fotosEdit = new ArrayList<>();
    private ImageButton imgBtnDeleteFtParc1, imgBtnDeleteFtParc2,
            imgBtnDeleteFtParc3, imgBtnDeleteFtParc4;
    private String urlDelete = null;

    public FotosParceirosFragment() {
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    public interface salvarFotosCallback {
        void onConcluido(ArrayList<String> fotosConfiguradas);

        void onError(String message);
    }

    public interface UparUrlCallback {
        void onUpado(String urlUpada);

        void onError(String message);
    }

    public interface RemocaoIndividualCallback {
        void onRemovido();

        void onError(String message);
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

    private void onButtonClicked(ArrayList<String> listaFotos) {
        if (edicao) {
            uploadFotos(listaFotos);
            return;
        }
        if (dataTransferListener != null) {
            usuario.setFotosParc(listaFotos);
            dataTransferListener.onUsuarioParc(usuario, "fotos");
        }
    }

    public void setName(Usuario usuarioParc) {
        usuario = usuarioParc;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fotos_parceiros, container, false);
        inicializandoComponentes(view);

        fotos.addAll(Arrays.asList(null, null, null, null));

        Bundle args = getArguments();
        if (args != null && args.containsKey("edit")) {
            storageRef = ConfiguracaoFirebase.getFirebaseStorage();
            //Configurando o progressDialog
            progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            fotosEdit = args.getStringArrayList("edit");
            fotosEdicao.addAll(Arrays.asList(null, null, null, null));
            for (int i = 0; i < fotosEdit.size(); i++) {
                fotosEdicao.set(i, currentLetter + fotosEdit.get(i));
                fotos.set(i, currentLetter + fotosEdit.get(i));
                //Log.d("EDITTESTE",currentLetter+fotosEdit.get(i));
                currentLetter = (char) (currentLetter + 1);
            }
            edicao = true;
            configVisibilidadeBtnsExclusao(fotosEdicao);
            ToastCustomizado.toastCustomizado("Size: " + fotosEdicao.size(), requireContext());
            previewFotosEdicao();
        }else{
            configVisibilidadeBtnsExclusao(fotos);
        }

        //Configurando o progressDialog
        progressDialog = new ProgressDialog(requireContext(), ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        selectorStyle = new PictureSelectorStyle();
        configStylePictureSelector();

        imgBtnFtParc1.setOnClickListener(this);
        imgBtnFtParc2.setOnClickListener(this);
        imgBtnFtParc3.setOnClickListener(this);
        imgBtnFtParc4.setOnClickListener(this);

        imgBtnDeleteFtParc1.setOnClickListener(this);
        imgBtnDeleteFtParc2.setOnClickListener(this);
        imgBtnDeleteFtParc3.setOnClickListener(this);
        imgBtnDeleteFtParc4.setOnClickListener(this);

        btnContinuarFtParc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configurarLista();
            }
        });

        return view;
    }

    private void configStylePictureSelector() {
        TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
        blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));

        BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
        numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_9b));
        numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));
        numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_white));
        numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.ps_demo_blue_num_selected);
        numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_53575e));
        numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_53575e));

        SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
        numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectNumberStyle(true);
        numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);

        numberBlueSelectMainStyle.setSelectBackground(R.drawable.ps_demo_blue_num_selector);
        numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_white));
        numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_preview_blue_num_selector);

        numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_9b));
        numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_blue));
        numberBlueSelectMainStyle.setSelectText(R.string.ps_completed);

        selectorStyle.setTitleBarStyle(blueTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
    }


    private void inicializandoComponentes(View view) {
        imgViewFtParc1 = view.findViewById(R.id.imgViewFtParc1);
        imgViewFtParc2 = view.findViewById(R.id.imgViewFtParc2);
        imgViewFtParc3 = view.findViewById(R.id.imgViewFtParc3);
        imgViewFtParc4 = view.findViewById(R.id.imgViewFtParc4);

        imgBtnFtParc1 = view.findViewById(R.id.imgBtnFtParc1);
        imgBtnFtParc2 = view.findViewById(R.id.imgBtnFtParc2);
        imgBtnFtParc3 = view.findViewById(R.id.imgBtnFtParc3);
        imgBtnFtParc4 = view.findViewById(R.id.imgBtnFtParc4);

        btnContinuarFtParc = view.findViewById(R.id.btnContinuarFtParc);

        imgBtnDeleteFtParc1 = view.findViewById(R.id.imgBtnDeleteFtParc1);
        imgBtnDeleteFtParc2 = view.findViewById(R.id.imgBtnDeleteFtParc2);
        imgBtnDeleteFtParc3 = view.findViewById(R.id.imgBtnDeleteFtParc3);
        imgBtnDeleteFtParc4 = view.findViewById(R.id.imgBtnDeleteFtParc4);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtnFtParc1:
                posicaoSelecionada = 0;
                verificarEscolhaMidia("imagem");
                break;
            case R.id.imgBtnFtParc2:
                posicaoSelecionada = 1;
                verificarEscolhaMidia("imagem");
                break;
            case R.id.imgBtnFtParc3:
                posicaoSelecionada = 2;
                verificarEscolhaMidia("imagem");
                break;
            case R.id.imgBtnFtParc4:
                posicaoSelecionada = 3;
                verificarEscolhaMidia("imagem");
                break;

            //Exclusão
            case R.id.imgBtnDeleteFtParc1:
                removerFotoIndividualmente(0, new RemocaoIndividualCallback() {
                    @Override
                    public void onRemovido() {
                        imgBtnDeleteFtParc1.setVisibility(View.GONE);
                        previewSemFoto(imgViewFtParc1);
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
                break;
            case R.id.imgBtnDeleteFtParc2:
                removerFotoIndividualmente(1, new RemocaoIndividualCallback() {
                    @Override
                    public void onRemovido() {
                        imgBtnDeleteFtParc2.setVisibility(View.GONE);
                        previewSemFoto(imgViewFtParc2);
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
                break;
            case R.id.imgBtnDeleteFtParc3:
                removerFotoIndividualmente(2, new RemocaoIndividualCallback() {
                    @Override
                    public void onRemovido() {
                        imgBtnDeleteFtParc3.setVisibility(View.GONE);
                        previewSemFoto(imgViewFtParc3);
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
                break;
            case R.id.imgBtnDeleteFtParc4:
                removerFotoIndividualmente(3, new RemocaoIndividualCallback() {
                    @Override
                    public void onRemovido() {
                        imgBtnDeleteFtParc4.setVisibility(View.GONE);
                        previewSemFoto(imgViewFtParc4);
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
                break;
        }
    }

    private void verificarEscolhaMidia(String tipoMidia) {
        switch (tipoMidia) {
            case "imagem":
                tipoMidiaPermissao = "galeria";
                checkPermissions();
                break;
        }
    }

    private void checkPermissions() {
        if (tipoMidiaPermissao != null) {
            boolean galleryPermissionsGranted = PermissionUtils.requestGalleryPermissions(requireActivity());
            if (galleryPermissionsGranted) {
                // Permissões da galeria já concedidas.
                switch (tipoMidiaPermissao) {
                    case "galeria":
                        selecionarGaleria();
                        break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkPermissionResult(grantResults)) {
                // Permissões concedidas.
                if (tipoMidiaPermissao != null) {
                    // Permissões da galeria já concedidas.
                    switch (tipoMidiaPermissao) {
                        case "galeria":
                            selecionarGaleria();
                            break;
                    }
                }
            } else {
                // Permissões negadas.
                PermissionUtils.openAppSettings(requireActivity(), requireContext());
            }
        }
    }

    private void selecionarGaleria() {
        PictureSelector.create(FotosParceirosFragment.this)
                .openGallery(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1)
                .setSelectorUIStyle(selectorStyle)
                .setSelectMaxFileSize(MAX_FILE_SIZE_IMAGEM * 1024 * 1024)
                .setImageEngine(GlideEngineCustomizado.createGlideEngine()) // Substitua GlideEngine pelo seu próprio mecanismo de carregamento de imagem, se necessário
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {

                        //Caso aconteça de alguma forma que a lista que já foi manipulada
                        //retorne com dados nela, ela é limpa para evitar duplicações.
                        limparUri();

                        //ToastCustomizado.toastCustomizado("RESULT", getApplicationContext());

                        if (result != null && result.size() > 0) {
                            for (LocalMedia media : result) {

                                // Faça o que for necessário com cada foto selecionada
                                String path = media.getPath(); // Obter o caminho do arquivo da foto

                                if (PictureMimeType.isHasImage(media.getMimeType())) {
                                    openCropActivity(Uri.parse(path), destinoImagemUri(result));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void limparUri() {
        if (uriSelecionada != null) {
            uriSelecionada = null;
        }
    }

    //*Método responsável por ajustar as proporções do corte.
    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                //.withMaxResultSize ( 510 , 715 )
                //Método chamado responsável pelas configurações
                //da interface e opções do próprio Ucrop.
                .withOptions(getOptions())
                .start(requireActivity(), this);

    }

    //*Método responsável pelas configurações
    //da interface e opções do próprio Ucrop.
    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        //Ajustando qualidade da imagem que foi cortada
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(70);
        //Ajustando título da interface
        options.setToolbarTitle("Ajustar imagem");
        //Possui diversas opções a mais no youtube e no próprio github.
        return options;
    }

    private Uri destinoImagemUri(ArrayList<LocalMedia> result) {

        Uri destinationUri = null;

        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            if (PictureMimeType.isHasImage(media.getMimeType())) {
                String fileName = DateUtils.getCreateFileName("CROP_") + ".jpg";
                File outputFile = new File(requireContext().getCacheDir(), fileName);
                destinationUri = Uri.fromFile(outputFile);
                //ToastCustomizado.toastCustomizado("Caminho: " + destinationUri, getApplicationContext());
                Log.d("Caminho ", String.valueOf(destinationUri));
                break; // Sai do loop após encontrar a primeira imagem
            }
        }

        return destinationUri;
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }


    private void exibirProgressDialog(String tipoMensagem) {

        switch (tipoMensagem) {
            case "config":
                progressDialog.setMessage("Ajustando mídia, aguarde um momento...");
                break;
        }
        if (!requireActivity().isFinishing()) {
            progressDialog.show();
        }
    }

    private void ocultarProgressDialog() {
        if (progressDialog != null && !requireActivity().isFinishing()
                && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                Uri imagemRecortada = UCrop.getOutput(data);
                if (imagemRecortada != null) {
                    ToastCustomizado.toastCustomizado("Posição " + posicaoSelecionada, requireContext());
                    if (edicao) {
                        if (fotosEdicao.get(posicaoSelecionada) != null) {
                            armazenarUrlParaRemocao(posicaoSelecionada);
                        }
                    }
                    uriSelecionada = imagemRecortada;
                    enviarDadoParaConfig("imagem");
                }
            }
        }
    }

    private void enviarDadoParaConfig(String tipoMidia) {
        ImageView imageViewAlvo = null;
        if (tipoMidiaPermissao != null
                && !tipoMidiaPermissao.isEmpty()) {
            switch (posicaoSelecionada) {
                case 0:
                    letra = "A";
                    imageViewAlvo = imgViewFtParc1;
                    break;
                case 1:
                    letra = "B";
                    imageViewAlvo = imgViewFtParc2;
                    break;
                case 2:
                    letra = "C";
                    imageViewAlvo = imgViewFtParc3;
                    break;
                case 3:
                    letra = "D";
                    imageViewAlvo = imgViewFtParc4;
                    break;
            }
            GlideCustomizado.loadUrl(requireActivity(),
                    uriSelecionada.toString(),
                    imageViewAlvo, android.R.color.transparent,
                    GlideCustomizado.CENTER_CROP, false, true);
            adicionarUri();
        }
    }

    private void adicionarUri() {
        if (posicaoSelecionada != -1) {
            if (posicaoSelecionada >= 0 && posicaoSelecionada < fotos.size()) {
                // Substitui a foto existente pela nova foto na posição
                if (edicao) {
                    fotos.set(posicaoSelecionada, letra.toUpperCase(Locale.ROOT) + "!" + uriSelecionada.toString());
                } else {
                    fotos.set(posicaoSelecionada, letra.toUpperCase(Locale.ROOT) + uriSelecionada.toString());
                }
            } else {
                if (edicao) {
                    // Adiciona a nova foto na lista
                    fotos.add(posicaoSelecionada, letra.toUpperCase(Locale.ROOT) + "!" + uriSelecionada.toString());
                } else {
                    // Adiciona a nova foto na lista
                    fotos.add(posicaoSelecionada, letra.toUpperCase(Locale.ROOT) + uriSelecionada.toString());
                }
            }
            ToastCustomizado.toastCustomizadoCurto(String.valueOf(posicaoSelecionada + " Uri: " + uriSelecionada.toString()), requireContext());
        }
    }

    private void configurarLista() {

        int tamanhoReal = 0;

        if (fotos != null) {
            for (String item : fotos) {
                if (item != null) {
                    tamanhoReal++;
                }
            }
            if (tamanhoReal == -1 || tamanhoReal <= 0) {
                ToastCustomizado.toastCustomizadoCurto("Necessário selecionar pelo menos uma foto", requireContext());
                return;
            }
        }
        fotos.removeAll(Collections.singleton(null));
        // Organize a lista com as fotos selecionadas
        Collections.sort(fotos);
        ArrayList<String> fotosOrdenadas = new ArrayList<>(fotos);
        for (String uri : fotosOrdenadas) {
            Log.d("URITESTE", uri);
        }
        ArrayList<String> fotosConfiguradas = new ArrayList<>();
        for (String originalString : fotosOrdenadas) {
            if (originalString.length() > 1) {
                String novaString = originalString.substring(1);
                fotosConfiguradas.add(novaString);
            }
        }
        for (String uriConfig : fotosConfiguradas) {
            Log.d("URITESTE2", uriConfig);
        }
        ToastCustomizado.toastCustomizadoCurto("Lista " + fotosConfiguradas.size(), requireContext());
        onButtonClicked(fotosConfiguradas);
    }

    private void previewFotosEdicao() {
        if (fotosEdit != null) {
            if (fotosEdit.size() == 4) {
                glideEdicao(imgViewFtParc1, fotosEdit.get(0));
                glideEdicao(imgViewFtParc2, fotosEdit.get(1));
                glideEdicao(imgViewFtParc3, fotosEdit.get(2));
                glideEdicao(imgViewFtParc4, fotosEdit.get(3));
            } else if (fotosEdit.size() == 3) {
                glideEdicao(imgViewFtParc1, fotosEdit.get(0));
                glideEdicao(imgViewFtParc2, fotosEdit.get(1));
                glideEdicao(imgViewFtParc3, fotosEdit.get(2));
            } else if (fotosEdit.size() == 2) {
                glideEdicao(imgViewFtParc1, fotosEdit.get(0));
                glideEdicao(imgViewFtParc2, fotosEdit.get(1));
            } else if (fotosEdit.size() == 1) {
                glideEdicao(imgViewFtParc1, fotosEdit.get(0));
            }
        }
    }

    private void glideEdicao(ImageView imgViewAlvo, String url) {
        GlideCustomizado.loadUrl(requireContext(),
                url, imgViewAlvo, android.R.color.transparent,
                GlideCustomizado.CENTER_CROP, false, true);
    }

    private void armazenarUrlParaRemocao(int posicao) {

        if (posicao > fotosEdicao.size()) {
            return;
        }

        if (urlsARemover != null
                && urlsARemover.size() > 0 && !urlsARemover.contains(fotosEdicao.get(posicao))) {
            urlsARemover.add(fotosEdicao.get(posicao));
        } else if (urlsARemover == null || urlsARemover != null && urlsARemover.size() <= 0) {
            urlsARemover.add(fotosEdicao.get(posicao));
        }

        ToastCustomizado.toastCustomizado("Remoção - " + urlsARemover.size(), requireContext());
    }

    private void uploadFotos(ArrayList<String> listaFotos) {
        if (listaFotos.isEmpty()) {
            // Lista vazia, não há fotos para fazer upload
            return;
        }

        // Começa com a primeira foto da lista
        uploadFotoAtIndex(listaFotos, 0);
    }

    private void uploadFotoAtIndex(ArrayList<String> listaFotos, int index) {
        if (index < listaFotos.size()) {
            String url = listaFotos.get(index);

            if (!url.isEmpty() && url.charAt(0) == '!') {
                String urlAjustada = url.substring(1);

                uparFoto(urlAjustada, index, new UparUrlCallback() {
                    @Override
                    public void onUpado(String urlUpada) {
                        listaFotos.set(index, urlUpada);
                        uploadFotoAtIndex(listaFotos, index + 1); // Chama a próxima foto
                    }

                    @Override
                    public void onError(String message) {
                        // Lida com o erro, se necessário
                    }
                });
            } else {
                // Nada para fazer upload, passa para a próxima foto
                uploadFotoAtIndex(listaFotos, index + 1);
            }
        } else {
            // Todas as fotos foram processadas, atualiza a lista no Firebase
            DatabaseReference atualizarListaRef = firebaseRef.child("usuarioParc")
                    .child(idUsuario).child("fotosParc");
            atualizarListaRef.setValue(listaFotos).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    if (urlsARemover != null && urlsARemover.size() > 0) {
                        removerFotosFirebaseStorage(urlsARemover);
                    } else {
                        //Tudo concluído.
                        IntentEdicaoPerfilParc.irParaEdicao(requireContext(), idUsuario);
                        ToastCustomizado.toastCustomizado("CONCLUIDO", requireContext());
                    }
                }
            });
        }
    }

    private void uparFoto(String url, int index, UparUrlCallback callback) {
        String nomeRandomico = UUID.randomUUID().toString();
        StorageReference imagemRef = storageRef.child("parceiros")
                .child("imagens")
                .child(UsuarioUtils.recuperarIdUserAtual())
                .child("imagem" + nomeRandomico + ".jpeg");
        imagemRef.putFile(Uri.parse(url))
                .addOnSuccessListener(taskSnapshot -> {
                    imagemRef.getDownloadUrl().addOnSuccessListener(uriResult -> {
                        callback.onUpado(uriResult.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    private void removerFotosFirebaseStorage(ArrayList<String> fotosParaRemover) {
        try {
            int totalFotos = fotosParaRemover.size();
            AtomicInteger fotosRemovidas = new AtomicInteger();

            for (String url : fotosParaRemover) {
                String urlSemLetra = url.substring(1);
                StorageReference fotoRef = storageRef.child("parceiros")
                        .child("imagens")
                        .child(idUsuario)
                        .getStorage()
                        .getReferenceFromUrl(urlSemLetra);
                fotoRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            fotosRemovidas.getAndIncrement();
                            if (fotosRemovidas.get() == totalFotos) {
                                ToastCustomizado.toastCustomizado("CONCLUIDO", requireContext());
                                IntentEdicaoPerfilParc.irParaEdicao(requireContext(), idUsuario);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Lida com o erro, se necessário
                            fotosRemovidas.getAndIncrement();
                            if (fotosRemovidas.get() == totalFotos) {
                                ToastCustomizado.toastCustomizado("CONCLUIDO", requireContext());
                            }
                        });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configVisibilidadeBtnsExclusao(ArrayList<String> fotosAlvo) {
            if (fotosAlvo != null
                    && fotosAlvo.size() > 0) {
                if (fotosAlvo.get(0) != null) {
                    imgBtnDeleteFtParc1.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFtParc1.setVisibility(View.GONE);
                }
                if (fotosAlvo.get(1) != null) {
                    imgBtnDeleteFtParc2.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFtParc2.setVisibility(View.GONE);
                }
                if (fotosAlvo.get(2) != null) {
                    imgBtnDeleteFtParc3.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFtParc3.setVisibility(View.GONE);
                }
                if (fotosAlvo.get(3) != null) {
                    imgBtnDeleteFtParc4.setVisibility(View.VISIBLE);
                } else {
                    imgBtnDeleteFtParc4.setVisibility(View.GONE);
                }
            }
    }

    private void removerFotoIndividualmente(int index, RemocaoIndividualCallback callback) {

        ArrayList<String> listaAnalisada = new ArrayList<>();
        listaAnalisada = fotos;
        listaAnalisada.removeAll(Collections.singleton(null));

        if (!edicao && listaAnalisada != null && listaAnalisada.size() <= 1) {
            ToastCustomizado.toastCustomizado("Necessário ter no mínimo uma foto em seu perfil", requireContext());
            return;
        }

        if (listaAnalisada == null
                || listaAnalisada != null && listaAnalisada.size() <= 0) {
            ToastCustomizado.toastCustomizado("Necessário ter no mínimo uma foto em seu perfil", requireContext());
            return;
        }

        urlDelete = null;

        if (index > listaAnalisada.size()) {
            return;
        }

        if (listaAnalisada.size() <= 1) {
            ToastCustomizado.toastCustomizado("Necessário que tenha no minímo 1 foto", requireContext());
            return;
        }

        urlDelete = fotos.get(index);
        urlDelete = urlDelete.substring(1);
        if (urlDelete.charAt(0) == '!') {
            urlDelete = urlDelete.substring(1);
        }
        ParceiroUtils.recuperarFotos(idUsuario, new ParceiroUtils.RecuperarFotosCallback() {
            @Override
            public void onRecuperado(ArrayList<String> listaFotos) {
                if (listaFotos.contains(urlDelete)) {
                    try {
                        StorageReference fotoRef = storageRef.child("parceiros")
                                .child("imagens")
                                .child(idUsuario)
                                .getStorage()
                                .getReferenceFromUrl(urlDelete);
                        fotoRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    if (listaFotos.size() <= 1) {
                                        ToastCustomizado.toastCustomizado("Necessário que tenha no minímo 1 foto salva no seu perfil", requireContext());
                                    } else {
                                        listaFotos.remove(urlDelete);
                                        DatabaseReference salvarUrlRef = firebaseRef
                                                .child("usuarioParc")
                                                .child(idUsuario).child("fotosParc");
                                        salvarUrlRef.setValue(listaFotos).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                fotos.set(index, null);
                                                if (edicao) {
                                                    fotosEdicao.set(index, null);
                                                }
                                                callback.onRemovido();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                callback.onError(e.getMessage());
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Lida com o erro, se necessário
                                    callback.onError(e.getMessage());
                                });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }else{
                    fotos.set(index, null);
                    if (edicao) {
                        fotosEdicao.set(index, null);
                    }
                }
            }

            @Override
            public void onSemDados() {
                fotos.set(index, null);
                if (edicao) {
                    fotosEdicao.set(index, null);
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }
    private void previewSemFoto(ImageView imgViewAlvo){
        String colorString = "#AEACAC"; // Substitua com a sua string de cor
        int color = Color.parseColor(colorString);
        imgViewAlvo.setColorFilter(color);
    }
}