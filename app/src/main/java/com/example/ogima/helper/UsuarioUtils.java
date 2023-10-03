package com.example.ogima.helper;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.model.Usuario;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class UsuarioUtils {

    public interface DeslogarUsuarioCallback{
        void onDeslogado();
    }

    public interface VerificaBlockCallback {
        void onBloqueado();

        void onDisponivel();

        void onError(String message);
    }

    public interface RecuperarTokenCallback {
        void onRecuperado(String token);

        void semToken();

        void onError(String message);
    }

    public interface VerificaOnlineCallback {
        void onOnline();

        void onOffline();

        void onError(String message);
    }

    public interface SinalizaAudioBottomCallback {
        void onSinalizado();

        void onError(String message);
    }

    public interface PostagemVisualizadaCallback {
        void onVisualizacao(boolean result);
    }

    public static String recuperarIdUserAtual() {
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (autenticacao.getCurrentUser() != null) {
            String emailUsuario = autenticacao.getCurrentUser().getEmail();
            if (emailUsuario != null && !emailUsuario.isEmpty()) {
                return Base64Custom.codificarBase64(emailUsuario);
            }
        }
        return null;
    }

    @NonNull
    public static String recuperarNomeConfigurado(@NonNull Usuario usuario) {
        String nomeRecuperado;
        nomeRecuperado = usuario.getNomeUsuario();
        return FormatarNomePesquisaUtils.formatarNomeParaPesquisa(nomeRecuperado);
    }

    public static void AtualizarStatusOnline(boolean statusOnline) {
        try {
            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
            FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
            String emailUsuario, idUsuario;

            emailUsuario = autenticacao.getCurrentUser().getEmail();
            idUsuario = Base64Custom.codificarBase64(emailUsuario);

            DatabaseReference salvarStatusOnlineRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("online");

            final String TAG = "StatusOnline";

            if (statusOnline) {
                salvarStatusOnlineRef.onDisconnect().setValue(false);
                Log.d(TAG, "Online");
            } else {
                Log.d(TAG, "Offline");
            }

            salvarStatusOnlineRef.setValue(statusOnline).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "Atualizado status - " + statusOnline);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Error " + e.getMessage());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void VerificaBlock(String idDestinatario, Context context, VerificaBlockCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String emailUsuario, idUsuario;

        emailUsuario = Objects.requireNonNull(autenticacao.getCurrentUser()).getEmail();
        idUsuario = Base64Custom.codificarBase64(Objects.requireNonNull(emailUsuario));

        DatabaseReference verificaBlockRef = firebaseRef.child("blockUser")
                .child(idUsuario).child(idDestinatario);

        verificaBlockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callback.onBloqueado();
                    ToastCustomizado.toastCustomizadoCurto("Usuário indisponível", context);
                } else {
                    callback.onDisponivel();
                }
                verificaBlockRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarTokenPeloFirebase(String idUser, RecuperarTokenCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference verificaTokenRef = firebaseRef.child("usuarios")
                .child(idUser).child("token");

        verificaTokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onRecuperado(snapshot.getValue(String.class));
                } else {
                    callback.semToken();
                }
                verificaTokenRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void verificarOnline(String idUser, VerificaOnlineCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference verificaOnlineRef = firebaseRef.child("usuarios")
                .child(idUser).child("online");

        verificaOnlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean statusOnline = snapshot.getValue(Boolean.class);
                    if (statusOnline) {
                        callback.onOnline();
                    } else {
                        callback.onOffline();
                    }
                } else {
                    callback.onOffline();
                }
                verificaOnlineRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void postagemJaVisualizada(String idUsuario, String idPost, PostagemVisualizadaCallback callback) {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference recuperarPostagensRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaPostagensVisualizadas");

        recuperarPostagensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> typeIndicator = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaPostagensVisualizadas = snapshot.getValue(typeIndicator);
                    if (listaPostagensVisualizadas != null && listaPostagensVisualizadas.size() > 0) {
                        callback.onVisualizacao(listaPostagensVisualizadas.contains(idPost));
                    } else {
                        callback.onVisualizacao(false);
                    }
                } else {
                    callback.onVisualizacao(false);
                }
                recuperarPostagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onVisualizacao(false);
            }
        });
    }

    public static void deslogarUsuario(Context context, DeslogarUsuarioCallback callback){
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (autenticacao.getCurrentUser() != null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.SEND_GOGL_ACCESS)
                    .requestEmail()
                    .build();
            GoogleSignInClient mSignInClient = GoogleSignIn.getClient(context, gso);
            FirebaseAuth.getInstance().signOut();
            mSignInClient.signOut();
            callback.onDeslogado();
        }
    }

    public static void exibirFotoPadrao(Context context, ImageView imgViewAlvo, String campo){
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals("foto")) {
                GlideCustomizado.loadDrawableCircular(context,
                        R.drawable.ic_menu_profile, imgViewAlvo, android.R.color.transparent);
            } else if (campo.equals("fundo")) {
                GlideCustomizado.loadDrawableImage(context,
                        R.drawable.placeholderuniverse, imgViewAlvo, android.R.color.transparent);
            }
        }
    }
}
