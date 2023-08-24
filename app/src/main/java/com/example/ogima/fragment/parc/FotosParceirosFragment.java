package com.example.ogima.fragment.parc;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import com.example.ogima.activity.ConfigurarPostagemActivity;
import com.example.ogima.activity.PostagemActivity;
import com.example.ogima.helper.DataTransferListener;
import com.example.ogima.helper.GlideCustomizado;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.PermissionUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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


    public FotosParceirosFragment() {

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
                fotos.set(posicaoSelecionada, letra.toUpperCase(Locale.ROOT)+uriSelecionada.toString());
            } else {
                // Adiciona a nova foto na lista
                fotos.add(posicaoSelecionada,letra.toUpperCase(Locale.ROOT)+uriSelecionada.toString());
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
        for(String uri : fotosOrdenadas){
            Log.d("URITESTE",uri);
        }
        ArrayList<String> fotosConfiguradas = new ArrayList<>();
        for (String originalString : fotosOrdenadas) {
            if (originalString.length() > 1) {
                String novaString = originalString.substring(1);
                fotosConfiguradas.add(novaString);
            }
        }
        for(String uriConfig : fotosConfiguradas){
            Log.d("URITESTE2",uriConfig);
        }
        ToastCustomizado.toastCustomizadoCurto("Lista " + fotosConfiguradas.size(), requireContext());
        onButtonClicked(fotosConfiguradas);
    }
}