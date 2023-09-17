package com.example.ogima.helper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.model.Grupo;
import com.example.ogima.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GrupoBloqueadoDAO {

    //private DatabaseReference databaseReference;
    private List<Grupo> listaGruposBloqueados = new ArrayList<>();
    private RecyclerView.Adapter adapter;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private String emailUsuario, idUsuario;

    private DatabaseReference grupoRef;
    private DatabaseReference usuarioRef;
    private Context context;

    private HashMap<String, Integer> hashMapGrupos;

    public GrupoBloqueadoDAO(RecyclerView.Adapter adapter, List<Grupo> listGrupo, Context context) {
        this.adapter = adapter;
        this.listaGruposBloqueados = listGrupo;
        this.context = context;
        this.hashMapGrupos = new HashMap<>();

        //Configurações iniciais.
        emailUsuario = autenticacao.getCurrentUser().getEmail();
        idUsuario = Base64Custom.codificarBase64(emailUsuario);


        for (int i = 0; i < listaGruposBloqueados.size(); i++) {
            hashMapGrupos.put(listaGruposBloqueados.get(i).getIdGrupo(), i);
        }

        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        grupoRef = firebaseRef.child("grupos");

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Usuario usuarioAtual = snapshot.getValue(Usuario.class);

                    grupoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshotGrupo : snapshot.getChildren()) {
                                if (snapshotGrupo.getValue() != null) {
                                    Grupo grupoBloqueado = snapshotGrupo.getValue(Grupo.class);

                                    if (usuarioAtual.getIdGruposBloqueados() != null
                                            && usuarioAtual.getIdGruposBloqueados().size() > 0
                                            && usuarioAtual.getIdGruposBloqueados().contains(grupoBloqueado.getIdGrupo())) {

                                        adicionarGrupoBloqueado(grupoBloqueado);
                                    }
                                }
                            }
                            grupoRef.removeEventListener(this);
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

            }
        });
    }

    public void adicionarGrupoBloqueado(Grupo grupoNovo) {
        if (!hashMapGrupos.containsKey(grupoNovo.getIdGrupo())) {
            listaGruposBloqueados.add(grupoNovo);
            hashMapGrupos.put(grupoNovo.getIdGrupo(), listaGruposBloqueados.size() - 1);
            adapter.notifyDataSetChanged();
        } else {
            listaGruposBloqueados.set(hashMapGrupos.get(grupoNovo.getIdGrupo()), grupoNovo);
        }
    }

    public void limparListaGrupoBloqueado() {
        listaGruposBloqueados.clear();
        adapter.notifyDataSetChanged();
    }

    public void removerGrupoBloqueado(Grupo grupoRemovido){

        if (hashMapGrupos.containsKey(grupoRemovido.getIdGrupo())) {
            int posicao = hashMapGrupos.get(grupoRemovido.getIdGrupo());
            if (posicao != -1) {
                listaGruposBloqueados.remove(posicao);
                hashMapGrupos.remove(grupoRemovido.getIdGrupo());
                adapter.notifyItemRemoved(posicao);

                // Atualiza as posições dos usuários no HashMap após a remoção
                for (int i = posicao; i < listaGruposBloqueados.size(); i++) {
                    Grupo grupoAtualizado = listaGruposBloqueados.get(i);
                    hashMapGrupos.put(grupoAtualizado.getIdGrupo(), i);
                }
            }
        }
    }
}