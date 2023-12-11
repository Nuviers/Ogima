package com.example.ogima.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.ogima.BuildConfig;
import com.example.ogima.R;
import com.example.ogima.model.Comunidade;
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

    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_BACKGROUND = "background";
    public static final int MAX_NAME_LENGHT = 20;
    public static final int MAX_NAME_LENGHT_FOUNDER = 33;
    public static final int MAX_COMMUNITY_NAME_LENGHT = 25;
    public static final int MAX_COMMUNITY_PREVIEW_DESC_LENGHT = 61;

    public interface DeslogarUsuarioCallback {
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

    public interface VerificaEpilepsiaCallback {
        void onConcluido(boolean epilepsia);

        void onSemDado();

        void onError(String message);
    }

    public interface CheckLockCallback {
        void onBlocked(boolean status);

        void onError(String message);
    }

    public interface DadosPadraoCallback {
        void onRecuperado(String nome, String foto, String fundo);

        void onSemDado();

        void onError(String message);
    }

    private interface RecuperarFotoCallback {
        void onRecuperado(String foto);

        void onError(String message);
    }

    private interface RecuperarNomeCallback{
        void onRecuperado(String nome);
        void onError(String message);
    }

    public interface RecuperarIdsMinhasComunidadesCallback{
        void onRecuperado(ArrayList<String> idsComunidades);
        void onNaoExiste();
        void onError(String message);
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

    @NonNull
    public static String recuperarNomeConfiguradoComunidade(@NonNull Comunidade comunidade) {
        String nomeRecuperado;
        nomeRecuperado = comunidade.getNomeComunidade();
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

    public static void verificaBlock(String idDestinatario, Context context, boolean exibirToast, VerificaBlockCallback callback) {
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
                    if (exibirToast) {
                        ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.user_unavailable), context);
                    }
                    callback.onBloqueado();
                } else {
                    callback.onDisponivel();
                }
                verificaBlockRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
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

    public static void deslogarUsuario(Context context, DeslogarUsuarioCallback callback) {
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

    public static void exibirFotoPadrao(Context context, ImageView imgViewAlvo, String campo, boolean removerBackground) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(FIELD_PHOTO)) {
                if (removerBackground) {
                    imgViewAlvo.setBackgroundResource(android.R.color.transparent);
                }
                GlideCustomizado.loadDrawableCircular(context,
                        R.drawable.ic_menu_profile, imgViewAlvo, android.R.color.transparent);
            } else if (campo.equals(FIELD_BACKGROUND)) {
                GlideCustomizado.loadDrawableImage(context,
                        R.drawable.placeholderuniverse, imgViewAlvo, android.R.color.transparent);
            }
        }
    }

    public static void verificaEpilepsia(String idUser, VerificaEpilepsiaCallback callback) {
        if (idUser != null && !idUser.isEmpty()) {
            DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
            DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                    .child(idUser).child("statusEpilepsia");
            usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        boolean epilepsia = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        callback.onConcluido(epilepsia);
                    } else {
                        callback.onSemDado();
                    }
                    usuarioRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        } else {
            callback.onSemDado();
        }
    }

    public static void checkBlockingStatus(Context context, String idSelecionado, CheckLockCallback callback) {
        String idUsuario = "";
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null
                || idUsuario.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        if (idSelecionado.equals(idUsuario)) {
            callback.onBlocked(false);
            return;
        }
        UsuarioUtils.verificaBlock(idSelecionado, context, false, new UsuarioUtils.VerificaBlockCallback() {
            @Override
            public void onBloqueado() {
                callback.onBlocked(true);
            }

            @Override
            public void onDisponivel() {
                callback.onBlocked(false);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static void recuperarDadosPadrao(Context context, String idAlvo, DadosPadraoCallback callback) {
        if (idAlvo == null
                || idAlvo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        recuperarFoto(context, idAlvo, new RecuperarFotoCallback() {
            @Override
            public void onRecuperado(String foto) {
                recuperarFundo(context, idAlvo, new RecuperarFotoCallback() {
                    @Override
                    public void onRecuperado(String fundo) {
                        recuperarNome(context, idAlvo, new RecuperarNomeCallback() {
                            @Override
                            public void onRecuperado(String nome) {
                                callback.onRecuperado(nome, foto, fundo);
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

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public static void recuperarFoto(Context context, String idAlvo, RecuperarFotoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recuperarFotoRef = firebaseRef.child("usuarios").child(idAlvo).child("minhaFoto");
        recuperarFotoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String urlFoto = "";
                if (snapshot.getValue() != null) {
                    urlFoto = snapshot.getValue(String.class);
                }
                recuperarFotoRef.removeEventListener(this);
                callback.onRecuperado(urlFoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public static void recuperarFundo(Context context, String idAlvo, RecuperarFotoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recuperarFundoRef = firebaseRef.child("usuarios").child(idAlvo).child("meuFundo");
        recuperarFundoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String urlFoto = "";
                if (snapshot.getValue() != null) {
                    urlFoto = snapshot.getValue(String.class);
                }
                recuperarFundoRef.removeEventListener(this);
                callback.onRecuperado(urlFoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public static void recuperarNome(Context context, String idAlvo, RecuperarNomeCallback callback){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recuperarNomeRef = firebaseRef.child("usuarios").child(idAlvo).child("nomeUsuario");
        recuperarNomeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nome = "";
                if (snapshot.getValue() != null) {
                    nome = snapshot.getValue(String.class);
                }
                callback.onRecuperado(nome);
                recuperarNomeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public static void recuperarIdsComunidades(Context context, String idAlvo, RecuperarIdsMinhasComunidadesCallback callback){
        if (idAlvo == null
                || idAlvo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recuperarIdsRef = firebaseRef.child("usuarios")
                .child(idAlvo).child("idMinhasComunidades");
        recuperarIdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> listaIds = snapshot.getValue(t);
                    if (listaIds != null
                            && listaIds.size() > 0) {
                        callback.onRecuperado(listaIds);
                    }else{
                        callback.onNaoExiste();
                    }
                }else{
                    callback.onNaoExiste();
                }
                recuperarIdsRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }
}
