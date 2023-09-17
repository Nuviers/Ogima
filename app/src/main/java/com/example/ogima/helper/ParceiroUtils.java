package com.example.ogima.helper;

import androidx.annotation.NonNull;

import com.example.ogima.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ParceiroUtils {
    public interface RecuperarUserParcCallback {
        void onRecuperado(Usuario usuario, String nome,
                          String orientacao, String exibirPerfilPara,
                          String idUserParc, ArrayList<String> listaHobbies,
                          ArrayList<String> listaFotos, ArrayList<String> listaIdsAEsconder);

        void onSemDados();

        void onError(String message);
    }

    public interface RecuperarNomeCallback {
        void onRecuperado(String nome);

        void onSemDados();

        void onError(String message);
    }

    public interface RecuperarOrientacaoCallback {
        void onRecuperado(String orientacaoSexual);

        void onSemDados();

        void onError(String message);
    }

    public interface RecuperarExibirPerfilAlvoCallback {
        void onRecuperado(String exibirPerfilPara);

        void onSemDados();

        void onError(String message);
    }

    public interface RecuperarIdsEscondidosCallback {
        void onRecuperado(ArrayList<String> idsEscondidos);

        void onSemDados();

        void onError(String message);
    }

    public interface RecuperarHobbiesCallback {
        void onRecuperado(ArrayList<String> listaHobbies);

        void onSemDados();

        void onError(String message);
    }

    public interface RecuperarFotosCallback {
        void onRecuperado(ArrayList<String> listaFotos);

        void onSemDados();

        void onError(String message);
    }

    public static void recuperarDados(String idUser, RecuperarUserParcCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupDadosRef = firebaseRef.child("usuarioParc")
                .child(idUser);
        recupDadosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    String nome = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuario.getNomeParc());
                    String orientacao = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuario.getOrientacaoSexual());
                    String exibirPerfilPara = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuario.getExibirPerfilPara());
                    String idUserParc = usuario.getIdUsuario();
                    ArrayList<String> listaHobbies = new ArrayList<>();
                    listaHobbies = usuario.getListaInteressesParc();
                    if (listaHobbies != null && listaHobbies.size() > 0) {
                    } else {
                        listaHobbies = null;
                    }
                    ArrayList<String> listaFotos = new ArrayList<>();
                    listaFotos = usuario.getFotosParc();
                    if (listaFotos != null && listaFotos.size() > 0) {
                    } else {
                        listaFotos = null;
                    }
                    ArrayList<String> listaIdsAEsconder = new ArrayList<>();
                    listaIdsAEsconder = usuario.getIdsEsconderParc();
                    if (listaIdsAEsconder != null && listaIdsAEsconder.size() > 0) {
                    } else {
                        listaIdsAEsconder = null;
                    }
                    callback.onRecuperado(snapshot.getValue(Usuario.class), nome, orientacao, exibirPerfilPara, idUserParc, listaHobbies, listaFotos, listaIdsAEsconder);
                } else {
                    callback.onSemDados();
                }
                recupDadosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarNome(String idUser, RecuperarNomeCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupNomeRef = firebaseRef.child("usuarioParc")
                .child(idUser).child("nomeParc");
        recupNomeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String nome = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(snapshot.getValue(String.class));
                    callback.onRecuperado(nome);
                } else {
                    callback.onSemDados();
                }
                recupNomeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarOrientacao(String idUser, RecuperarOrientacaoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupOrientacaoRef = firebaseRef.child("usuarioParc")
                .child(idUser).child("orientacaoSexual");
        recupOrientacaoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String orientacaoSexual = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(snapshot.getValue(String.class));
                    callback.onRecuperado(orientacaoSexual);
                } else {
                    callback.onSemDados();
                }
                recupOrientacaoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarExibirPerfilAlvo(String idUser, RecuperarExibirPerfilAlvoCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupExibirPerfilParaRef = firebaseRef.child("usuarioParc")
                .child(idUser).child("exibirPerfilPara");
        recupExibirPerfilParaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String exibirPerfilPara = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(snapshot.getValue(String.class));
                    callback.onRecuperado(exibirPerfilPara);
                } else {
                    callback.onSemDados();
                }
                recupExibirPerfilParaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarIdsEscondidos(String idUser, RecuperarIdsEscondidosCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupIdsEscondidosRef = firebaseRef.child("usuarioParc")
                .child(idUser).child("idsEsconderParc");
        recupIdsEscondidosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> typeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> listaIdsEscondidos = snapshot.getValue(typeIndicator);
                    if (listaIdsEscondidos != null && listaIdsEscondidos.size() > 0) {
                        callback.onRecuperado(listaIdsEscondidos);
                    } else {
                        callback.onSemDados();
                    }
                } else {
                    callback.onSemDados();
                }
                recupIdsEscondidosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarHobbies(String idUser, RecuperarHobbiesCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupHobbiesRef = firebaseRef.child("usuarioParc")
                .child(idUser).child("listaInteressesParc");
        recupHobbiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> typeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> listaHobbies = snapshot.getValue(typeIndicator);
                    if (listaHobbies != null && listaHobbies.size() > 0) {
                        callback.onRecuperado(listaHobbies);
                    } else {
                        callback.onSemDados();
                    }
                } else {
                    callback.onSemDados();
                }
                recupHobbiesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarFotos(String idUser, RecuperarFotosCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference recupFotosRef = firebaseRef.child("usuarioParc")
                .child(idUser).child("fotosParc");
        recupFotosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> typeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                    ArrayList<String> listaFotos = snapshot.getValue(typeIndicator);
                    if (listaFotos != null && listaFotos.size() > 0) {
                        callback.onRecuperado(listaFotos);
                    } else {
                        callback.onSemDados();
                    }
                } else {
                    callback.onSemDados();
                }
                recupFotosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
