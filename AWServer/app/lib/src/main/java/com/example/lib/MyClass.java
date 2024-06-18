package com.example.lib;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyClass {
    private static List<ServerSocketThread> clients = new ArrayList<>();
    public static void main(String[] args){
        new MyClass();
    }
    public static ConcurrentHashMap<ServerSocketThread, Boolean> gameOverFlags;
    public MyClass(){
        ServerSocket serverSocket = null;
        try {
            serverSocket= new ServerSocket(9999);
            System.out.println("--Listener Port: 9999--");
            // 1. 创建ServerSocket
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("New client connected");
                ServerSocketThread serverSocketThread = new ServerSocketThread(client,clients);
                synchronized (clients) {
                    clients.add(serverSocketThread);
                }
                serverSocketThread.start();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            try {
                if(serverSocket!=null && !serverSocket.isClosed()){
                    serverSocket.close();
                }
                for(ServerSocketThread client :clients){
                    client.socket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}