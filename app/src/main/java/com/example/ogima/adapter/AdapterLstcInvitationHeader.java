package com.example.ogima.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ogima.R;
import com.example.ogima.activity.CommunityInvitationsActivity;
import com.example.ogima.helper.ConfiguracaoFirebase;
import com.example.ogima.helper.ToastCustomizado;
import com.example.ogima.helper.UsuarioUtils;
import com.google.firebase.database.DatabaseReference;

public class AdapterLstcInvitationHeader extends RecyclerView.Adapter<AdapterLstcInvitationHeader.HeaderViewHolder> {

    private String idUsuario = "";
    private boolean existeConvite = false;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDataBase();
    private Context context;

    public AdapterLstcInvitationHeader(Context c, boolean existeConvite) {
        this.context = c;
        this.existeConvite = existeConvite;
        idUsuario = UsuarioUtils.recuperarIdUserAtual();
    }

    public void setExisteConvite(boolean existeConvite) {
        this.existeConvite = existeConvite;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HeaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lstc_invitation_header, parent, false);
        return new AdapterLstcInvitationHeader.HeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeaderViewHolder holder, int position) {
        if (existeConvite) {
            holder.linearLayoutSemConvites.setVisibility(View.GONE);
            holder.linearLayoutConvites.setVisibility(View.VISIBLE);
        }else{
            holder.linearLayoutConvites.setVisibility(View.GONE);
            holder.linearLayoutSemConvites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayoutConvites, linearLayoutSemConvites;
        private ImageButton imgBtnVerConvitesComunidade;
        private Button btnVerConvitesComunidade;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutConvites = itemView.findViewById(R.id.linearLayoutInvitationHeader);
            linearLayoutSemConvites = itemView.findViewById(R.id.linearLayoutNoInvitationsHeader);
            imgBtnVerConvitesComunidade = itemView.findViewById(R.id.imgBtnVerConvitesComunidade);
            btnVerConvitesComunidade = itemView.findViewById(R.id.btnVerConvitesComunidade);

            linearLayoutConvites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verConvites();
                }
            });

            imgBtnVerConvitesComunidade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verConvites();
                }
            });

            btnVerConvitesComunidade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verConvites();
                }
            });
        }
        private void verConvites(){
            if (existeConvite) {
                Intent intent = new Intent(context, CommunityInvitationsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }else{
                ToastCustomizado.toastCustomizadoCurto(context.getString(R.string.no_community_invites), context);
            }
        }
    }
}
