package com.example.loom_group_2.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.TextPart;
import com.example.loom_group_2.R;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.type.GenerateContentResponse;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageView btnBack;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnSend;

    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        GenerativeModel baseModel = FirebaseAI.getInstance().generativeModel("gemini-3-flash-preview");
        model = GenerativeModelFutures.from(baseModel);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Welcome message (motorcycle trip focused)
        if (messages.isEmpty()) {
            messages.add(new ChatMessage("Hi Rider! I'm Loom AI. Ask me about fuel efficiency, route planning, trip logs, motorcycle maintenance, or anything else!", false));
            adapter.notifyItemInserted(0);
        }

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String userMessage = etMessage.getText().toString().trim();
        if (userMessage.isEmpty()) return;

        messages.add(new ChatMessage(userMessage, true));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
        etMessage.setText("");

        messages.add(new ChatMessage("Loom AI is thinking...", false));
        int thinkingIndex = messages.size() - 1;
        adapter.notifyItemInserted(thinkingIndex);
        rvChat.scrollToPosition(thinkingIndex);

        new Thread(() -> {
            try {
                String enhancedPrompt = "You are Loom AI, a helpful assistant for all vehicle riders and trip logging. "
                        + "The user is using the Loom app for trip management, fuel calculation, and route planning. "
                        + "Be friendly, practical, and give useful advice for riders. User message: " + userMessage;

                Content prompt = new Content.Builder()
                        .addPart(new TextPart(enhancedPrompt))
                        .build();
                ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(prompt);
                GenerateContentResponse result = responseFuture.get();
                String response = result.getText();

                runOnUiThread(() -> {
                    if (!messages.isEmpty()) {
                        int index = messages.size() - 1;
                        messages.set(index, new ChatMessage(response != null ? response : "No response.", false));
                        adapter.notifyItemChanged(index);
                        rvChat.scrollToPosition(index);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!messages.isEmpty()) {
                        int index = messages.size() - 1;
                        messages.set(index, new ChatMessage("Error: " + e.getMessage(), false));
                        adapter.notifyItemChanged(index);
                    }
                });
            }
        }).start();
    }
}
