package com.example.linkvault;

public class Singleton {
    private static Singleton instance;
    private MainActivity mainActivityInstance;

    private Singleton() {
    }

    public static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public void setMainActivityInstance(MainActivity activity) {
        mainActivityInstance = activity;
    }

    public MainActivity getMainActivityInstance() {
        return mainActivityInstance;
    }
}

