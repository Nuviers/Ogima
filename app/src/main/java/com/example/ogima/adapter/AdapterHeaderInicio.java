package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.AddDailyShortsActivity;
import com.example.ogima.activity.CadProfileParceiroActivity;
import com.example.ogima.activity.EdicaoGeralParcActivity;
import com.example.ogima.activity.LobbyChatRandomActivity;
import com.example.ogima.activity.LogicaFeedTesteActivity;
import com.example.ogima.activity.NotificationsTesteActivity;
import com.example.ogima.activity.PermissaoSegundoPlanoActivity;
import com.example.ogima.activity.TesteQRCodeActivity;
import com.example.ogima.activity.TesteTenorActivity;
import com.example.ogima.activity.UsersDailyShortsActivity;
import com.example.ogima.helper.Base64Custom;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.GlideCustomizado;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class AdapterHeaderInicio extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private String idUsuario;
    private String emailUsuario;
    private boolean statusEpilepsia = true;
    private BottomSheetDialog bottomSheetDialog;

    public AdapterHeaderInicio(Context c) {
        this.context = c;
        this.emailUsuario = autenticacao.getCurrentUser().getEmail();
        this.idUsuario = Base64Custom.codificarBase64(emailUsuario);
    }

    public void setStatusEpilepsia(boolean statusEpilepsia) {
        this.statusEpilepsia = statusEpilepsia;
        notifyDataSetChanged();
    }

    public boolean isStatusEpilepsia() {
        return statusEpilepsia;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_header_inicio, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView txtViewVerStickers, txtDailyShorts;
        private ImageView imgViewGifFireDestaque;
        private LinearLayout linearLayoutVerDaily;

        private ImageButton imgBtnAddDailyShorts, imgBtnVerDailyShorts;
        private TextView txtViewAddDailyShorts, txtViewVerDailyShorts;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);

            txtViewVerStickers = itemView.findViewById(R.id.txtViewVerStickers);
            txtDailyShorts = itemView.findViewById(R.id.txtDailyShorts);
            imgViewGifFireDestaque = itemView.findViewById(R.id.imgViewGifFireDestaque);
            linearLayoutVerDaily = itemView.findViewById(R.id.linearLayoutVerDaily);

            exibirGifDestaque();
            configurarBottomSheetDialog();

            linearLayoutVerDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //*dialogOpcoesDailyShorts();
                    irParaTeste();
                }
            });

            txtDailyShorts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //*dialogOpcoesDailyShorts();
                    irParaTeste();
                }
            });
        }

        private void configurarBottomSheetDialog() {
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_funcao_daily);
        }

        private void dialogOpcoesDailyShorts() {
            bottomSheetDialog.show();
            bottomSheetDialog.setCancelable(true);

            imgBtnAddDailyShorts = bottomSheetDialog.findViewById(R.id.imgBtnAddDailyShorts);
            imgBtnVerDailyShorts = bottomSheetDialog.findViewById(R.id.imgBtnVerDailyShorts);
            txtViewAddDailyShorts = bottomSheetDialog.findViewById(R.id.txtViewAddDailyShorts);
            txtViewVerDailyShorts = bottomSheetDialog.findViewById(R.id.txtViewVerDailyShorts);

            imgBtnAddDailyShorts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irParaAddDailyShorts();
                }
            });

            txtViewAddDailyShorts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irParaAddDailyShorts();
                }
            });

            imgBtnVerDailyShorts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verUsuariosDailyShorts();
                }
            });

            txtViewVerDailyShorts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    verUsuariosDailyShorts();
                }
            });
        }

        private void irParaAddDailyShorts() {
            fecharDialog();
            Intent intent = new Intent(context, AddDailyShortsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void irParaTeste() {
            fecharDialog();
            Intent intent = new Intent(context, LobbyChatRandomActivity.class);
            //*Intent intent = new Intent(context, TesteTenorActivity.class);
            //intent.putExtra("tipoEdicao", "fotos");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void verUsuariosDailyShorts() {
            fecharDialog();
            Intent intent = new Intent(context, UsersDailyShortsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private void fecharDialog() {
            if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
        }

        private void exibirGifDestaque() {
            if (isStatusEpilepsia()) {
                GlideCustomizado.montarGlideGifLocalPorDrawableEpilepsia(context,
                        R.drawable.gif_ic_sticker_destaque, imgViewGifFireDestaque, android.R.color.transparent);
            } else if (!isStatusEpilepsia()) {
                GlideCustomizado.montarGlideGifLocalPorDrawable(context,
                        R.drawable.gif_ic_sticker_destaque, imgViewGifFireDestaque, android.R.color.transparent);
            }
        }
    }
}
