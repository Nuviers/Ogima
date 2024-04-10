package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ogima.R;
import com.example.ogima.model.Grupo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupUtils {
    private Activity activity;
    private Context context;
    public static final int MAX_NUMBER_PARTICIPANTS = 200;
    public static final int MAX_SELECTION = 40;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private int contTopicoRemocao = 0;

    public GroupUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public GroupUtils(Context context) {
        this.context = context;
    }

    public interface ConfigBundleCallback {
        void onCadastro();

        void onEdicao(Grupo dadosEdicao);

        void onSemDado();
    }

    public interface TopicosAnterioresCallback {
        void onConcluido(ArrayList<String> topicosAnteriores);

        void onError(String message);
    }

    public interface RecuperarTimestampCriacaoCallback {
        void onConcluido(long timestamp);

        void onError(String message);
    }

    public interface VerificaBlockCallback {
        void onBlock(boolean status);

        void onError(String message);
    }

    public interface VerificaParticipanteCallback {
        void onParticipante(boolean status);

        void onError(String message);
    }

    public interface RecuperaCargoCallback {
        void onConcluido(String cargo);

        void onError(String message);
    }

    public interface SairDoGrupoCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface RecuperarListaAdmsCallback {
        void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm);

        void onNaoExiste();

        void onError(String message);
    }

    public interface RecuperarListaParticipantesCallback {
        void onConcluido(ArrayList<String> idsParticipantes);

        void onError(String message);
    }

    public interface ExcluirGrupoCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface DesbloquearGrupoCallback {
        void onDesbloqueado();

        void onError(String message);
    }

    public interface BloquearGrupoCallback {
        void onBloqueado();

        void onError(String message);
    }

    public interface EnviarDenunciaCallback {
        void onConcluido();

        void onJaExisteDenuncia();

        void onError(String message);
    }

    public interface VerificaDenunciaCallback {
        void onDenuncia(boolean statusDenuncia);

        void onError(String message);
    }

    public interface PromoverAdmCallback {
        void onConcluido();

        void onNaoParticipa();

        void onError(String message);
    }

    public interface VerificaAdmCallback {
        void onRecuperado(boolean adm);

        void onError(String message);
    }

    public interface DespromoverAdmCallback {
        void onConcluido();

        void onNaoParticipa();

        void onError(String message);
    }

    public interface TransferirFundadorCallback {
        void onConcluido();

        void onLimiteMaxAtingido();

        void onNaoParticipante();

        void onError(String message);
    }

    public interface AjustarMeusGruposCallback {
        void onConcluido(ArrayList<String> idsUserAlvo, ArrayList<String> idsUserAtual);

        void onLimiteMaxAtingido();

        void onError(String message);
    }

    public interface AjustarIdsGrupoCallback {
        void onAjustado(ArrayList<String> listaIds);

        void onError(String message);
    }

    public void configurarBundle(Bundle dados, ConfigBundleCallback callback) {
        if (dados != null) {
            if (dados.containsKey("edit")) {
                boolean edicao;
                edicao = dados.getBoolean("edit");
                if (edicao) {
                    if (dados.containsKey("dadosEdicao")
                            && dados.getSerializable("dadosEdicao") != null) {
                        Grupo grupoEdicao = (Grupo) dados.getSerializable("dadosEdicao");
                        callback.onEdicao(grupoEdicao);
                    } else {
                        callback.onSemDado();
                    }
                    return;
                }
                callback.onCadastro();
            } else {
                callback.onSemDado();
            }
        } else {
            callback.onSemDado();
        }
    }

    public void recuperarTopicosAnteriores(String idGrupo, TopicosAnterioresCallback callback) {
        if (idGrupo == null || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarTopicosRef = firebaseRef.child("grupos")
                .child(idGrupo).child("topicos");
        recuperarTopicosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaTopicos = snapshot.getValue(t);
                    if (listaTopicos != null
                            && listaTopicos.size() > 0) {
                        callback.onConcluido(listaTopicos);
                    }
                }
                recuperarTopicosRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void recuperarTimestampCriacao(String idGrupo, RecuperarTimestampCriacaoCallback callback) {
        if (idGrupo == null || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarTimestampRef = firebaseRef.child("grupos")
                .child(idGrupo).child("timestampinteracao");
        recuperarTimestampRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    long timestamp = snapshot.getValue(Long.class);
                    callback.onConcluido(timestamp);
                } else {
                    TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                        @Override
                        public void onRecuperado(long timestampNegativo) {
                            callback.onConcluido(timestampNegativo);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                }
                recuperarTimestampRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void verificaBlock(String idAlvo, String idGrupo, VerificaBlockCallback callback) {
        if (idAlvo == null || idGrupo == null
                || idAlvo.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference verificaBlockRef = firebaseRef.child("blockGroup")
                .child(idAlvo).child(idGrupo);
        verificaBlockRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onBlock(snapshot.getValue() != null);
                verificaBlockRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void verificaSeEParticipante(String idGrupo, String idAlvo, VerificaParticipanteCallback callback) {
        DatabaseReference verificaParticipanteRef = firebaseRef.child("groupFollowers")
                .child(idGrupo).child(idAlvo);
        verificaParticipanteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onParticipante(true);
                } else {
                    callback.onParticipante(false);
                }
                verificaParticipanteRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void recuperaCargo(Grupo grupoAlvo, RecuperaCargoCallback callback) {

        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null
                || idUsuario.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        if (grupoAlvo == null) {
            callback.onError("Ocorreu um erro ao recuperar os dados do grupo.");
            return;
        }

        if (grupoAlvo.getIdSuperAdmGrupo() != null
                && !grupoAlvo.getIdSuperAdmGrupo().isEmpty()
                && grupoAlvo.getIdSuperAdmGrupo().equals(idUsuario)) {
            callback.onConcluido(CommunityUtils.FOUNDER_POSITION);
            return;
        }

        if (grupoAlvo.getAdmsGrupo() != null
                && !grupoAlvo.getAdmsGrupo().isEmpty()
                && grupoAlvo.getAdmsGrupo().contains(idUsuario)) {
            callback.onConcluido(CommunityUtils.ADM_POSITION);
            return;
        }

        verificaSeEParticipante(grupoAlvo.getIdGrupo(), idUsuario, new VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    callback.onConcluido(CommunityUtils.PARTICIPANT_POSITION);
                } else {
                    callback.onError("Você não faz parte desse grupo.");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError("Ocorreu um erro ao recuperar os dados do grupo.");
            }
        });
    }

    public void sairDoGrupo(String idGrupo, String idAlvo, SairDoGrupoCallback callback) {
        if (idAlvo == null || idGrupo == null
                || idAlvo.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaSeEParticipante(idGrupo, idAlvo, new VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    recuperarListaAdms(idGrupo, new RecuperarListaAdmsCallback() {
                        HashMap<String, Object> dadosOperacao = new HashMap<>();
                        String caminhoFollowers = "/groupFollowers/" + idGrupo + "/" + idAlvo;
                        String caminhoFollowing = "/groupFollowing/" + idAlvo + "/" + idGrupo;
                        String caminhoGrupo = "/grupos/" + idGrupo + "/";

                        @Override
                        public void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm) {
                            dadosOperacao.put(caminhoFollowers, null);
                            dadosOperacao.put(caminhoFollowing, null);
                            dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(-1));
                            if (usuarioAtualAdm) {
                                idsAdms.remove(idAlvo);
                                dadosOperacao.put(caminhoGrupo + "/admsGrupo/", idsAdms);
                                dadosOperacao.put(caminhoGrupo + "nrAdms", ServerValue.increment(-1));
                            }

                            recuperarListaParticipantes(idGrupo, new RecuperarListaParticipantesCallback() {
                                @Override
                                public void onConcluido(ArrayList<String> idsParticipantes) {
                                    if (idsParticipantes != null && !idsParticipantes.isEmpty()) {
                                        if (idsParticipantes.contains(idAlvo)) {
                                            idsParticipantes.remove(idAlvo);
                                        }
                                    }
                                    dadosOperacao.put(caminhoGrupo + "participantes", idsParticipantes);
                                    salvarHashmapSairDoGrupo(dadosOperacao, callback);
                                }

                                @Override
                                public void onError(String message) {
                                    callback.onError(message);
                                }
                            });
                        }

                        @Override
                        public void onNaoExiste() {
                            dadosOperacao.put(caminhoFollowers, null);
                            dadosOperacao.put(caminhoFollowing, null);
                            dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(-1));
                            salvarHashmapSairDoGrupo(dadosOperacao, callback);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                } else {
                    callback.onConcluido();
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void recuperarListaAdms(String idGrupo, RecuperarListaAdmsCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarListaRef = firebaseRef.child("grupos")
                .child(idGrupo)
                .child("admsGrupo");

        recuperarListaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean adm = false;
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaIds = snapshot.getValue(t);
                    if (listaIds != null
                            && !listaIds.isEmpty()) {
                        if (listaIds.contains(idUsuario)) {
                            adm = true;
                        }
                        callback.onConcluido(listaIds, adm);
                    } else {
                        callback.onNaoExiste();
                    }
                } else {
                    callback.onNaoExiste();
                }
                recuperarListaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    private void recuperarListaParticipantes(String idGrupo, RecuperarListaParticipantesCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarListaRef = firebaseRef.child("grupos")
                .child(idGrupo)
                .child("participantes");

        recuperarListaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaIds = snapshot.getValue(t);
                    if (listaIds != null
                            && !listaIds.isEmpty()) {
                        callback.onConcluido(listaIds);
                    } else {
                        callback.onConcluido(null);
                    }
                } else {
                    callback.onError("Ocorreu um erro ao recuperar os dados do grupo, tente novamente");
                }
                recuperarListaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void salvarHashmapSairDoGrupo(HashMap<String, Object> dadosOperacao, SairDoGrupoCallback callback) {
        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onConcluido();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public void excluirGrupo(Context context, String idGrupo, ExcluirGrupoCallback callback) {

        //Falta remover os interesses do grupo também

        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        recuperarTopicosAnteriores(idGrupo, new TopicosAnterioresCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoFollowers = "/groupFollowers/" + idGrupo;
            String caminhoGrupo = "/grupos/" + idGrupo;
            String caminhoGruposUsuario = "/usuarios/" + idUsuario + "/" + "idMeusGrupos/";
            String caminhoGrupoPublico = "/publicGroups/" + idGrupo;
            String caminhoGrupoPrivado = "/privateGroups/" + idGrupo;

            @Override
            public void onConcluido(ArrayList<String> topicosAnteriores) {
                for (String interesseRemover : topicosAnteriores) {
                    String caminhoInteresses = "/groupInterests/" + interesseRemover + "/" + idGrupo;
                    dadosOperacao.put(caminhoInteresses, null);
                    contTopicoRemocao++;
                    if (contTopicoRemocao == topicosAnteriores.size()) {
                        contTopicoRemocao = 0;
                        dadosOperacao.put(caminhoGrupoPublico, null);
                        dadosOperacao.put(caminhoGrupoPrivado, null);
                        dadosOperacao.put(caminhoFollowers, null);
                        dadosOperacao.put(caminhoGrupo, null);
                        UsuarioUtils.recuperarIdsGrupos(context, idUsuario, new UsuarioUtils.RecuperarIdsMeusGruposCallback() {
                            @Override
                            public void onRecuperado(ArrayList<String> idsGrupos) {
                                if (idsGrupos.contains(idUsuario)) {
                                    idsGrupos.remove(idUsuario);
                                    dadosOperacao.put(caminhoGruposUsuario, idsGrupos);
                                }
                                salvarHashmapExlusaoGrupo(dadosOperacao, callback);
                            }

                            @Override
                            public void onNaoExiste() {
                                salvarHashmapExlusaoGrupo(dadosOperacao, callback);
                            }

                            @Override
                            public void onError(String message) {
                                callback.onError(message);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void salvarHashmapExlusaoGrupo(HashMap<String, Object> dadosOperacao, ExcluirGrupoCallback callback) {
        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onConcluido();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public void desbloquearGrupo(String idGrupo, DesbloquearGrupoCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        HashMap<String, Object> dadosOperacao = new HashMap<>();
        String caminhoBlock = "/blockGroup/" + idUsuario + "/" + idGrupo;
        dadosOperacao.put(caminhoBlock, null);
        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onDesbloqueado();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public void bloquearGrupo(String idGrupo, BloquearGrupoCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        HashMap<String, Object> dadosOperacao = new HashMap<>();
        String caminhoBlock = "/blockGroup/" + idUsuario + "/" + idGrupo + "/";
        dadosOperacao.put(caminhoBlock + "idGrupo", idGrupo);
        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onBloqueado();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public void enviarDenunciaGrupo(String idGrupo, EnviarDenunciaCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        verificaSeExisteDenuncia(idGrupo, new VerificaDenunciaCallback() {
            @Override
            public void onDenuncia(boolean statusDenuncia) {
                if (statusDenuncia) {
                    callback.onJaExisteDenuncia();
                    return;
                }
                TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                    String caminhoDenuncia = "/groupReports/" + idGrupo + "/" + idUsuario + "/";
                    HashMap<String, Object> dadosOperacao = new HashMap<>();

                    @Override
                    public void onRecuperado(long timestampNegativo) {
                        dadosOperacao.put(caminhoDenuncia + "timestampinteracao", timestampNegativo);
                        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {
                                    callback.onConcluido();
                                } else {
                                    callback.onError(String.valueOf(error.getCode()));
                                }
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

    public void verificaSeExisteDenuncia(String idGrupo, VerificaDenunciaCallback callback) {

        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        DatabaseReference verificaDenunciaRef = firebaseRef.child("groupReports")
                .child(idGrupo).child(idUsuario);
        verificaDenunciaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onDenuncia(snapshot.getValue() != null);
                verificaDenunciaRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void promoverParaAdm(String idGrupo, String idAlvo, PromoverAdmCallback callback) {

        if (idAlvo == null || idGrupo == null
                || idAlvo.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaSeEParticipante(idGrupo, idAlvo, new VerificaParticipanteCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoFollowers = "/groupFollowers/" + idGrupo + "/" + idAlvo + "/";
            String caminhoGrupo = "/grupos/" + idGrupo + "/";
            ArrayList<String> listaPronta = new ArrayList<>();

            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    dadosOperacao.put(caminhoFollowers + "administrator", true);
                    dadosOperacao.put(caminhoGrupo + "nrAdms", ServerValue.increment(1));
                    recuperarListaAdms(idGrupo, new RecuperarListaAdmsCallback() {
                        @Override
                        public void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm) {
                            listaPronta = idsAdms;
                            listaPronta.add(idAlvo);
                            ToastCustomizado.toastCustomizadoCurto("ADD: " + idAlvo, context);
                            dadosOperacao.put(caminhoGrupo + "admsGrupo/", listaPronta);
                            salvarHashmapPromocao(dadosOperacao, callback);
                        }

                        @Override
                        public void onNaoExiste() {
                            ToastCustomizado.toastCustomizadoCurto("NOVO ADD: " + idAlvo, context);
                            listaPronta.add(idAlvo);
                            dadosOperacao.put(caminhoGrupo + "admsGrupo/", listaPronta);
                            salvarHashmapPromocao(dadosOperacao, callback);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                } else {
                    callback.onNaoParticipa();
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void salvarHashmapPromocao(HashMap<String, Object> dadosOperacao, PromoverAdmCallback callback) {
        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onConcluido();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }

    public void despromoverAdm(String idGrupo, String idAlvo, DespromoverAdmCallback callback) {

        if (idAlvo == null || idGrupo == null
                || idAlvo.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaSeEParticipante(idGrupo, idAlvo, new VerificaParticipanteCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoFollowers = "/groupFollowers/" + idGrupo + "/" + idAlvo + "/";
            String caminhoGrupo = "/grupos/" + idGrupo + "/";

            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    verificaAdm(idGrupo, idAlvo, new VerificaAdmCallback() {
                        @Override
                        public void onRecuperado(boolean adm) {
                            if (adm) {
                                //Usuário é realmente um dos adms.
                                dadosOperacao.put(caminhoFollowers + "administrator", false);
                                dadosOperacao.put(caminhoGrupo + "nrAdms", ServerValue.increment(-1));
                                recuperarListaAdms(idGrupo, new RecuperarListaAdmsCallback() {
                                    @Override
                                    public void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm) {
                                        idsAdms.remove(idAlvo);
                                        ToastCustomizado.toastCustomizado("DELL: " + idAlvo, context);
                                        if (idsAdms.size() <= 0) {
                                            dadosOperacao.put(caminhoGrupo + "admsGrupo/", null);
                                        } else {
                                            dadosOperacao.put(caminhoGrupo + "admsGrupo/", idsAdms);
                                        }
                                        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error == null) {
                                                    callback.onConcluido();
                                                } else {
                                                    callback.onError(String.valueOf(error.getCode()));
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onNaoExiste() {
                                        ToastCustomizado.toastCustomizado("NÃO É ADM: " + idAlvo, context);
                                        //Usuário não é mais o adm.
                                        callback.onConcluido();
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                            } else {
                                //Usuário não é adm.
                                callback.onConcluido();
                            }
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                } else {
                    callback.onNaoParticipa();
                }
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void verificaAdm(String idGrupo, String idAlvo, VerificaAdmCallback callback) {
        if (idAlvo == null || idGrupo == null
                || idAlvo.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        FirebaseRecuperarUsuario.recoverGroup(idGrupo, new FirebaseRecuperarUsuario.RecoverGroupCallback() {
            @Override
            public void onGrupoRecuperado(Grupo grupoAtual) {
                if (grupoAtual.getAdmsGrupo() != null
                        && grupoAtual.getAdmsGrupo().size() > 0
                        && grupoAtual.getAdmsGrupo().contains(idAlvo)) {
                    callback.onRecuperado(true);
                } else {
                    callback.onRecuperado(false);
                }
            }

            @Override
            public void onNaoExiste() {
                callback.onError("Esse grupo não existe mais.");
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public void transferirFundador(String idGrupo, String idAlvo, TransferirFundadorCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idUsuario.isEmpty() || idAlvo == null || idGrupo == null
                || idAlvo.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        ajustarIdGrupos(idAlvo, idGrupo, new AjustarMeusGruposCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoGrupo = "/grupos/" + idGrupo + "/";
            String usuarioAlvoCaminho = "/usuarios/" + idAlvo + "/idMeusGrupos/";
            String usuarioAtualCaminho = "/usuarios/" + idUsuario + "/idMeusGrupos/";
            String caminhoFollowers = "/groupFollowers/" + idGrupo + "/" + idAlvo;

            @Override
            public void onConcluido(ArrayList<String> idsUserAlvo, ArrayList<String> idsUserAtual) {
                dadosOperacao.put(caminhoFollowers, null);
                dadosOperacao.put(caminhoGrupo + "idSuperAdmGrupo", idAlvo);
                dadosOperacao.put(caminhoGrupo + "nrParticipantes", ServerValue.increment(-1));
                dadosOperacao.put(usuarioAlvoCaminho, idsUserAlvo);
                dadosOperacao.put(usuarioAtualCaminho, idsUserAtual);

                recuperarListaParticipantes(idGrupo, new RecuperarListaParticipantesCallback() {
                    @Override
                    public void onConcluido(ArrayList<String> idsParticipantes) {
                        if (idsParticipantes != null && !idsParticipantes.isEmpty()) {
                            if (idsParticipantes.contains(idAlvo)) {
                                idsParticipantes.remove(idAlvo);
                            }
                        }

                        dadosOperacao.put(caminhoGrupo + "participantes", idsParticipantes);

                        verificaAdm(idGrupo, idAlvo, new VerificaAdmCallback() {
                            @Override
                            public void onRecuperado(boolean adm) {
                                if (adm) {
                                    //Usuário escolhido é adm.
                                    despromoverAdm(idGrupo, idAlvo, new DespromoverAdmCallback() {
                                        @Override
                                        public void onConcluido() {
                                            salvarHashMapTransferirFundador(dadosOperacao, callback);
                                        }

                                        @Override
                                        public void onNaoParticipa() {
                                            callback.onNaoParticipante();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            callback.onError(message);
                                        }
                                    });
                                } else {
                                    //Usuário não é adm.
                                    salvarHashMapTransferirFundador(dadosOperacao, callback);
                                }
                            }

                            @Override
                            public void onError(String message) {
                                callback.onError(message);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onLimiteMaxAtingido() {
                callback.onLimiteMaxAtingido();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void ajustarIdGrupos(String idAlvo, String idGrupo, AjustarMeusGruposCallback callback) {
        //Verificar se usuário selecionado tem o limite máximo de grupo ou não.
        UsuarioUtils.recuperarIdsGrupos(context, idAlvo, new UsuarioUtils.RecuperarIdsMeusGruposCallback() {
            @Override
            public void onRecuperado(ArrayList<String> idsUserAlvo) {
                if (idsUserAlvo == null) {
                    callback.onError("");
                    return;
                }
                if (idsUserAlvo.size() >= 5) {
                    callback.onLimiteMaxAtingido();
                    return;
                }
                idsUserAlvo.add(idGrupo);
                removerIdMeusGrupos(idGrupo, new AjustarIdsGrupoCallback() {
                    @Override
                    public void onAjustado(ArrayList<String> listaIds) {
                        callback.onConcluido(idsUserAlvo, listaIds);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onNaoExiste() {
                ArrayList<String> idsUserAlvo = new ArrayList<>();
                idsUserAlvo.add(idGrupo);
                removerIdMeusGrupos(idGrupo, new AjustarIdsGrupoCallback() {
                    @Override
                    public void onAjustado(ArrayList<String> listaIds) {
                        callback.onConcluido(idsUserAlvo, listaIds);
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

    private void removerIdMeusGrupos(String idGrupo, AjustarIdsGrupoCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idGrupo == null
                || idUsuario.isEmpty() || idGrupo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        UsuarioUtils.recuperarIdsGrupos(context, idUsuario, new UsuarioUtils.RecuperarIdsMeusGruposCallback() {
            @Override
            public void onRecuperado(ArrayList<String> idsUserAtual) {
                if (idsUserAtual == null) {
                    callback.onError("");
                    return;
                }
                if (idsUserAtual.contains(idGrupo)) {
                    idsUserAtual.remove(idGrupo);
                }
                callback.onAjustado(idsUserAtual);
            }

            @Override
            public void onNaoExiste() {
                callback.onAjustado(null);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void salvarHashMapTransferirFundador(HashMap<String, Object> dadosOperacao, TransferirFundadorCallback callback) {
        firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    callback.onConcluido();
                } else {
                    callback.onError(String.valueOf(error.getCode()));
                }
            }
        });
    }
}
