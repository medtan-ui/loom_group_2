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
    
    // Limits to prevent overloading
    private int userMessageCount = 0;
    private static final int MAX_MESSAGES_PER_SESSION = 10;
    private static final int MAX_CHAR_LIMIT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // Using Gemini 2.5 Flash Lite as requested
        GenerativeModel baseModel = FirebaseAI.getInstance().generativeModel("gemini-2.5-flash-lite");
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
            messages.add(new ChatMessage("Hi Rider! I'm Loom AI. Ask me about your Loom trips, fuel efficiency, destinations, or distance logging!", false));
            adapter.notifyItemInserted(0);
        }

        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String userMessage = etMessage.getText().toString().trim();
        if (userMessage.isEmpty()) return;

        // Check character limit
        if (userMessage.length() > MAX_CHAR_LIMIT) {
            messages.add(new ChatMessage("Loom AI: Your message is too long. Please keep it under 200 characters.", false));
            adapter.notifyItemInserted(messages.size() - 1);
            rvChat.scrollToPosition(messages.size() - 1);
            return;
        }

        // Check session message limit
        if (userMessageCount >= MAX_MESSAGES_PER_SESSION) {
            messages.add(new ChatMessage("Loom AI: You've reached the message limit for this session. Please return later!", false));
            adapter.notifyItemInserted(messages.size() - 1);
            rvChat.scrollToPosition(messages.size() - 1);
            return;
        }

        userMessageCount++;
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
                // Strict Prompt Engineering
                String systemInstruction = "You are Loom AI, the official assistant for the Loom App. "
                        + "The Loom app is designed for motorcycle riders to log trips, plan routes, track destinations, calculate travel distances, and monitor average fuel consumption. "
                        + "Your ONLY purpose is to answer questions related to the Loom app and motorcycle trip management. "
                        + "STRICT RULE: If a user asks about anything unrelated to Loom app features or motorcycle trips (e.g., math, science, history, general knowledge, or 'the value of pi'), "
                        + "you MUST politely decline and state: 'I am sorry, but I can only assist with Loom-related trip and motorcycle queries.' "
                        + "Be concise, helpful, and rider-focused. ";

                String enhancedPrompt = systemInstruction + "\n\nUser Question: " + userMessage;

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
