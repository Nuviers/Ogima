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
import com.example.ogima.activity.CreateCommunityActivity;
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

public class AdapterLstcHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String idUsuario = "";
    private boolean limiteCadAtingido = false;
    private boolean cadIndisponivel = false;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Context context;
    private BottomSheetDialog bottomSheetDialog;

    private interface VerificaLimiteCadCallback {
        void onConcluido();
    }

    public AdapterLstcHeader(Context c) {
        this.context = c;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lstc_header, parent, false);
        return new AdapterLstcHeader.HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutLstcHeader;
        private ImageButton imgBtnCadComunidade;
        private Button btnCadComunidade;
        private Snackbar snackbarLimiteComunidade;

        //BottomSheetDialog
        private Button btnComunidadePublica, btnComunidadePrivada;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutLstcHeader = itemView.findViewById(R.id.linearLayoutLstcHeader);
            imgBtnCadComunidade = itemView.findViewById(R.id.imgBtnCadComunidade);
            btnCadComunidade = itemView.findViewById(R.id.btnCadComunidade);

            configurarBottomSheetDialog();

            verificarLimiteComunidade(new VerificaLimiteCadCallback() {
                @Override
                public void onConcluido() {
                    linearLayoutLstcHeader.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irParaCadComunidade();
                        }
                    });

                    imgBtnCadComunidade.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            irParaCadComunidade();
                        }
                    });

                    btnCadComunidade.setOnClickListener(new View.OnClickListener() {
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
                    .child(idUsuario).child("idMinhasComunidades");
            verificaLimiteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() != null) {
                        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                        };
                        ArrayList<String> idsComunidades = snapshot.getValue(t);
                        if (idsComunidades != null
                                && idsComunidades.size() >= 5) {
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
                snackbarLimiteComunidade = Snackbar.make(btnCadComunidade, context.getString(R.string.community_creation_limit),
                        Snackbar.LENGTH_LONG);
                View snackbarView = snackbarLimiteComunidade.getView();
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                textView.setMaxLines(8); // altera o número máximo de linhas exibidas
                snackbarLimiteComunidade.show();
            } else {
                bottomSheetDialog.show();
                bottomSheetDialog.setCancelable(true);

                btnComunidadePublica = bottomSheetDialog.findViewById(R.id.btnViewComunidadePublica);
                btnComunidadePrivada = bottomSheetDialog.findViewById(R.id.btnViewComunidadePrivada);

                btnComunidadePublica.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intentParaCadComunidade(true);
                    }
                });

                btnComunidadePrivada.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       intentParaCadComunidade(false);
                    }
                });
            }
        }

        private void intentParaCadComunidade(boolean comunidadePublica){
            fecharDialog();
            Intent intent = new Intent(context, CreateCommunityActivity.class);
            intent.putExtra("comunidadePublica", comunidadePublica);
            intent.putExtra("edit", false);
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
