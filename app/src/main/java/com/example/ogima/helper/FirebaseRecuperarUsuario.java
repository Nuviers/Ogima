package com.example.ogima.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseRecuperarUsuario {

    public interface MontarAvisoChatCallback {
        void onNomesAvisoConfigurado(String nomeAfetado, String nomeLogado);

        void onError(String mensagem);
    }

    public interface RecuperaUsuarioCallback {
        void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia);

        void onError(String mensagem);
    }

    public interface RecuperaGrupoCallback {
        void onGrupoRecuperado(Grupo grupoAtual);

        void onError(String mensagem);
    }

    public interface RecuperaComunidadeCallback {
        void onComunidadeRecuperada(Comunidade comunidadeAtual);

        void onError(String mensagem);
    }

    public static void montarAvisoChat(String idAfetado, String idAtual, MontarAvisoChatCallback callback) {
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(idAfetado);
        DatabaseReference usuarioAtualRef = FirebaseDatabase.getInstance().getReference("usuarios").child(idAtual);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);
                    usuarioAtualRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                Usuario usuarioLogado = snapshot.getValue(Usuario.class);

                                String nomeUserRemovido;
                                String nomeUserAtual;

                                if (usuarioRecuperado.getExibirApelido().equals("sim")) {
                                    nomeUserRemovido = usuarioRecuperado.getApelidoUsuario();
                                } else {
                                    nomeUserRemovido = usuarioRecuperado.getNomeUsuario();
                                }

                                if (usuarioLogado.getExibirApelido().equals("sim")) {
                                    nomeUserAtual = usuarioLogado.getApelidoUsuario();
                                } else {
                                    nomeUserAtual = usuarioLogado.getNomeUsuario();
                                }
                                callback.onNomesAvisoConfigurado(nomeUserRemovido, nomeUserAtual);
                            }
                            usuarioAtualRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                usuarioRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperaUsuario(String idUsuario, RecuperaUsuarioCallback callback) {

        DatabaseReference usuarioRecuperadoRef = FirebaseDatabase.getInstance().getReference("usuarios").child(idUsuario);
        usuarioRecuperadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);

                    String nomeAjustado = "";
                    Boolean epilepsia = false;

                    if (usuarioRecuperado.getExibirApelido().equals("sim")) {
                        nomeAjustado = usuarioRecuperado.getApelidoUsuario();
                    } else {
                        nomeAjustado = usuarioRecuperado.getNomeUsuario();
                    }

                    if (usuarioRecuperado.getEpilepsia().equals("Sim")) {
                        epilepsia = true;
                    } else if (usuarioRecuperado.getEpilepsia().equals("Não")) {
                        epilepsia = false;
                    }

                    callback.onUsuarioRecuperado(usuarioRecuperado, nomeAjustado, epilepsia);
                }
                usuarioRecuperadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperaGrupo(String idGrupo, RecuperaGrupoCallback callback) {
        DatabaseReference grupoRecuperadoRef = FirebaseDatabase.getInstance().getReference("grupos").child(idGrupo);
        grupoRecuperadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoRecuperado = snapshot.getValue(Grupo.class);
                    callback.onGrupoRecuperado(grupoRecuperado);
                }
                grupoRecuperadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperaComunidade(String idComunidade, RecuperaComunidadeCallback callback) {
        DatabaseReference comunidadeRecuperadaRef = FirebaseDatabase.getInstance().getReference("comunidades").child(idComunidade);
        comunidadeRecuperadaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidadeRecuperada = snapshot.getValue(Comunidade.class);
                    callback.onComunidadeRecuperada(comunidadeRecuperada);
                }
                comunidadeRecuperadaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
