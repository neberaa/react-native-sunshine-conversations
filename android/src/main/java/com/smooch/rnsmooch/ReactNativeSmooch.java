package com.smooch.rnsmooch;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.Promise;

import java.util.HashMap;
import java.util.Map;
import java.lang.String;

import io.smooch.core.Smooch;
import io.smooch.core.SmoochCallback;
import io.smooch.core.User;
import io.smooch.ui.ConversationActivity;
import io.smooch.core.MessageModifierDelegate;
import io.smooch.core.Message;
import io.smooch.core.ConversationDetails;
import io.smooch.core.LogoutResult;
import io.smooch.core.LoginResult;

public class ReactNativeSmooch extends ReactContextBaseJavaModule {
    @Override
    public String getName() {
        return "SmoochManager";
    }

    public ReactNativeSmooch(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void login(String userId, String jwt, final Promise promise) {
        Smooch.login(userId, jwt, new SmoochCallback<LoginResult>() {
            @Override
            public void run(@NonNull Response<LoginResult> response) {
                if (response.getData() == InitializationStatus.SUCCESS) {
                    promise.resolve(null);
                } else {
                    promise.reject(response.getError());
                }
            }
        });
    }

    @ReactMethod
    public void logout(final Promise promise) {
        Smooch.logout(new SmoochCallback<LogoutResult>() {
            @Override
            public void run(@NonNull Response<LogoutResult> response) {
                if (response.getData() == InitializationStatus.SUCCESS) {
                    promise.resolve(null);
                } else {
                    promise.reject(response.getError());
                }
            }
        });
    }

    @ReactMethod
    public void show() {
        ConversationActivity.builder().withFlags(Intent.FLAG_ACTIVITY_NEW_TASK).show(getReactApplicationContext());
    }

    @ReactMethod
    public void close() {
        ConversationActivity.close();
    }

    @ReactMethod
    public void getUnreadCount(Promise promise) {
        int unreadCount = Smooch.getConversation().getUnreadCount();
        promise.resolve(unreadCount);
    }

    @ReactMethod
    public void setFirstName(String firstName) {
        User.getCurrentUser().setFirstName(firstName);
    }

    @ReactMethod
    public void setLastName(String lastName) {
        User.getCurrentUser().setLastName(lastName);
    }

    @ReactMethod
    public void setEmail(String email) {
        User.getCurrentUser().setEmail(email);
    }

    @ReactMethod
    public void setMetadata(ReadableMap metadata) {
        Smooch.setMessageModifierDelegate(new CustomMessageDelegate() {
            @Override
            public Message beforeDisplay(ConversationDetails conversationDetails, Message message){
                return message;
            }
            public Message beforeNotification(String conversationId, Message message) {
                return message;
            }
            public Message beforeSend(ConversationDetails conversationDetails, Message message) {
                message.setMetadata(getProperties(metadata));

                return message;
            }
        });
    }

    private Map<String, Object> getProperties(ReadableMap properties) {
        ReadableMapKeySetIterator iterator = properties.keySetIterator();
        Map<String, Object> props = new HashMap<>();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = properties.getType(key);
            if (type == ReadableType.Boolean) {
                props.put(key, properties.getBoolean(key));
            } else if (type == ReadableType.Number) {
                props.put(key, properties.getDouble(key));
            } else if (type == ReadableType.String) {
                props.put(key, properties.getString(key));
            }
        }

        return props;
    }

}
