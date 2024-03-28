package com.example.ogima.helper;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.ogima.R;
import com.example.ogima.model.Chat;
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
import java.util.HashMap;
import java.util.List;

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

    public interface RecoverCommunityCallback {
        void onComunidadeRecuperada(Comunidade comunidadeAtual);

        void onNaoExiste();

        void onError(String mensagem);
    }

    public interface RecoverGroupCallback {
        void onGrupoRecuperado(Grupo grupoAtual);

        void onNaoExiste();

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

    public interface RecuperarInteressesCallback {
        void onRecuperado(HashMap<String, Double> listaInteresses);

        void onSemInteresses();

        void onError(String message);
    }

    public interface PostagensVisualizadasCallback {
        void onPostagens(ArrayList<String> postagensVisualizadas);

        void onSemPostagens();

        void onError(String message);
    }

    public interface RecuperarDetalhesChatCallback {
        void onDetalheChatRecuperado(Chat chatAtual);

        void onSemDados();

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

                                nomeUserRemovido = usuarioRecuperado.getNomeUsuario();

                                nomeUserAtual = usuarioLogado.getNomeUsuario();

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

                    nomeAjustado = usuarioRecuperado.getNomeUsuario();

                    epilepsia = usuarioRecuperado.isStatusEpilepsia();

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

    public static void recoverCommunity(String idComunidade, RecoverCommunityCallback callback) {
        DatabaseReference comunidadeRecuperadaRef = FirebaseDatabase.getInstance().getReference("comunidades").child(idComunidade);
        comunidadeRecuperadaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Comunidade comunidadeRecuperada = snapshot.getValue(Comunidade.class);
                    callback.onComunidadeRecuperada(comunidadeRecuperada);
                } else {
                    callback.onNaoExiste();
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

                    if (usuarioRecuperado.getNomeUsuario() != null
                            && !usuarioRecuperado.getNomeUsuario().isEmpty()) {
                        nomeAjustado = FormatarNomePesquisaUtils.formatarNomeParaPesquisa(usuarioRecuperado.getNomeUsuario());
                    }

                    epilepsia = usuarioRecuperado.isStatusEpilepsia();

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

    public static void recuperarInteresses(String idUsuario, RecuperarInteressesCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference recuperarInteressesRef = firebaseRef.child("usuarios")
                .child(idUsuario).child("listaInteresses");

        recuperarInteressesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    GenericTypeIndicator<HashMap<String, Double>> typeIndicator = new GenericTypeIndicator<HashMap<String, Double>>() {
                    };
                    HashMap<String, Double> listaInteresses = snapshot.getValue(typeIndicator);
                    if (listaInteresses != null &&
                            listaInteresses.size() > 0) {
                        callback.onRecuperado(listaInteresses);
                    } else {
                        callback.onSemInteresses();
                    }
                } else {
                    callback.onSemInteresses();
                }
                recuperarInteressesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarPostagensVisualizadas(String idUsuario, PostagensVisualizadasCallback callback) {
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
                        callback.onPostagens(listaPostagensVisualizadas);
                    } else {
                        callback.onSemPostagens();
                    }
                } else {
                    callback.onSemPostagens();
                }
                recuperarPostagensRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public static void recuperarDetalhesChat(Context context, String idAlvo, RecuperarDetalhesChatCallback callback) {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
        String idUsuario = "";
        idUsuario = UsuarioUtils.recuperarIdUserAtual();

        if (idUsuario == null || idAlvo == null
                || idUsuario.isEmpty() || idAlvo.isEmpty()) {
            callback.onError(context.getString(R.string.error_recovering_data));
            return;
        }
        DatabaseReference recuperarDetalhesRef = firebaseRef.child("detalhesChat")
                .child(idUsuario).child(idAlvo);
        recuperarDetalhesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null && chat.getIdUsuario() != null
                            && !chat.getIdUsuario().isEmpty()) {
                        callback.onDetalheChatRecuperado(chat);
                    } else {
                        callback.onSemDados();
                    }
                } else {
                    callback.onSemDados();
                }
                recuperarDetalhesRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(String.valueOf(error.getCode()));
            }
        });
    }
    public static void recoverGroup(String idGrupo, RecoverGroupCallback callback) {
        DatabaseReference grupoRecuperadoRef = FirebaseDatabase.getInstance().getReference("grupos").child(idGrupo);
        grupoRecuperadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Grupo grupoRecuperado = snapshot.getValue(Grupo.class);
                    callback.onGrupoRecuperado(grupoRecuperado);
                } else {
                    callback.onNaoExiste();
                }
                grupoRecuperadoRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
