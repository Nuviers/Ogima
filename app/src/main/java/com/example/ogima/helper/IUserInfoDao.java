package com.example.ogima.helper;

import com.example.ogima.model.Informacoes;
import com.example.ogima.model.Usuario;

import java.util.List;

public interface IUserInfoDao {

    public boolean salvar (Informacoes informacoes);
    public boolean atualizar (Informacoes informacoes);
    public boolean deletar (Informacoes informacoes);
    public boolean recuperar (Informacoes informacoes);
    public List<Informacoes> listar();
}
