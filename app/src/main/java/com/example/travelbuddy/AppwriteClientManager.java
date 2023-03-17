package com.example.travelbuddy;

import android.content.Context;
import io.appwrite.Client;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;


public class AppwriteClientManager {
    private static Client client;
    private static Account account;


    public static void initialize(Context context) {
        client = new Client(context);
        client.setEndpoint("http://10.0.2.2/v1")
                .setProject("64136f06c970db619408")
                .setSelfSigned(true); // For self-signed certificates, only use for development
        account = new Account(client);
    }

    public static Client getClient() {
        return client;
    }

    public static Account getAccount() {
        return account;
    }


}

