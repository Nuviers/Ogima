package com.example.ogima.helper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ogima.R;
import com.example.ogima.model.Comunidade;
import com.example.ogima.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommunityUtils {
    private Activity activity;
    private Context context;
    public static final String FIELD_PHOTO = "photo";
    public static final String FIELD_BACKGROUND = "background";
    public static final int MAX_LENGHT_NAME = 100;
    public static final int MIN_LENGHT_NAME = 10;
    public static final int MAX_LENGHT_DESCRIPTION = 200;
    public static final int MIN_LENGHT_DESCRIPTION = 10;
    public static final int MAX_LENGTH_TOPICOS = 15;
    public static final int MIN_LENGTH_TOPICOS = 1;
    public static final int MAX_NUMBER_PARTICIPANTS = 40;
    public static final int MAX_NUMBER_ADMS = 5;
    public static final String ADM_POSITION = "ADM";
    public static final String FOUNDER_POSITION = "FOUNDER";
    public static final String PARTICIPANT_POSITION = "PARTICIPANT";
    public static final String FUNCTION_ADD = "FUNCTION_ADD";
    public static final String FUNCTION_REMOVE = "FUNCTION_REMOVE";
    public static final String FUNCTION_DEMOTING = "FUNCTION_DEMOTING";
    public static final String FUNCTION_PROMOTE = "FUNCTION_PROMOTE";
    public static final String FUNCTION_NEW_FOUNDER = "FUNCTION_NEW_FOUNDER";
    public static final String FUNCTION_SET = "FUNCTION_SET";
    public static final String PUBLIC_COMMUNITIES = "Comunidades públicas";
    public static final String MY_COMMUNITIES = "Suas comunidades";
    public static final String COMMUNITIES_FOLLOWING = "Comunidades que você segue";
    public static final String RECOMMENDED_COMMUNITIES = "Comunidades recomendadas";
    public static final String ALL_COMMUNITIES = "Todas as comunidades";
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private int cont = 0;

    public CommunityUtils(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public CommunityUtils(Context context) {
        this.context = context;
    }

    public interface ConfigBundleCallback {
        void onCadastro();

        void onEdicao(Comunidade comunidadeEdicao);

        void onSemDado();
    }

    public interface VerificaParticipanteCallback {
        void onParticipante(boolean status);

        void onError(String message);
    }

    public interface ParticiparDaComunidadeCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface VerificaFundadorCallback {
        void onConcluido(boolean fundador);

        void onError(String message);
    }

    public interface RecuperaCargoCallback {
        void onConcluido(String cargo);

        void onError(String message);
    }

    public interface SairDaComunidadeCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface RecuperarListaAdmsCallback {
        void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm);

        void onNaoExiste();

        void onError(String message);
    }

    public interface ExcluirComunidadeCallback {
        void onConcluido();

        void onError(String message);
    }

    public interface PrepararListaCallback {
        void onConcluido(List<Usuario> listaAjustada);

        void onSemDados(String message);

        void onError(String error);
    }

    public interface VerificaConviteCallback {
        void onExiste();

        void onNaoExiste();

        void onError(String message);
    }

    public interface EnviarConviteCallback {
        void onEnviado();

        void onError(String message);
    }

    public interface PromoverAdmCallback {
        void onConcluido();

        void onNaoParticipa();

        void onError(String message);
    }

    public interface DespromoverAdmCallback {
        void onConcluido();

        void onNaoParticipa();

        void onError(String message);
    }

    public interface VerificaAdmCallback {
        void onRecuperado(boolean adm);

        void onError(String message);
    }

    public interface TransferirFundadorCallback {
        void onConcluido();

        void onLimiteMaxAtingido();

        void onNaoParticipante();

        void onError(String message);
    }

    public interface AjustarMinhasComunidadesCallback {
        void onConcluido(ArrayList<String> idsUserAlvo, ArrayList<String> idsUserAtual);

        void onLimiteMaxAtingido();

        void onError(String message);
    }

    public interface AjustarIdsComunidadeCallback {
        void onAjustado(ArrayList<String> listaIds);

        void onError(String message);
    }

    public interface BloquearComunidadeCallback {
        void onBloqueado();

        void onError(String message);
    }

    public interface DesbloquearComunidadeCallback {
        void onDesbloqueado();

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

    public interface VerificaBlockCallback {
        void onBlock(boolean status);

        void onError(String message);
    }

    public interface RecusarConviteCallback {
        void onConcluido();

        void onNaoExiste();

        void onError(String message);
    }

    public interface AceitarConviteCallback {
        void onConcluido();

        void onBlocked();

        void onNaoExiste();

        void onError(String message);
    }

    public interface VerificaSeComunidadeExisteCallback {
        void onStatus(boolean comunidadeExiste);

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
                        Comunidade comunidadeEdicao = (Comunidade) dados.getSerializable("dadosEdicao");
                        callback.onEdicao(comunidadeEdicao);
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

    public static void exibirFotoPadrao(Context context, ImageView imgViewAlvo, String campo, boolean removerBackground) {
        if (campo != null && !campo.isEmpty()) {
            if (campo.equals(FIELD_PHOTO)) {
                if (removerBackground) {
                    imgViewAlvo.setBackgroundResource(android.R.color.transparent);
                }
                GlideCustomizado.loadDrawableCircular(context,
                        R.drawable.ic_comunidade_padrao, imgViewAlvo, android.R.color.transparent);
            } else if (campo.equals(FIELD_BACKGROUND)) {
                GlideCustomizado.loadDrawableImage(context,
                        R.drawable.placeholderuniverse, imgViewAlvo, android.R.color.transparent);
            }
        }
    }

    public void verificaSeEParticipante(String idComunidade, String idAlvo, VerificaParticipanteCallback callback) {
        DatabaseReference verificaParticipanteRef = firebaseRef.child("communityFollowers")
                .child(idComunidade).child(idAlvo);
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

    public void participarDaComunidade(String idComunidade, ParticiparDaComunidadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoFollowers = "/communityFollowers/" + idComunidade + "/" + idUsuario + "/";
            String caminhoFollowing = "/communityFollowing/" + idUsuario + "/" + idComunidade + "/";
            String caminhoComunidade = "/comunidades/" + idComunidade + "/";
            String caminhoConvites = "/convitesComunidade/" + idUsuario + "/" + idComunidade;

            @Override
            public void onRecuperado(long timestampNegativo) {
                dadosOperacao.put(caminhoComunidade + "nrParticipantes", ServerValue.increment(1));
                dadosOperacao.put(caminhoFollowers + "timestampinteracao", timestampNegativo);
                dadosOperacao.put(caminhoFollowers + "idParticipante", idUsuario);
                dadosOperacao.put(caminhoFollowers + "administrator", false);
                dadosOperacao.put(caminhoConvites, null);
                dadosOperacao.put(caminhoFollowing + "idComunidade", idComunidade);
                dadosOperacao.put(caminhoFollowing + "timestampinteracao", timestampNegativo);
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

    public void verificaFundador(String idAlvo, String idComunidade, VerificaFundadorCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        DatabaseReference verificaFundadorRef = firebaseRef.child("comunidades")
                .child(idComunidade).child("idSuperAdmComunidade");
        verificaFundadorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    String idFundador = snapshot.getValue(String.class);
                    if (idFundador != null
                            && !idFundador.isEmpty() && idFundador.equals(idUsuario)) {
                        callback.onConcluido(true);
                    } else {
                        callback.onConcluido(false);
                    }
                } else {
                    callback.onConcluido(false);
                }
                verificaFundadorRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void recuperaCargo(Comunidade comunidadeAlvo, RecuperaCargoCallback callback) {

        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null
                || idUsuario.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        if (comunidadeAlvo == null) {
            callback.onError("Ocorreu um erro ao recuperar os dados da comunidade.");
            return;
        }

        if (comunidadeAlvo.getIdSuperAdmComunidade() != null
                && !comunidadeAlvo.getIdSuperAdmComunidade().isEmpty()
                && comunidadeAlvo.getIdSuperAdmComunidade().equals(idUsuario)) {
            callback.onConcluido(FOUNDER_POSITION);
            return;
        }

        if (comunidadeAlvo.getAdmsComunidade() != null
                && comunidadeAlvo.getAdmsComunidade().size() > 0
                && comunidadeAlvo.getAdmsComunidade().contains(idUsuario)) {
            callback.onConcluido(ADM_POSITION);
            return;
        }

        verificaSeEParticipante(comunidadeAlvo.getIdComunidade(), idUsuario, new VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    callback.onConcluido(PARTICIPANT_POSITION);
                } else {
                    callback.onError("Você não faz parte dessa comunidade.");
                }
            }

            @Override
            public void onError(String message) {
                callback.onError("Ocorreu um erro ao recuperar os dados da comunidade.");
            }
        });
    }

    public void sairDaComunidade(String idComunidade, String idAlvo, SairDaComunidadeCallback callback) {
        if (idAlvo == null || idComunidade == null
                || idAlvo.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaSeEParticipante(idComunidade, idAlvo, new VerificaParticipanteCallback() {
            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    recuperarListaAdms(idComunidade, new RecuperarListaAdmsCallback() {
                        HashMap<String, Object> dadosOperacao = new HashMap<>();
                        String caminhoFollowers = "/communityFollowers/" + idComunidade + "/" + idAlvo;
                        String caminhoFollowing = "/communityFollowing/" + idAlvo + "/" + idComunidade;
                        String caminhoComunidade = "/comunidades/" + idComunidade + "/";

                        @Override
                        public void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm) {
                            dadosOperacao.put(caminhoFollowers, null);
                            dadosOperacao.put(caminhoFollowing, null);
                            dadosOperacao.put(caminhoComunidade + "nrParticipantes", ServerValue.increment(-1));
                            if (usuarioAtualAdm) {
                                idsAdms.remove(idAlvo);
                                dadosOperacao.put(caminhoComunidade + "/admsComunidade/", idsAdms);
                                dadosOperacao.put(caminhoComunidade + "nrAdms", ServerValue.increment(-1));
                            }
                            salvarHashmapSairDaComunidade(dadosOperacao, callback);
                        }

                        @Override
                        public void onNaoExiste() {
                            dadosOperacao.put(caminhoFollowers, null);
                            dadosOperacao.put(caminhoFollowing, null);
                            dadosOperacao.put(caminhoComunidade + "nrParticipantes", ServerValue.increment(-1));
                            salvarHashmapSairDaComunidade(dadosOperacao, callback);
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

    private void recuperarListaAdms(String idComunidade, RecuperarListaAdmsCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarListaRef = firebaseRef.child("comunidades")
                .child(idComunidade)
                .child("admsComunidade");

        recuperarListaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    boolean adm = false;
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    ArrayList<String> listaIds = snapshot.getValue(t);
                    if (listaIds != null
                            && listaIds.size() > 0) {
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

    public void salvarHashmapSairDaComunidade(HashMap<String, Object> dadosOperacao, SairDaComunidadeCallback callback) {
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

    public void excluirComunidade(Context context, String idComunidade, ExcluirComunidadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        HashMap<String, Object> dadosOperacao = new HashMap<>();
        String caminhoFollowers = "/communityFollowers/" + idComunidade;
        String caminhoComunidade = "/comunidades/" + idComunidade;
        String caminhoComunidadesUsuario = "/usuarios/" + idUsuario + "/" + "idMinhasComunidades/";
        String postagensComunidade = "/postagensComunidade/" + idComunidade;
        String caminhoComunidadePublica = "/publicCommunities/" + idComunidade;
        String caminhoComunidadePrivada = "/privateCommunities/" + idComunidade;
        dadosOperacao.put(caminhoComunidadePublica, null);
        dadosOperacao.put(caminhoComunidadePrivada, null);
        dadosOperacao.put(caminhoFollowers, null);
        dadosOperacao.put(caminhoComunidade, null);
        dadosOperacao.put(postagensComunidade, null);
        UsuarioUtils.recuperarIdsComunidades(context, idUsuario, new UsuarioUtils.RecuperarIdsMinhasComunidadesCallback() {
            @Override
            public void onRecuperado(ArrayList<String> idsComunidades) {
                if (idsComunidades.contains(idUsuario)) {
                    idsComunidades.remove(idUsuario);
                    dadosOperacao.put(caminhoComunidadesUsuario, idsComunidades);
                }
                salvarHashmapExlusaoComunidade(dadosOperacao, callback);
            }

            @Override
            public void onNaoExiste() {
                salvarHashmapExlusaoComunidade(dadosOperacao, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }


    private void salvarHashmapExlusaoComunidade(HashMap<String, Object> dadosOperacao, ExcluirComunidadeCallback callback) {
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

    public void prepararListaPromocao(String tipoGerenciamento, Comunidade comunidadeAlvo, PrepararListaCallback callback) {
        cont = 0;
        List<Usuario> listaInicial = new ArrayList<>();
        if (tipoGerenciamento == null || tipoGerenciamento.isEmpty()
                || comunidadeAlvo == null) {
            callback.onError("Ocorreu um erro ao preparar a lista de usuários para a função desejada.");
            return;
        }
        if (tipoGerenciamento.equals(FUNCTION_NEW_FOUNDER)) {
            if (comunidadeAlvo.getNrParticipantes() > 0) {
                Query procurarUsuariosRef = firebaseRef.child("communityFollowers")
                        .child(comunidadeAlvo.getIdComunidade()).orderByChild("administrator")
                        .equalTo(false);
                procurarUsuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                cont++;
                                Comunidade dadosParticipante = snapshot1.getValue(Comunidade.class);
                                adicionarUsuarioALista(dadosParticipante.getIdParticipante(), listaInicial, cont >= snapshot.getChildrenCount(), callback);
                            }
                        } else {
                            callback.onSemDados("Não existem usuários no momento que atendem os requisitos para essa função desejada.");
                        }
                        procurarUsuariosRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError("Ocorreu um erro ao preparar a lista de usuários para a função desejada.");
                    }
                });
            } else {
                callback.onSemDados("Não existem usuários no momento que atendem os requisitos para essa função desejada.");
            }
        } else {

        }
    }

    private void adicionarUsuarioALista(String idAlvo, List<Usuario> listaAlvo, boolean terminado, PrepararListaCallback callback) {
        if (listaAlvo == null) {
            callback.onError("Ocorreu um erro ao preparar a lista de usuários para a função desejada.");
            return;
        }
        FirebaseRecuperarUsuario.recuperaUsuario(idAlvo, new FirebaseRecuperarUsuario.RecuperaUsuarioCallback() {
            @Override
            public void onUsuarioRecuperado(Usuario usuarioAtual, String nomeUsuarioAjustado, Boolean epilepsia) {
                if (listaAlvo.size() > 0) {
                    if (!listaAlvo.contains(usuarioAtual)) {
                        listaAlvo.add(usuarioAtual);
                    }
                } else {
                    listaAlvo.add(usuarioAtual);
                }

                if (terminado) {
                    callback.onConcluido(listaAlvo);
                }
            }

            @Override
            public void onError(String mensagem) {
                if (terminado) {
                    callback.onConcluido(listaAlvo);
                }
            }
        });
    }

    public void verificaConviteComunidade(String idComunidade, String idAlvo, VerificaConviteCallback callback) {
        if (idAlvo == null || idAlvo.isEmpty()
                || idComunidade == null || idComunidade.isEmpty()) {
            callback.onError("Ocorreu um erro ao verificar os convites.");
            return;
        }
        DatabaseReference verificaConviteRef = firebaseRef.child("convitesComunidade")
                .child(idComunidade).child(idAlvo);
        verificaConviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    callback.onExiste();
                } else {
                    callback.onNaoExiste();
                }
                verificaConviteRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }

    public void enviarConvite(String idComunidade, String idAlvo, EnviarConviteCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idUsuario.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        if (idAlvo == null || idAlvo.isEmpty()
                || idComunidade == null || idComunidade.isEmpty()) {
            callback.onError("");
            return;
        }
        TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoConvite = "/convitesComunidade/" + idComunidade + "/" + idAlvo + "/";
            String caminhoConviteAlvo = "/convitesComunidade/" + idAlvo + "/" + idComunidade + "/";
            String usuarioAlvoCaminho = "/usuarios/" + idAlvo + "/nrConvitesComunidade";

            @Override
            public void onRecuperado(long timestampNegativo) {
                dadosOperacao.put(caminhoConvite + "idRemetente", idUsuario);
                dadosOperacao.put(caminhoConvite + "idDestinatario", idAlvo);
                dadosOperacao.put(caminhoConvite + "idComunidade", idComunidade);
                dadosOperacao.put(caminhoConvite + "timestampinteracao", timestampNegativo);
                dadosOperacao.put(caminhoConviteAlvo + "idRemetente", idUsuario);
                dadosOperacao.put(caminhoConviteAlvo + "idDestinatario", idAlvo);
                dadosOperacao.put(caminhoConviteAlvo + "idComunidade", idComunidade);
                dadosOperacao.put(caminhoConviteAlvo + "timestampinteracao", timestampNegativo);
                dadosOperacao.put(usuarioAlvoCaminho, ServerValue.increment(1));
                firebaseRef.updateChildren(dadosOperacao, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {
                            callback.onEnviado();
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

    public void promoverParaAdm(String idComunidade, String idAlvo, PromoverAdmCallback callback) {

        //PROBLEMA COM A LISTA POR ESTAR DENTRO DE UM FOR, AS VEZES ELE FUNCIONA BEM
        //AS VEZES NÃO, RESUMINDO POR CAUSA DO FOR AS VEZES ELE NÃO TEM TEMPO
        //DE ALTERAR O ARRAYLIST DA FORMA QUE EU DESEJAVA.

        if (idAlvo == null || idComunidade == null
                || idAlvo.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaSeEParticipante(idComunidade, idAlvo, new VerificaParticipanteCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoFollowers = "/communityFollowers/" + idComunidade + "/" + idAlvo + "/";
            String caminhoComunidade = "/comunidades/" + idComunidade + "/";
            ArrayList<String> listaPronta = new ArrayList<>();

            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    dadosOperacao.put(caminhoFollowers + "administrator", true);
                    dadosOperacao.put(caminhoComunidade + "nrAdms", ServerValue.increment(1));
                    recuperarListaAdms(idComunidade, new RecuperarListaAdmsCallback() {
                        @Override
                        public void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm) {
                            listaPronta = idsAdms;
                            listaPronta.add(idAlvo);
                            ToastCustomizado.toastCustomizadoCurto("ADD: " + idAlvo, context);
                            dadosOperacao.put(caminhoComunidade + "admsComunidade/", listaPronta);
                            salvarHashmapPromocao(dadosOperacao, callback);
                        }

                        @Override
                        public void onNaoExiste() {
                            ToastCustomizado.toastCustomizadoCurto("NOVO ADD: " + idAlvo, context);
                            listaPronta.add(idAlvo);
                            dadosOperacao.put(caminhoComunidade + "admsComunidade/", listaPronta);
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

    public void despromoverAdm(String idComunidade, String idAlvo, DespromoverAdmCallback callback) {

        //PROBLEMA COM A LISTA POR ESTAR DENTRO DE UM FOR, AS VEZES ELE FUNCIONA BEM
        //AS VEZES NÃO, RESUMINDO POR CAUSA DO FOR AS VEZES ELE NÃO TEM TEMPO
        //DE ALTERAR O ARRAYLIST DA FORMA QUE EU DESEJAVA.

        if (idAlvo == null || idComunidade == null
                || idAlvo.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaSeEParticipante(idComunidade, idAlvo, new VerificaParticipanteCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoFollowers = "/communityFollowers/" + idComunidade + "/" + idAlvo + "/";
            String caminhoComunidade = "/comunidades/" + idComunidade + "/";

            @Override
            public void onParticipante(boolean status) {
                if (status) {
                    verificaAdm(idComunidade, idAlvo, new VerificaAdmCallback() {
                        @Override
                        public void onRecuperado(boolean adm) {
                            if (adm) {
                                //Usuário é realmente um dos adms.
                                dadosOperacao.put(caminhoFollowers + "administrator", false);
                                dadosOperacao.put(caminhoComunidade + "nrAdms", ServerValue.increment(-1));
                                recuperarListaAdms(idComunidade, new RecuperarListaAdmsCallback() {
                                    @Override
                                    public void onConcluido(ArrayList<String> idsAdms, boolean usuarioAtualAdm) {
                                        idsAdms.remove(idAlvo);
                                        ToastCustomizado.toastCustomizado("DELL: " + idAlvo, context);
                                        if (idsAdms.size() <= 0) {
                                            dadosOperacao.put(caminhoComunidade + "admsComunidade/", null);
                                        } else {
                                            dadosOperacao.put(caminhoComunidade + "admsComunidade/", idsAdms);
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

    private void verificaAdm(String idComunidade, String idAlvo, VerificaAdmCallback callback) {
        if (idAlvo == null || idComunidade == null
                || idAlvo.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        FirebaseRecuperarUsuario.recoverCommunity(idComunidade, new FirebaseRecuperarUsuario.RecoverCommunityCallback() {
            @Override
            public void onComunidadeRecuperada(Comunidade comunidadeAtual) {
                if (comunidadeAtual.getAdmsComunidade() != null
                        && comunidadeAtual.getAdmsComunidade().size() > 0
                        && comunidadeAtual.getAdmsComunidade().contains(idAlvo)) {
                    callback.onRecuperado(true);
                } else {
                    callback.onRecuperado(false);
                }
            }

            @Override
            public void onNaoExiste() {
                callback.onError("Essa comunidade não existe mais.");
            }

            @Override
            public void onError(String mensagem) {
                callback.onError(mensagem);
            }
        });
    }

    public void transferirFundador(String idComunidade, String idAlvo, TransferirFundadorCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idUsuario.isEmpty() || idAlvo == null || idComunidade == null
                || idAlvo.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        ajustarIdComunidades(idAlvo, idComunidade, new AjustarMinhasComunidadesCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoComunidade = "/comunidades/" + idComunidade + "/";
            String usuarioAlvoCaminho = "/usuarios/" + idAlvo + "/idMinhasComunidades/";
            String usuarioAtualCaminho = "/usuarios/" + idUsuario + "/idMinhasComunidades/";
            String caminhoFollowers = "/communityFollowers/" + idComunidade + "/" + idAlvo;

            @Override
            public void onConcluido(ArrayList<String> idsUserAlvo, ArrayList<String> idsUserAtual) {
                dadosOperacao.put(caminhoFollowers, null);
                dadosOperacao.put(caminhoComunidade + "idSuperAdmComunidade", idAlvo);
                dadosOperacao.put(caminhoComunidade + "nrParticipantes", ServerValue.increment(-1));
                dadosOperacao.put(usuarioAlvoCaminho, idsUserAlvo);
                dadosOperacao.put(usuarioAtualCaminho, idsUserAtual);
                verificaAdm(idComunidade, idAlvo, new VerificaAdmCallback() {
                    @Override
                    public void onRecuperado(boolean adm) {
                        if (adm) {
                            //Usuário escolhido é adm.
                            despromoverAdm(idComunidade, idAlvo, new DespromoverAdmCallback() {
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
            public void onLimiteMaxAtingido() {
                callback.onLimiteMaxAtingido();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void ajustarIdComunidades(String idAlvo, String idComunidade, AjustarMinhasComunidadesCallback callback) {
        //Verificar se usuário selecionado tem o limite máximo de comunidade ou não.
        UsuarioUtils.recuperarIdsComunidades(context, idAlvo, new UsuarioUtils.RecuperarIdsMinhasComunidadesCallback() {
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
                idsUserAlvo.add(idComunidade);
                removerIdMinhasComunidade(idComunidade, new AjustarIdsComunidadeCallback() {
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
                idsUserAlvo.add(idComunidade);
                removerIdMinhasComunidade(idComunidade, new AjustarIdsComunidadeCallback() {
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

    private void removerIdMinhasComunidade(String idComunidade, AjustarIdsComunidadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        UsuarioUtils.recuperarIdsComunidades(context, idUsuario, new UsuarioUtils.RecuperarIdsMinhasComunidadesCallback() {
            @Override
            public void onRecuperado(ArrayList<String> idsUserAtual) {
                if (idsUserAtual == null) {
                    callback.onError("");
                    return;
                }
                if (idsUserAtual.contains(idComunidade)) {
                    idsUserAtual.remove(idComunidade);
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

    public void bloquearComunidade(String idComunidade, BloquearComunidadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        HashMap<String, Object> dadosOperacao = new HashMap<>();
        String caminhoBlock = "/blockCommunity/" + idUsuario + "/" + idComunidade + "/";
        dadosOperacao.put(caminhoBlock + "idComunidade", idComunidade);
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

    public void desbloquearComunidade(String idComunidade, DesbloquearComunidadeCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        HashMap<String, Object> dadosOperacao = new HashMap<>();
        String caminhoBlock = "/blockCommunity/" + idUsuario + "/" + idComunidade;
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

    public void enviarDenunciaComunidade(String idComunidade, EnviarDenunciaCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        verificaSeExisteDenuncia(idComunidade, new VerificaDenunciaCallback() {
            @Override
            public void onDenuncia(boolean statusDenuncia) {
                if (statusDenuncia) {
                    callback.onJaExisteDenuncia();
                    return;
                }
                TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                    String caminhoDenuncia = "/communityReports/" + idComunidade + "/" + idUsuario + "/";
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

    public void verificaSeExisteDenuncia(String idComunidade, VerificaDenunciaCallback callback) {

        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }

        DatabaseReference verificaDenunciaRef = firebaseRef.child("communityReports")
                .child(idComunidade).child(idUsuario);
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

    public void verificaBlock(String idAlvo, String idComunidade, VerificaBlockCallback callback) {
        if (idAlvo == null || idComunidade == null
                || idAlvo.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference verificaBlockRef = firebaseRef.child("blockCommunity")
                .child(idAlvo).child(idComunidade);
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

    public void recusarConvite(String idComunidade, RecusarConviteCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaConviteComunidade(idComunidade, idUsuario, new VerificaConviteCallback() {
            HashMap<String, Object> dadosOperacao = new HashMap<>();
            String caminhoConvite = "/convitesComunidade/" + idComunidade + "/" + idUsuario;
            String caminhoConviteAlvo = "/convitesComunidade/" + idUsuario + "/" + idComunidade;
            String usuarioAlvoCaminho = "/usuarios/" + idUsuario + "/nrConvitesComunidade";

            @Override
            public void onExiste() {
                dadosOperacao.put(caminhoConvite, null);
                dadosOperacao.put(caminhoConviteAlvo, null);
                dadosOperacao.put(usuarioAlvoCaminho, ServerValue.increment(-1));
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
                callback.onNaoExiste();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void aceitarConvite(String idComunidade, AceitarConviteCallback callback) {
        String idUsuario;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
        if (idUsuario == null || idComunidade == null
                || idUsuario.isEmpty() || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        verificaBlock(idUsuario, idComunidade, new VerificaBlockCallback() {
            @Override
            public void onBlock(boolean status) {
                if (status) {
                    callback.onBlocked();
                    return;
                }
                verificaConviteComunidade(idComunidade, idUsuario, new VerificaConviteCallback() {
                    HashMap<String, Object> dadosOperacao = new HashMap<>();
                    String caminhoConvite = "/convitesComunidade/" + idComunidade + "/" + idUsuario;
                    String caminhoConviteAlvo = "/convitesComunidade/" + idUsuario + "/" + idComunidade;
                    String usuarioAlvoCaminho = "/usuarios/" + idUsuario + "/nrConvitesComunidade";
                    String caminhoFollowers = "/communityFollowers/" + idComunidade + "/" + idUsuario + "/";
                    String caminhoFollowing = "/communityFollowing/" + idUsuario + "/" + idComunidade + "/";
                    String caminhoComunidade = "/comunidades/" + idComunidade + "/";
                    String caminhoConvites = "/convitesComunidade/" + idUsuario + "/" + idComunidade;
                    @Override
                    public void onExiste() {
                        dadosOperacao.put(caminhoConvite, null);
                        dadosOperacao.put(caminhoConviteAlvo, null);
                        dadosOperacao.put(usuarioAlvoCaminho, ServerValue.increment(-1));
                        TimestampUtils.RecuperarTimestamp(context, new TimestampUtils.RecuperarTimestampCallback() {
                            @Override
                            public void onRecuperado(long timestampNegativo) {
                                dadosOperacao.put(caminhoComunidade + "nrParticipantes", ServerValue.increment(1));
                                dadosOperacao.put(caminhoFollowers + "timestampinteracao", timestampNegativo);
                                dadosOperacao.put(caminhoFollowers + "idParticipante", idUsuario);
                                dadosOperacao.put(caminhoFollowers + "administrator", false);
                                dadosOperacao.put(caminhoFollowing + "idComunidade", idComunidade);
                                dadosOperacao.put(caminhoFollowing + "timestampinteracao", timestampNegativo);
                                dadosOperacao.put(caminhoConvites, null);

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
                    public void onNaoExiste() {
                        callback.onNaoExiste();
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

    public void verificaSeComunidadeExiste(String idComunidade, VerificaSeComunidadeExisteCallback callback) {
        if (idComunidade == null || idComunidade.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference verificaComunidadeRef = firebaseRef.child("comunidades")
                .child(idComunidade).child("idComunidade");
        verificaComunidadeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onStatus(snapshot.getValue() != null);
                verificaComunidadeRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }
}
