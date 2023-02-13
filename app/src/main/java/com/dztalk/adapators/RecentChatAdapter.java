package com.dztalk.adapators;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.dztalk.R;
import com.dztalk.databinding.ItemContainerRecentChatBinding;
import com.dztalk.listeners.ChatListener;
import com.dztalk.models.ChatMessage;
import com.dztalk.modules.Users;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ChatViewHolder>{

    private final List<ChatMessage> chatMessageList;
    private final ChatListener chatListener;

    public RecentChatAdapter(List<ChatMessage> chatMessageList, ChatListener chatListener){
        this.chatMessageList = chatMessageList;
        this.chatListener = chatListener;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType){
        return new ChatViewHolder(
                ItemContainerRecentChatBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChatAdapter.ChatViewHolder holder, int position){
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount(){
        return chatMessageList.size();
    }

    /**
     * The inner chat view holder
     */
    class ChatViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentChatBinding binding;
        ChatViewHolder(ItemContainerRecentChatBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(ChatMessage chatMessage){
            binding.imageProfile.setImageBitmap(getChatImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversationName);
            if(chatMessage.type!= null && chatMessage.type.equals("image")){
                binding.textRecentMessage.setText(R.string.chat_image_type);
            }else if(chatMessage.type!= null && chatMessage.type.equals("location")){
                binding.textRecentMessage.setText(R.string.chat_location_type);
            }else if(chatMessage.type!= null && chatMessage.type.equals("message")){
                if(chatMessage.message.length()>20){
                    binding.textRecentMessage.setText(chatMessage.message.substring(0,20));
                }else {
                    binding.textRecentMessage.setText(chatMessage.message);
                }
            }
            binding.getRoot().setOnClickListener( v -> {
                Users user = new Users();
                user.id = chatMessage.conversationId;
                user.fullname = chatMessage.conversationName;
                user.image = chatMessage.conversionImage;
                chatListener.onChatClicked(user);
            });
        }
    }

    /**
     * Get the image for chat window
     * @param encodedImage
     * @return
     */
    private Bitmap getChatImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
