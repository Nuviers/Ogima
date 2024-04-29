package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.AddGroupUsersActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterLstGrupoHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String idUsuario = "";
    private boolean limiteCadAtingido = false;
    private boolean cadIndisponivel = false;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Context context;
    private BottomSheetDialog bottomSheetDialog;

    private interface VerificaLimiteCadCallback {
        void onConcluido();
    }

    public AdapterLstGrupoHeader(Context c) {
        this.context = c;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lstc_grupo_header, parent, false);
        return new AdapterLstGrupoHeader.HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutLstHeader;
        private ImageButton imgBtnCadGrupo;
        private Button btnCadGrupo;
        private Snackbar snackbarLimiteGrupo;

        //BottomSheetDialog
        private Button btnGrupoPublico, btnGrupoPrivado;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutLstHeader = itemView.findViewById(R.id.linearLayoutLstGrupoHeader);
            imgBtnCadGrupo = itemView.findViewById(R.id.imgBtnCadGrupo);
            btnCadGrupo = itemView.findViewById(R.id.btnCadGrupo);

            configurarBottomSheetDialog();

            verificarLimiteComunidade(new VerificaLimiteCadCallback() {
                @Override
                public void onConcluido() {
                    linearLayoutLstHeader.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irParaCadComunidade();
                        }
                    });

                    imgBtnCadGrupo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irParaCadComunidade();
                        }
                    });

                    btnCadGrupo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irParaCadComunidade();
                        }
                    });
                }
            });
        }

        private void verificarLimiteComunidade(VerificaLimiteCadCallback callback) {
            DatabaseReference verificaLimiteRef = firebaseRef.child("usuarios")
                    .child(idUsuario).child("idMeusGrupos");
            verificaLimiteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                        };
                        ArrayList<String> idsGrupos = snapshot.getValue(t);
                        if (idsGrupos != null
                                && idsGrupos.size() >= 5) {
                            limiteCadAtingido = true;
                        } else {
                            limiteCadAtingido = false;
                        }
                        callback.onConcluido();
                    } else {
                        limiteCadAtingido = false;
                        callback.onConcluido();
                    }
                    verificaLimiteRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    cadIndisponivel = true;
                    callback.onConcluido();
                }
            });
        }

        private void irParaCadComunidade(){
            if (cadIndisponivel) {
                ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.error_community_registration), context);
                return;
            }
            if (limiteCadAtingido) {
                //Usuário atual já tem 5 comunidades criadas.
                snackbarLimiteGrupo = Snackbar.make(btnCadGrupo, context.getString(R.string.community_creation_limit),
                        Snackbar.LENGTH_LONG);
                View snackbarView = snackbarLimiteGrupo.getView();
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                textView.setMaxLines(8); // altera o número máximo de linhas exibidas
                snackbarLimiteGrupo.show();
            } else {
                bottomSheetDialog.show();
                bottomSheetDialog.setCancelable(true);

                btnGrupoPublico = bottomSheetDialog.findViewById(R.id.btnViewComunidadePublica);
                btnGrupoPrivado = bottomSheetDialog.findViewById(R.id.btnViewComunidadePrivada);

                btnGrupoPublico.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intentParaCadGrupo(true);
                    }
                });

                btnGrupoPrivado.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       intentParaCadGrupo(false);
                    }
                });
            }
        }

        private void intentParaCadGrupo(boolean grupoPublico){
            fecharDialog();
            Intent intent = new Intent(context, AddGroupUsersActivity.class);
            intent.putExtra("grupoPublico", grupoPublico);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void configurarBottomSheetDialog() {
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_tipo_comunidade);
        }

        private void fecharDialog() {
            if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
        }
    }
}
