package com.dztalk.adapators;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dztalk.databinding.ItemContainerReceivedMessageBinding;
import com.dztalk.databinding.ItemContainerSentMessageBinding;
import com.dztalk.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderID;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderID) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderID = senderID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderID)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){
            if(chatMessage.type!=null && chatMessage.type.equals("image")){
                binding.textImage.setImageBitmap(decodeImage(chatMessage.message));
                binding.imageDateTime.setText(chatMessage.dateTime);
                binding.textMessage.setVisibility(View.INVISIBLE);
                binding.textDateTime.setVisibility(View.INVISIBLE);
            }else{
                binding.textMessage.setText(chatMessage.message);
                binding.textDateTime.setText(chatMessage.dateTime);
                binding.textImage.setVisibility(View.INVISIBLE);
                binding.imageDateTime.setVisibility(View.INVISIBLE);
            }

        }

        private Bitmap decodeImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder
    {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;

        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            if(chatMessage.type!=null && chatMessage.type.equals("image")){
                binding.textImage.setImageBitmap(decodeImage(chatMessage.message));
                binding.imageDateTime.setText(chatMessage.dateTime);
                binding.textMessage.setVisibility(View.INVISIBLE);
                binding.textDateTime.setVisibility(View.INVISIBLE);
            }else{
                binding.textMessage.setText(chatMessage.message);
                binding.textDateTime.setText(chatMessage.dateTime);
                binding.textImage.setVisibility(View.INVISIBLE);
                binding.imageDateTime.setVisibility(View.INVISIBLE);
            }

            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
//        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
//            binding.textMessage.setText(chatMessage.message);
//            binding.textDateTime.setText(chatMessage.dateTime);
//            if (receiverProfileImage != null){
//                binding.imageProfile.setImageBitmap(receiverProfileImage);
//            }
//        }

        private Bitmap decodeImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }
    }
}
