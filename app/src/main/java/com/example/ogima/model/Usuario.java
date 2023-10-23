package com.example.ogima.model;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.FcmUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Usuario implements Serializable, Comparator<Usuario> {

    public static final int CUSTO_VIEWER = 10;

    private String idUsuario;
    private String nomeUsuario;
    private String nomeUsuarioPesquisa;
    private String apelidoUsuario;
    private String apelidoUsuarioPesquisa;
    private String emailUsuario;
    private String senhaUsuario;
    private String fotoPerfil;
    private String fundoPerfil;
    private String generoUsuario;
    private String opcaoSexualUsuario;
    private String numero;
    private int idade;
    private int seguidoresUsuario;
    private int seguindoUsuario;
    private int amigosUsuario;
    private int pedidosAmizade;
    private int viewsPerfil;
    private String dataNascimento;
    private List<String> fotosUsuario;
    private ArrayList<String> interesses;

    private String caminhoFotoPerfil;
    private String caminhoFotoPerfilFundo;

    private String minhaFoto; //Foto de perfil
    private String meuFundo; // Foto de fundo

    private boolean statusEmail;
    private String exibirApelido;
    private String epilepsia;

    private Date dataMensagemCompleta;
    private String contatoFavorito;

    private String idRemetente, idDestinatario;

    private ArrayList<String> idMeusGrupos;
    private ArrayList<String> idGruposBloqueados;
    private Boolean gruposSomentePorAmigos;


    private ArrayList<String> idMinhasComunidades;
    private ArrayList<String> idComunidadesBloqueadas;
    private Boolean comunidadesSomentePorAmigos;
    private ArrayList<String> idPostagensCurtidas;

    private Boolean dailyShortAtivo;
    private String urlLastDaily;
    private ArrayList<String> listaIdAmigos;
    private ArrayList<String> listaIdSeguindo;
    private String tipoMidia;
    private String dataLastDaily;

    private ArrayList<String> idLikePosts;
    private String privacidadePostagens;

    private long timeStampView;
    private long timestampinteracao;
    private String dataView;
    private boolean viewLiberada;

    private int nrAdsVisualizadas;
    private int ogimaCoins;
    private long timeStampResetarLimiteAds;

    @Exclude
    private boolean orderByTimeStampView, orderByName;

    private boolean online;

    private String token;
    private ArrayList<String> topicosNotificacoes;
    private int novasMensagens;
    private int totalMensagensPerdidas;
    private boolean statusMensagemNaoLida;
    private boolean exibirBadgeNewMensagens;
    private boolean nasConversas;

    private HashMap<String, Double> listaInteresses;
    private ArrayList<String> listaPostagensVisualizadas;

    private String nomeParc;
    private String exibirPerfilPara;
    private ArrayList<String> fotosParc;
    private String orientacaoSexual;
    private String nomeInstituicao;
    private ArrayList<String> listaInteressesParc;
    private ArrayList<String> idsEsconderParc;
    private int posicao;
    private boolean emparelhado;
    private String generoDesejado;
    private int idadeMaxDesejada;
    private int contadorAddRandom;
    private boolean statusEpilepsia;
    private String idQRCode;
    private boolean indisponivel;

    public Usuario() {
    }

    public Usuario(String nomeParc, String exibirPerfilPara, ArrayList<String> fotosParc,
                   String orientacaoSexual, String nomeInstituicao,
                   ArrayList<String> listaInteressesParc){
        this.nomeParc = nomeParc;
        this.exibirPerfilPara = exibirPerfilPara;
        this.fotosParc = fotosParc;
        this.orientacaoSexual = orientacaoSexual;
        this.nomeInstituicao = nomeInstituicao;
        this.listaInteressesParc = listaInteressesParc;
    }

    public Usuario(boolean orderByTimeStampView, boolean orderByName) {
        this.orderByTimeStampView = orderByTimeStampView;
        this.orderByName = orderByName;
    }

    //Adicionado para ordenação da listaChat em ChatFragment
    public Usuario(Date dataMensagemCompleta) {
        this.dataMensagemCompleta = dataMensagemCompleta;
    }

    public void salvar(boolean salvarToken){

        DatabaseReference firebaseref = ConfiguracaoFirebase.getFirebaseDataBase();
        DatabaseReference usuario = firebaseref.child("usuarios").child(getIdUsuario());

        if (salvarToken) {
            FcmUtils.salvarTokenAtualNoUserAtual(new FcmUtils.SalvarTokenCallback() {
                @Override
                public void onSalvo(String token) {
                    setToken(token);
                    usuario.setValue(this);
                }

                @Override
                public void onError(String message) {
                    //Salvar algum dado no SQLite para recuperar e salvar o token
                    //posteriormente.
                }
            });
        }else{
            usuario.setValue(this);
        }
    }

    public long getTimestampinteracao() {
        return timestampinteracao;
    }

    public void setTimestampinteracao(long timestampinteracao) {
        this.timestampinteracao = timestampinteracao;
    }

    public boolean isIndisponivel() {
        return indisponivel;
    }

    public void setIndisponivel(boolean indisponivel) {
        this.indisponivel = indisponivel;
    }

    public String getIdQRCode() {
        return idQRCode;
    }

    public void setIdQRCode(String idQRCode) {
        this.idQRCode = idQRCode;
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
    }

    public int getContadorAddRandom() {
        return contadorAddRandom;
    }

    public void setContadorAddRandom(int contadorAddRandom) {
        this.contadorAddRandom = contadorAddRandom;
    }

    public int getIdadeMaxDesejada() {
        return idadeMaxDesejada;
    }

    public void setIdadeMaxDesejada(int idadeMaxDesejada) {
        this.idadeMaxDesejada = idadeMaxDesejada;
    }

    public String getGeneroDesejado() {
        return generoDesejado;
    }

    public void setGeneroDesejado(String generoDesejado) {
        this.generoDesejado = generoDesejado;
    }

    public boolean isEmparelhado() {
        return emparelhado;
    }

    public void setEmparelhado(boolean emparelhado) {
        this.emparelhado = emparelhado;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public ArrayList<String> getIdsEsconderParc() {
        return idsEsconderParc;
    }

    public void setIdsEsconderParc(ArrayList<String> idsEsconderParc) {
        this.idsEsconderParc = idsEsconderParc;
    }

    public String getNomeParc() {
        return nomeParc;
    }

    public void setNomeParc(String nomeParc) {
        this.nomeParc = nomeParc;
    }

    public String getExibirPerfilPara() {
        return exibirPerfilPara;
    }

    public void setExibirPerfilPara(String exibirPerfilPara) {
        this.exibirPerfilPara = exibirPerfilPara;
    }

    public ArrayList<String> getFotosParc() {
        return fotosParc;
    }

    public void setFotosParc(ArrayList<String> fotosParc) {
        this.fotosParc = fotosParc;
    }

    public String getOrientacaoSexual() {
        return orientacaoSexual;
    }

    public void setOrientacaoSexual(String orientacaoSexual) {
        this.orientacaoSexual = orientacaoSexual;
    }

    public String getNomeInstituicao() {
        return nomeInstituicao;
    }

    public void setNomeInstituicao(String nomeInstituicao) {
        this.nomeInstituicao = nomeInstituicao;
    }

    public ArrayList<String> getListaInteressesParc() {
        return listaInteressesParc;
    }

    public void setListaInteressesParc(ArrayList<String> listaInteressesParc) {
        this.listaInteressesParc = listaInteressesParc;
    }

    public ArrayList<String> getListaPostagensVisualizadas() {
        return listaPostagensVisualizadas;
    }

    public void setListaPostagensVisualizadas(ArrayList<String> listaPostagensVisualizadas) {
        this.listaPostagensVisualizadas = listaPostagensVisualizadas;
    }

    public HashMap<String, Double> getListaInteresses() {
        return listaInteresses;
    }

    public void setListaInteresses(HashMap<String, Double> listaInteresses) {
        this.listaInteresses = listaInteresses;
    }

    public boolean isNasConversas() {
        return nasConversas;
    }

    public void setNasConversas(boolean nasConversas) {
        this.nasConversas = nasConversas;
    }

    public boolean isExibirBadgeNewMensagens() {
        return exibirBadgeNewMensagens;
    }

    public void setExibirBadgeNewMensagens(boolean exibirBadgeNewMensagens) {
        this.exibirBadgeNewMensagens = exibirBadgeNewMensagens;
    }

    public int getTotalMensagensPerdidas() {
        return totalMensagensPerdidas;
    }

    public void setTotalMensagensPerdidas(int totalMensagensPerdidas) {
        this.totalMensagensPerdidas = totalMensagensPerdidas;
    }

    public boolean isStatusMensagemNaoLida() {
        return statusMensagemNaoLida;
    }

    public void setStatusMensagemNaoLida(boolean statusMensagemNaoLida) {
        this.statusMensagemNaoLida = statusMensagemNaoLida;
    }

    public int getNovasMensagens() {
        return novasMensagens;
    }

    public void setNovasMensagens(int novasMensagens) {
        this.novasMensagens = novasMensagens;
    }

    public ArrayList<String> getTopicosNotificacoes() {
        return topicosNotificacoes;
    }

    public void setTopicosNotificacoes(ArrayList<String> topicosNotificacoes) {
        this.topicosNotificacoes = topicosNotificacoes;
    }

    //Aula do whats 230, o id também tá como criptografado
    //public void atualizar(){

    //String identificadorUsuario = UsuarioFirebase.getIdUsuarioCriptografado();
    // DatabaseReference database = ConfiguracaoFirebase.getFirebaseDataBase();

    //  DatabaseReference usuariosRef = database.child("usuarios")
    //    .child(identificadorUsuario);

    //Map<String, Object> valoresUsuario = converterparaMap();

    //usuariosRef.updateChildren(valoresUsuario);
    // }
/*
    @Exclude
    public Map<String, Object> converterparaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("emailUsuario", getEmailUsuario());
        usuarioMap.put("apelidoUsuario", getApelidoUsuario());
        usuarioMap.put("nomeUsuario", getNomeUsuario());
        usuarioMap.put("generoUsuario", getGeneroUsuario());
        usuarioMap.put("dataNascimento", getDataNascimento());
        usuarioMap.put("idade", getIdade());
        usuarioMap.put("interesses", getInteresses());
        usuarioMap.put("fotoPerfil", getCaminhoFotoPerfil());
        usuarioMap.put("fotoPerfilFundo", getCaminhoFotoPerfilFundo());

        return usuarioMap;
    }

 */

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getTimeStampResetarLimiteAds() {
        return timeStampResetarLimiteAds;
    }

    public void setTimeStampResetarLimiteAds(long timeStampResetarLimiteAds) {
        this.timeStampResetarLimiteAds = timeStampResetarLimiteAds;
    }

    public int getOgimaCoins() {
        return ogimaCoins;
    }

    public void setOgimaCoins(int ogimaCoins) {
        this.ogimaCoins = ogimaCoins;
    }

    public int getNrAdsVisualizadas() {
        return nrAdsVisualizadas;
    }

    public void setNrAdsVisualizadas(int nrAdsVisualizadas) {
        this.nrAdsVisualizadas = nrAdsVisualizadas;
    }

    public boolean isViewLiberada() {
        return viewLiberada;
    }

    public void setViewLiberada(boolean viewLiberada) {
        this.viewLiberada = viewLiberada;
    }

    public long getTimeStampView() {
        return timeStampView;
    }

    public void setTimeStampView(long timeStampView) {
        this.timeStampView = timeStampView;
    }

    public String getDataView() {
        return dataView;
    }

    public void setDataView(String dataView) {
        this.dataView = dataView;
    }

    public String getPrivacidadePostagens() {
        return privacidadePostagens;
    }

    public void setPrivacidadePostagens(String privacidadePostagens) {
        this.privacidadePostagens = privacidadePostagens;
    }

    public ArrayList<String> getIdLikePosts() {
        return idLikePosts;
    }

    public void setIdLikePosts(ArrayList<String> idLikePosts) {
        this.idLikePosts = idLikePosts;
    }

    public String getDataLastDaily() {
        return dataLastDaily;
    }

    public void setDataLastDaily(String dataLastDaily) {
        this.dataLastDaily = dataLastDaily;
    }

    public String getTipoMidia() {
        return tipoMidia;
    }

    public void setTipoMidia(String tipoMidia) {
        this.tipoMidia = tipoMidia;
    }

    public ArrayList<String> getListaIdSeguindo() {
        return listaIdSeguindo;
    }

    public void setListaIdSeguindo(ArrayList<String> listaIdSeguindo) {
        this.listaIdSeguindo = listaIdSeguindo;
    }

    public ArrayList<String> getListaIdAmigos() {
        return listaIdAmigos;
    }

    public void setListaIdAmigos(ArrayList<String> listaIdAmigos) {
        this.listaIdAmigos = listaIdAmigos;
    }

    public String getUrlLastDaily() {
        return urlLastDaily;
    }

    public void setUrlLastDaily(String urlLastDaily) {
        this.urlLastDaily = urlLastDaily;
    }

    public Boolean getDailyShortAtivo() {
        return dailyShortAtivo;
    }

    public void setDailyShortAtivo(Boolean dailyShortAtivo) {
        this.dailyShortAtivo = dailyShortAtivo;
    }

    public ArrayList<String> getIdPostagensCurtidas() {
        return idPostagensCurtidas;
    }

    public void setIdPostagensCurtidas(ArrayList<String> idPostagensCurtidas) {
        this.idPostagensCurtidas = idPostagensCurtidas;
    }

    public ArrayList<String> getIdMinhasComunidades() {
        return idMinhasComunidades;
    }

    public void setIdMinhasComunidades(ArrayList<String> idMinhasComunidades) {
        this.idMinhasComunidades = idMinhasComunidades;
    }

    public ArrayList<String> getIdComunidadesBloqueadas() {
        return idComunidadesBloqueadas;
    }

    public void setIdComunidadesBloqueadas(ArrayList<String> idComunidadesBloqueadas) {
        this.idComunidadesBloqueadas = idComunidadesBloqueadas;
    }

    public Boolean getComunidadesSomentePorAmigos() {
        return comunidadesSomentePorAmigos;
    }

    public void setComunidadesSomentePorAmigos(Boolean comunidadesSomentePorAmigos) {
        this.comunidadesSomentePorAmigos = comunidadesSomentePorAmigos;
    }

    public ArrayList<String> getIdGruposBloqueados() {
        return idGruposBloqueados;
    }

    public void setIdGruposBloqueados(ArrayList<String> idGruposBloqueados) {
        this.idGruposBloqueados = idGruposBloqueados;
    }

    public Boolean getGruposSomentePorAmigos() {
        return gruposSomentePorAmigos;
    }

    public void setGruposSomentePorAmigos(Boolean gruposSomentePorAmigos) {
        this.gruposSomentePorAmigos = gruposSomentePorAmigos;
    }

    public ArrayList<String> getIdMeusGrupos() {
        return idMeusGrupos;
    }

    public void setIdMeusGrupos(ArrayList<String> idMeusGrupos) {
        this.idMeusGrupos = idMeusGrupos;
    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getContatoFavorito() {
        return contatoFavorito;
    }

    public void setContatoFavorito(String contatoFavorito) {
        this.contatoFavorito = contatoFavorito;
    }

    //Adicionado para ordenação da listaChat em ChatFragment
    public Date getDataMensagemCompleta() {
        return dataMensagemCompleta;
    }

    public void setDataMensagemCompleta(Date dataMensagemCompleta) {
        this.dataMensagemCompleta = dataMensagemCompleta;
    }

    public int getViewsPerfil() {
        return viewsPerfil;
    }

    public void setViewsPerfil(int viewsPerfil) {
        this.viewsPerfil = viewsPerfil;
    }

    public int getPedidosAmizade() {
        return pedidosAmizade;
    }

    public void setPedidosAmizade(int pedidosAmizade) {
        this.pedidosAmizade = pedidosAmizade;
    }

    public int getSeguidoresUsuario() {
        return seguidoresUsuario;
    }

    public void setSeguidoresUsuario(int seguidoresUsuario) {
        this.seguidoresUsuario = seguidoresUsuario;
    }

    public int getSeguindoUsuario() {
        return seguindoUsuario;
    }

    public void setSeguindoUsuario(int seguindoUsuario) {
        this.seguindoUsuario = seguindoUsuario;
    }

    public int getAmigosUsuario() {
        return amigosUsuario;
    }

    public void setAmigosUsuario(int amigosUsuario) {
        this.amigosUsuario = amigosUsuario;
    }

    public String getNomeUsuarioPesquisa() {
        return nomeUsuarioPesquisa;
    }

    public void setNomeUsuarioPesquisa(String nomeUsuarioPesquisa) {
        this.nomeUsuarioPesquisa = nomeUsuarioPesquisa;
    }

    public String getApelidoUsuarioPesquisa() {
        return apelidoUsuarioPesquisa;
    }

    public void setApelidoUsuarioPesquisa(String apelidoUsuarioPesquisa) {
        this.apelidoUsuarioPesquisa = apelidoUsuarioPesquisa;
    }

    public String getEpilepsia() {
        return epilepsia;
    }

    public void setEpilepsia(String epilepsia) {
        this.epilepsia = epilepsia;
    }

    public String getExibirApelido() {
        return exibirApelido;
    }

    public void setExibirApelido(String exibirApelido) {
        this.exibirApelido = exibirApelido;
    }

    public boolean isStatusEmail() {
        return statusEmail;
    }

    public void setStatusEmail(boolean statusEmail) {
        this.statusEmail = statusEmail;
    }

    public String getMeuFundo() {
        return meuFundo;
    }

    public void setMeuFundo(String meuFundo) {
        this.meuFundo = meuFundo;
    }

    public String getMinhaFoto() {
        return minhaFoto;
    }

    public void setMinhaFoto(String minhaFoto) {
        this.minhaFoto = minhaFoto;
    }

    public String getCaminhoFotoPerfilFundo() {
        return caminhoFotoPerfilFundo;
    }

    public void setCaminhoFotoPerfilFundo(String caminhoFotoPerfilFundo) {
        this.caminhoFotoPerfilFundo = caminhoFotoPerfilFundo;
    }

    public String getCaminhoFotoPerfil() {
        return caminhoFotoPerfil;
    }

    public void setCaminhoFotoPerfil(String caminhoFotoPerfil) {
        this.caminhoFotoPerfil = caminhoFotoPerfil;
    }

    public ArrayList<String> getInteresses() {
        return interesses;
    }

    public void setInteresses(ArrayList<String> interesses) {
        this.interesses = interesses;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getApelidoUsuario() {
        return apelidoUsuario;
    }

    public void setApelidoUsuario(String apelidoUsuario) {
        this.apelidoUsuario = apelidoUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    //Tirar o exclude somente depois de cripotagrafar a senha do usuario
    @Exclude
    public String getSenhaUsuario() {
        return senhaUsuario;
    }

    public void setSenhaUsuario(String senhaUsuario) {
        this.senhaUsuario = senhaUsuario;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getFundoPerfil() {
        return fundoPerfil;
    }

    public void setFundoPerfil(String fundoPerfil) {
        this.fundoPerfil = fundoPerfil;
    }

    public String getGeneroUsuario() {
        return generoUsuario;
    }

    public void setGeneroUsuario(String generoUsuario) {
        this.generoUsuario = generoUsuario;
    }

    public String getOpcaoSexualUsuario() {
        return opcaoSexualUsuario;
    }

    public void setOpcaoSexualUsuario(String opcaoSexualUsuario) {
        this.opcaoSexualUsuario = opcaoSexualUsuario;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public List<String> getFotosUsuario() {
        return fotosUsuario;
    }

    public void setFotosUsuario(List<String> fotosUsuario) {
        this.fotosUsuario = fotosUsuario;
    }


    //Servem para que compare pelo id para não ter duplicações no hashset.
    @Override
    public int hashCode() {
        return Objects.hash(getIdUsuario());
    }

    @Override
    public int compare(Usuario u1, Usuario u2) {
        //Comparator adicionado para a ordenação dos dados dos viewers,
        //se ele não causar problemas em paginações com listeners ativos
        //será uma boa função quando não é possível ordenar por query.
        //levar em conta para ver se não atrapalha o CRUD.
        if (orderByTimeStampView) {
            return Long.compare(u1.getTimeStampView(), u2.getTimeStampView());
        } else if(orderByName) {
            // Comparação com base em outra propriedade
            // Retorne o valor desejado de acordo com o critério de comparação
            String nome1, nome2;

            nome1 = u1.getNomeUsuario();
            nome2 = u2.getNomeUsuario();

            return nome1.compareToIgnoreCase(nome2);
        }else{
           return u1.getIdUsuario().compareToIgnoreCase(u2.getIdUsuario());
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;
        if (!(obj instanceof Usuario)) return false;
        Usuario usuario = (Usuario) obj;
        return Objects.equals(getIdUsuario(), usuario.getIdUsuario());

        /*
        if (obj == null) return false;
        if (!(obj instanceof Usuario)) return false;
        if (obj == this) return true;
        return this.idUsuario.equals(((Usuario) obj).idUsuario);
         */
    }
}

