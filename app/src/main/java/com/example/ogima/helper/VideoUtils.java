package com.example.ogima.helper;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class VideoUtils {

    public static int MIN_CRF_1080P = 20;
    public static int MAX_CRF_1080P = 26;
    public static int MIN_CRF_720P = 23;
    public static int MAX_CRF_720P = 28;
    public static int MIN_CRF_480P = 24;
    public static int MAX_CRF_480P = 30;

    public interface VideoInfoCallback {
        void onVideoInfoReceived(long durationMs, float frameRate, long fileSizeBytes, int width, int height, int bitsRate);
    }

    private static final String TAG = "VideoUtils";

    public static void getVideoInfo(Context context, Uri videoUri, VideoInfoCallback callback) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, videoUri, null);

            long frameCount = 0;

            int trackCount = extractor.getTrackCount();
            int videoTrackIndex = -1;

            // Encontra a faixa de vídeo
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mimeType = format.getString(MediaFormat.KEY_MIME);
                if (mimeType != null && mimeType.startsWith("video/")) {
                    extractor.selectTrack(i);

                    long sampleTime;
                    int count = 0;
                    while ((sampleTime = extractor.getSampleTime()) != -1) {
                        count++;
                        extractor.advance();
                    }
                    frameCount = count;

                    videoTrackIndex = i;
                    break;
                }
            }

            if (videoTrackIndex >= 0) {
                MediaFormat videoFormat = extractor.getTrackFormat(videoTrackIndex);

                // Duração do vídeo em microssegundos
                long durationUs = videoFormat.getLong(MediaFormat.KEY_DURATION);
                long durationMs = durationUs / 1000;
                Log.d(TAG, "Duração do vídeo: " + durationMs + " ms");

                float frameRate = (float) frameCount * 1000 / durationMs;
                Log.d(TAG, "Taxa de quadros do vídeo: " + frameRate + " fps");

                // Tamanho do arquivo em bytes
                String filePath = getFilePathFromUri(context, videoUri);
                File videoFile = new File(filePath);
                long fileSizeBytes = videoFile.length();
                Log.d(TAG, "Tamanho do arquivo: " + fileSizeBytes + " bytes");

                // Resolução do vídeo
                int width = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
                int height = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);
                Log.d(TAG, "Resolução do vídeo: " + width + "x" + height);
                extractor.release();

                // Cálculo da taxa de bits
                int bitrate = (int) (fileSizeBytes * 8 / durationMs);

                Log.d(TAG, "Taxa de bits original: " + bitrate + "k");
                if (callback != null) {
                    callback.onVideoInfoReceived(durationMs, frameRate, fileSizeBytes, width, height, bitrate);
                }
            } else {
                // Não foi encontrada nenhuma faixa de vídeo
                Log.d(TAG, "Nenhuma faixa de vídeo encontrada");
                extractor.release();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            extractor.release();
        }
    }

    private static String getFilePathFromUri(Context context, Uri uri) {
        String filePath = null;
        if (uri.getScheme().equals("file")) {
            filePath = uri.getPath();
        } else {
            String[] projection = {android.provider.MediaStore.Video.Media.DATA};
            android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Video.Media.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        return filePath;
    }
}