package com.dztalk.adapators;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dztalk.R;
import com.dztalk.databinding.ItemContainerUserBinding;
import com.dztalk.listeners.UserListener;
import com.dztalk.modules.Users;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewerHolder>{

    private final List<Users> userList;
    private final UserListener userListener;

    public UserAdapter (List<Users> userList, UserListener userListener){
        this.userList = userList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewerHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewerHolder holder, int position) {
        holder.setBinding(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewerHolder extends RecyclerView.ViewHolder{

        ItemContainerUserBinding binding;

        UserViewerHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding =itemContainerUserBinding;
        }

        void setBinding(Users users){
            String status = users.curRequestStatus;
            binding.textName.setText(users.firstname+" "+users.lastname);
            binding.textEmail.setText(users.email);
            binding.imageProfile.setImageBitmap(getUserImage(users.image));
            if(status!=null){
                if(status.equals("accepted")){
                    binding.status.setImageResource(R.drawable.ic_round_check);
                }else if(status.equals("pending")){
                    binding.status.setImageResource(R.drawable.ic_outline_pending);
                    binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(users));
                }else if(status.equals("rejected")){
                    binding.status.setImageResource(R.drawable.ic_round_clear);
                }
            }else {
                binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(users));
            }
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
}