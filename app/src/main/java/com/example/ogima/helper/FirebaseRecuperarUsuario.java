package com.example.ogima.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Grupo;
import com.example.ogima.model.Postagem;
import com.example.ogima.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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

    public interface RecuperaUsuarioCompletoCallback {
        void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado,
                                 Boolean epilepsia, ArrayList<String> listaIdAmigos,
                                 ArrayList<String> listaIdSeguindo, String fotoUsuario,
                                 String fundoUsuario);

        void onSemDados();

        void onError(String mensagem);
    }

    public interface RecuperaComunidadeDetalhesCallback {
        void onComunidadeRecuperada(Comunidade comunidadeAtual, String idFundador, ArrayList<String> idsAdms, boolean existemAdms);

        void semDados(boolean semDados);

        void onError(String mensagem);
    }

    public interface RecuperaPostagemComunidadeCallback {
        void onPostagemComunidadeRecuperada(Postagem postagemAtual);

        void semDados(boolean semDados);

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

    public static void recuperaComunidadeDetalhes(String idComunidade, RecuperaComunidadeDetalhesCallback callback) {

        GenericTypeIndicator<ArrayList<String>> typeIndicatorArray = new GenericTypeIndicator<ArrayList<String>>() {
        };

        DatabaseReference comunidadeRecuperadaRef = FirebaseDatabase.getInstance().getReference("comunidades").child(idComunidade);
        comunidadeRecuperadaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {

                    boolean existemAdms = false;

                    Comunidade comunidadeRecuperada = snapshot.getValue(Comunidade.class);
                    String idFundador = comunidadeRecuperada.getIdSuperAdmComunidade();
                    ArrayList<String> idsAdms = new ArrayList<>();

                    if (comunidadeRecuperada.getAdmsComunidade() != null
                            && comunidadeRecuperada.getAdmsComunidade().size() >= 0) {
                        idsAdms.addAll(comunidadeRecuperada.getAdmsComunidade());
                        existemAdms = true;
                    } else {
                        existemAdms = false;
                    }

                    callback.onComunidadeRecuperada(comunidadeRecuperada, idFundador, idsAdms, existemAdms);
                } else {
                    callback.semDados(true);
                }
                comunidadeRecuperadaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
                callback.semDados(true);
            }
        });
    }

    public static void recuperaPostagemComunidade(String idComunidade, String idPostagem, RecuperaPostagemComunidadeCallback callback) {
        DatabaseReference postagemRecuperadaRef = FirebaseDatabase.getInstance().getReference("postagensComunidade").child(idComunidade)
                .child(idPostagem);
        postagemRecuperadaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Postagem postagemRecuperada = snapshot.getValue(Postagem.class);
                    callback.onPostagemComunidadeRecuperada(postagemRecuperada);
                    callback.semDados(true);
                } else {
                    callback.semDados(true);
                }
                postagemRecuperadaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
                callback.semDados(true);
            }
        });
    }

    public static void recuperaUsuarioCompleto(String idUsuario, RecuperaUsuarioCompletoCallback callback) {

        DatabaseReference usuarioRecuperadoRef = FirebaseDatabase.getInstance().getReference("usuarios").child(idUsuario);
        usuarioRecuperadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioRecuperado = snapshot.getValue(Usuario.class);

                    String nomeAjustado = null;
                    String fotoUsuario = null;
                    String fundoUsuario = null;
                    Boolean epilepsia = true;

                    if (usuarioRecuperado.getApelidoUsuario() != null
                            && !usuarioRecuperado.getApelidoUsuario().isEmpty()
                            && usuarioRecuperado.getExibirApelido().equals("sim")) {
                        nomeAjustado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioRecuperado.getApelidoUsuario());
                    } else if (usuarioRecuperado.getNomeUsuario() != null
                            && !usuarioRecuperado.getNomeUsuario().isEmpty()
                            && usuarioRecuperado.getExibirApelido().equals("não")) {
                        nomeAjustado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioRecuperado.getNomeUsuario());
                    }

                    if (usuarioRecuperado.getEpilepsia().equals("Sim")) {
                        epilepsia = true;
                    } else if (usuarioRecuperado.getEpilepsia().equals("Não")) {
                        epilepsia = false;
                    }

                    ArrayList<String> listaIdAmigos = new ArrayList<>();
                    ArrayList<String> listaIdSeguindo = new ArrayList<>();

                    if (usuarioRecuperado.getListaIdAmigos() != null &&
                            usuarioRecuperado.getListaIdAmigos().size() > 0) {
                        listaIdAmigos = usuarioRecuperado.getListaIdAmigos();
                    }

                    if (usuarioRecuperado.getListaIdSeguindo() != null &&
                            usuarioRecuperado.getListaIdSeguindo().size() > 0) {
                        listaIdSeguindo = usuarioRecuperado.getListaIdSeguindo();
                    }

                    if (usuarioRecuperado.getMinhaFoto() != null
                            && !usuarioRecuperado.getMinhaFoto().isEmpty()) {
                        fotoUsuario = usuarioRecuperado.getMinhaFoto();
                    }

                    if (usuarioRecuperado.getMeuFundo() != null
                            && !usuarioRecuperado.getMeuFundo().isEmpty()) {
                        fundoUsuario = usuarioRecuperado.getMeuFundo();
                    }

                    callback.onUsuarioRecuperado(usuarioRecuperado, nomeAjustado, epilepsia, listaIdAmigos, listaIdSeguindo, fotoUsuario, fundoUsuario);
                } else {
                    callback.onSemDados();
                }
                usuarioRecuperadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
