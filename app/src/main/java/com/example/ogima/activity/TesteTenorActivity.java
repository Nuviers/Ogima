package com.example.ogima.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.ogima.R;
import com.example.ogima.helper.GlideEngineCustomizado;
import com.example.ogima.helper.LimparCacheUtils;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.ml.LiteModelEfficientdetLite4DetectionDefault2;
import com.example.ogima.ml.LiteModelEfficientdetLite4DetectionMetadata2;
import com.example.ogima.ml.LiteModelImagenetMobilenetV3Large100224Classification5Metadata1;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.utils.DateUtils;
import com.yalantis.ucrop.UCrop;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TesteTenorActivity extends AppCompatActivity {

    private Button btnFotoGaleriaTensor;
    private static final int MAX_FILE_SIZE_IMAGEM = 6;
    private Uri uriSelecionada = null;
    private ImageView imgViewTesteTensor;

    private LiteModelEfficientdetLite4DetectionDefault2 model;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LimparCacheUtils limparCacheUtils = new LimparCacheUtils();
        limparCacheUtils.clearAppCache(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_tenor);

        btnFotoGaleriaTensor = findViewById(R.id.btnFotoGaleriaTensor);
        imgViewTesteTensor = findViewById(R.id.imgViewTesteTensor);

        btnFotoGaleriaTensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecionarGaleria();
            }
        });
    }


    private void selecionarGaleria() {
        PictureSelector.create(TesteTenorActivity.this)
                .openGallery(SelectMimeType.ofImage()) // Definir o tipo de mídia que você deseja selecionar (somente imagens, neste caso)
                .setSelectionMode(SelectModeConfig.SINGLE)
                .setMaxSelectNum(1)
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
                .start(TesteTenorActivity.this);

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
                File outputFile = new File(getCacheDir(), fileName);
                destinationUri = Uri.fromFile(outputFile);
                //ToastCustomizado.toastCustomizado("Caminho: " + destinationUri, getApplicationContext());
                Log.d("Caminho ", String.valueOf(destinationUri));
                break; // Sai do loop após encontrar a primeira imagem
            }
        }

        return destinationUri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                Uri imagemRecortada = UCrop.getOutput(data);
                if (imagemRecortada != null) {
                    try{
                        uriSelecionada = imagemRecortada;

                        /*Funcionando bem - https://tfhub.dev/tensorflow/lite-model/efficientdet/lite4/detection/metadata/2
                        int inputSize = 640; // Tamanho esperado pelo modelo
                        Bitmap bitmapTeste = uriToBitmap(getApplicationContext(), uriSelecionada, inputSize);
                        configEfficientdet(bitmapTeste);
                         */

                        //MobileNetV3
                        logicaMobileNetv3(uriToBitmap(getApplicationContext(),uriSelecionada, 224));
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    // Função para converter a URI da imagem para ByteBuffer
    public static ByteBuffer uriToByteBuffer(Context context, Uri uri, int inputSize) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);

        // Verificar se o InputStream é nulo
        if (inputStream == null) {
            throw new IOException("Falha ao abrir o InputStream da URI");
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        // Verificar se o Bitmap é nulo ou inválido
        if (bitmap == null) {
            throw new IOException("Falha ao decodificar o Bitmap");
        }

        // Redimensionar o Bitmap para as dimensões esperadas pelo modelo (inputSize x inputSize)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);

        // Converter o Bitmap para um array de pixels no formato ARGB_8888
        int[] pixels = new int[inputSize * inputSize];
        resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

        // Criar um ByteBuffer para armazenar os valores dos pixels no formato esperado pelo modelo (UINT8)
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3); // 3 canais de cores (RGB)

        // Converter os valores dos pixels para o formato UINT8 (0 a 255) e armazená-los no ByteBuffer
        for (int pixelValue : pixels) {
            byteBuffer.put((byte) ((pixelValue >> 16) & 0xFF)); // Canal Vermelho
            byteBuffer.put((byte) ((pixelValue >> 8) & 0xFF));  // Canal Verde
            byteBuffer.put((byte) (pixelValue & 0xFF));         // Canal Azul
        }

        // Limpar recursos (opcional)
        bitmap.recycle();
        resizedBitmap.recycle();
        inputStream.close();

        // Resetar posição do ByteBuffer para 0 antes de retorná-lo
        byteBuffer.rewind();

        return byteBuffer;
    }

    private void configEfficientdet(Bitmap bitmap){
        try {
            LiteModelEfficientdetLite4DetectionMetadata2 modelV2 = LiteModelEfficientdetLite4DetectionMetadata2.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Runs model inference and gets result.
            LiteModelEfficientdetLite4DetectionMetadata2.Outputs outputs = modelV2.process(image);
            List<LiteModelEfficientdetLite4DetectionMetadata2.DetectionResult> detectionResults = outputs.getDetectionResultList();

            // Defina um valor de limiar (threshold) para o score
            float threshold = 0.5f; // Por exemplo, consideraremos apenas as detecções com score acima de 0.5

// Mostra as caixas delimitadoras na imagem original.
            Bitmap markedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true); // Cria uma cópia do bitmap original.

            // Crie um StringBuilder para armazenar as categorias e scores que atendem ao threshold
            StringBuilder resultString = new StringBuilder();

            for (LiteModelEfficientdetLite4DetectionMetadata2.DetectionResult detectionResult : detectionResults) {
                RectF location = detectionResult.getLocationAsRectF();
                String category = detectionResult.getCategoryAsString();
                float score = detectionResult.getScoreAsFloat();

                // Verifica se o score é maior que o limiar definido
                if (score > threshold) {
                    // Desenha a caixa delimitadora na imagem com o Canvas.

                    Canvas canvas = new Canvas(markedBitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(2);
                    canvas.drawRect(location, paint);

                    // Adicione o texto da categoria e pontuação acima da caixa delimitadora.
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(20);
                    canvas.drawText(category + " - " + score, location.left, location.top, paint);

                    // Adicione a categoria e score ao StringBuilder
                    resultString.append(category).append(" - ").append(score).append("\n");
                }
            }

            // Exibe a imagem marcada no ImageView.
            imgViewTesteTensor.setImageBitmap(markedBitmap);

            ToastCustomizado.toastCustomizado(resultString.toString(), getApplicationContext());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap uriToBitmap(Context context, Uri uri, int inputSize) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);

        // Verificar se o InputStream é nulo
        if (inputStream == null) {
            throw new IOException("Falha ao abrir o InputStream da URI");
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        // Verificar se o Bitmap é nulo ou inválido
        if (bitmap == null) {
            throw new IOException("Falha ao decodificar o Bitmap");
        }

        // Redimensionar o Bitmap para as dimensões esperadas pelo modelo (inputSize x inputSize)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);

        // Limpar recursos (opcional)
        bitmap.recycle();
        inputStream.close();

        return resizedBitmap;
    }






    private void logicaMobileNetv3(Bitmap bitmap){
        try {
            // Carregue o modelo do MobileNetV3
            LiteModelImagenetMobilenetV3Large100224Classification5Metadata1 modelV3 = LiteModelImagenetMobilenetV3Large100224Classification5Metadata1.newInstance(getApplicationContext());

            // Crie inputs para referência.
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Executa a inferência do modelo e obtém os resultados.
            LiteModelImagenetMobilenetV3Large100224Classification5Metadata1.Outputs outputs = modelV3.process(image);
            List<Category> outputFeature0 = outputs.getLogitAsCategoryList();

            // Mostra as categorias e scores acima do threshold em uma String
            StringBuilder resultString = new StringBuilder();

            // Loop sobre os resultados da detecção
            for (int i = 0; i < outputFeature0.size(); i++) {

                String category = outputFeature0.get(i).getLabel();
                float score = outputFeature0.get(i).getScore();

                // Verifica se o score é maior que o limiar definido
                if (score >= 6) {
                    // Adicione a categoria e score ao StringBuilder
                    resultString.append(category).append(" - ").append(score).append("\n");
                }
            }


            // Exiba a String com as categorias e scores acima do threshold no Toast
            ToastCustomizado.toastCustomizado(resultString.toString(), getApplicationContext());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}