package com.example.ogima.model;

import androidx.annotation.Nullable;

import com.example.ogima.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class Postagem implements Serializable {

    public static final int POST_TYPE_PHOTO = 0;
    public static final int POST_TYPE_VIDEO = 1;
    public static final int POST_TYPE_GIF = 2;
    public static final int POST_TYPE_TEXT = 3;

    private int totalCurtidasPostagem;
    private int totalComentarios;
    private int totalViewsFotoPostagem;
    private String comentarioPostado;
    private String ocultarComentario;
    private String dataComentario;
    private String idComentario;
    private String dataCurtidaPostagem;
    private String publicoPostagem;
    private int totalDenunciasPostagem;
    private int totalDenunciasComentario;
    private int totalCurtidasComentario;

    //Estava na classe Usuario
    private String idPostagem;
    private String idUsuarioInterativo;
    private String idDonoPostagem;
    private String idDonoComentario;
    private String idDenunciado;
    private String idDenunciador;
    private String dataDenunciaPostagem;
    private String dataDenunciaComentario;
    private String descricaoDenunciaPostagem;
    private String descricaoDenunciaComentario;
    private String tituloPostagem;
    private String descricaoPostagem;
    private String dataPostagem;
    private Date dataPostagemNova;
    private ArrayList<String> listaUrlPostagens;
    //private int contadorFotos;
    private String sinalizarRefresh;
    private String tipoPostagem;

    //Postagem
    private int totalPostagens;
    private String urlPostagem;

    private long timeStampNegativo;
    private String idComunidade;
    private Boolean edicaoEmAndamento;
    private boolean postType;

    public Postagem() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();

        DatabaseReference postagemComunidadeRef = firebaseRef.child("postagensComunidade");
        String idRandomicoGrupo = postagemComunidadeRef.push().getKey();
        setIdPostagem(idRandomicoGrupo);


    }

    public Postagem(String idPostagem, String idDonoPostagem, String dataPostagem, String tipoPostagem, String urlPostagem, long timeStampNegativo, String idComunidade) {
        this.idPostagem = idPostagem;
        this.idDonoPostagem = idDonoPostagem;
        this.dataPostagem = dataPostagem;
        this.tipoPostagem = tipoPostagem;
        this.urlPostagem = urlPostagem;
        this.timeStampNegativo = timeStampNegativo;
        this.idComunidade = idComunidade;
    }

    public boolean isPostType() {
        return postType;
    }

    public void setPostType(boolean postType) {
        this.postType = postType;
    }

    public Boolean getEdicaoEmAndamento() {
        return edicaoEmAndamento;
    }

    public void setEdicaoEmAndamento(Boolean edicaoEmAndamento) {
        this.edicaoEmAndamento = edicaoEmAndamento;
    }

    public String getIdComunidade() {
        return idComunidade;
    }

    public void setIdComunidade(String idComunidade) {
        this.idComunidade = idComunidade;
    }

    public long getTimeStampNegativo() {
        return timeStampNegativo;
    }

    public void setTimeStampNegativo(long timeStampNegativo) {
        this.timeStampNegativo = timeStampNegativo;
    }

    public String getTipoPostagem() {
        return tipoPostagem;
    }

    public void setTipoPostagem(String tipoPostagem) {
        this.tipoPostagem = tipoPostagem;
    }

    public String getUrlPostagem() {
        return urlPostagem;
    }

    public void setUrlPostagem(String urlPostagem) {
        this.urlPostagem = urlPostagem;
    }

    public ArrayList<String> getListaUrlPostagens() {
        return listaUrlPostagens;
    }

    public void setListaUrlPostagens(ArrayList<String> listaUrlPostagens) {
        this.listaUrlPostagens = listaUrlPostagens;
    }

    public int getTotalPostagens() {
        return totalPostagens;
    }

    public void setTotalPostagens(int totalPostagens) {
        this.totalPostagens = totalPostagens;
    }

    public Date getDataPostagemNova() {
        return dataPostagemNova;
    }

    public void setDataPostagemNova(Date dataPostagemNova) {
        this.dataPostagemNova = dataPostagemNova;
    }

    public int getTotalViewsFotoPostagem() {
        return totalViewsFotoPostagem;
    }

    public void setTotalViewsFotoPostagem(int totalViewsFotoPostagem) {
        this.totalViewsFotoPostagem = totalViewsFotoPostagem;
    }

    public String getPublicoPostagem() {
        return publicoPostagem;
    }

    public void setPublicoPostagem(String publicoPostagem) {
        this.publicoPostagem = publicoPostagem;
    }

    public String getDescricaoDenunciaComentario() {
        return descricaoDenunciaComentario;
    }

    public void setDescricaoDenunciaComentario(String descricaoDenunciaComentario) {
        this.descricaoDenunciaComentario = descricaoDenunciaComentario;
    }

    public String getDataDenunciaComentario() {
        return dataDenunciaComentario;
    }

    public void setDataDenunciaComentario(String dataDenunciaComentario) {
        this.dataDenunciaComentario = dataDenunciaComentario;
    }

    public int getTotalDenunciasComentario() {
        return totalDenunciasComentario;
    }

    public void setTotalDenunciasComentario(int totalDenunciasComentario) {
        this.totalDenunciasComentario = totalDenunciasComentario;
    }

    public String getIdDonoComentario() {
        return idDonoComentario;
    }

    public void setIdDonoComentario(String idDonoComentario) {
        this.idDonoComentario = idDonoComentario;
    }

    public int getTotalCurtidasComentario() {
        return totalCurtidasComentario;
    }

    public void setTotalCurtidasComentario(int totalCurtidasComentario) {
        this.totalCurtidasComentario = totalCurtidasComentario;
    }

    public String getIdDonoPostagem() {
        return idDonoPostagem;
    }

    public void setIdDonoPostagem(String idDonoPostagem) {
        this.idDonoPostagem = idDonoPostagem;
    }

    public String getIdUsuarioInterativo() {
        return idUsuarioInterativo;
    }

    public void setIdUsuarioInterativo(String idUsuarioInterativo) {
        this.idUsuarioInterativo = idUsuarioInterativo;
    }

    public String getDescricaoDenunciaPostagem() {
        return descricaoDenunciaPostagem;
    }

    public void setDescricaoDenunciaPostagem(String descricaoDenunciaPostagem) {
        this.descricaoDenunciaPostagem = descricaoDenunciaPostagem;
    }

    public String getDataDenunciaPostagem() {
        return dataDenunciaPostagem;
    }

    public void setDataDenunciaPostagem(String dataDenunciaPostagem) {
        this.dataDenunciaPostagem = dataDenunciaPostagem;
    }

    public String getIdDenunciado() {
        return idDenunciado;
    }

    public void setIdDenunciado(String idDenunciado) {
        this.idDenunciado = idDenunciado;
    }

    public String getIdDenunciador() {
        return idDenunciador;
    }

    public void setIdDenunciador(String idDenunciador) {
        this.idDenunciador = idDenunciador;
    }

    public int getTotalDenunciasPostagem() {
        return totalDenunciasPostagem;
    }

    public void setTotalDenunciasPostagem(int totalDenunciasPostagem) {
        this.totalDenunciasPostagem = totalDenunciasPostagem;
    }

    public String getDataCurtidaPostagem() {
        return dataCurtidaPostagem;
    }

    public void setDataCurtidaPostagem(String dataCurtidaPostagem) {
        this.dataCurtidaPostagem = dataCurtidaPostagem;
    }

    public int getTotalCurtidasPostagem() {
        return totalCurtidasPostagem;
    }

    public void setTotalCurtidasPostagem(int totalCurtidasPostagem) {
        this.totalCurtidasPostagem = totalCurtidasPostagem;
    }

    public String getIdPostagem() {
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public String getTituloPostagem() {
        return tituloPostagem;
    }

    public void setTituloPostagem(String tituloPostagem) {
        this.tituloPostagem = tituloPostagem;
    }

    public String getDescricaoPostagem() {
        return descricaoPostagem;
    }

    public void setDescricaoPostagem(String descricaoPostagem) {
        this.descricaoPostagem = descricaoPostagem;
    }

    public String getDataPostagem() {
        return dataPostagem;
    }

    public void setDataPostagem(String dataPostagem) {
        this.dataPostagem = dataPostagem;
    }

    public String getSinalizarRefresh() {
        return sinalizarRefresh;
    }

    public void setSinalizarRefresh(String sinalizarRefresh) {
        this.sinalizarRefresh = sinalizarRefresh;
    }

    public int getTotalComentarios() {
        return totalComentarios;
    }

    public void setTotalComentarios(int totalComentarios) {
        this.totalComentarios = totalComentarios;
    }

    public String getComentarioPostado() {
        return comentarioPostado;
    }

    public void setComentarioPostado(String comentarioPostado) {
        this.comentarioPostado = comentarioPostado;
    }

    public String getOcultarComentario() {
        return ocultarComentario;
    }

    public void setOcultarComentario(String ocultarComentario) {
        this.ocultarComentario = ocultarComentario;
    }

    public String getDataComentario() {
        return dataComentario;
    }

    public void setDataComentario(String dataComentario) {
        this.dataComentario = dataComentario;
    }

    public String getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }

    public static Comparator<Postagem> PostagemDataEF = new Comparator<Postagem>() {
        @Override
        public int compare(Postagem t2, Postagem t1) {
            return t1.getDataPostagem().compareTo(t2.getDataPostagem());
        }
    };

    public static Comparator<Postagem> PostagemComentarioDS = new Comparator<Postagem>() {
        @Override
        public int compare(Postagem t2n, Postagem t1n) {
            return t1n.getDataComentario().compareTo(t2n.getDataComentario());
        }
    };

    public static Comparator<Postagem> PostagemCurtidaDS = new Comparator<Postagem>() {
        @Override
        public int compare(Postagem t2n, Postagem t1n) {
            return t1n.getDataCurtidaPostagem().compareTo(t2n.getDataCurtidaPostagem());
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Postagem)) return false;
        Postagem postagem = (Postagem) obj;
        return Objects.equals(getIdPostagem(), postagem.getIdPostagem());
    }

    @Override
    public int hashCode() {
        //Adicionado para ajudar a verificar igualdade.
        return Objects.hash(getIdPostagem());
    }
}
