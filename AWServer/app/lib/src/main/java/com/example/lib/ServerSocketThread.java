package com.example.lib;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerSocketThread extends Thread{
    private BufferedReader in;
    private PrintWriter pw;
    protected Socket socket;
    private static List<ServerSocketThread> clients;  // 静态变量
    private static ConcurrentHashMap<ServerSocketThread, Integer> scores = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<ServerSocketThread, Boolean> gameOverFlags = new ConcurrentHashMap<>();
    private static int clientCount = 0;  // 静态变量来追踪客户端数量

    public ServerSocketThread(Socket socket,List<ServerSocketThread> serverClients){
        this.socket = socket;
        clients = serverClients;
    }

    @Override
    public void run(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            pw = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
            synchronized (clients) {
                clientCount++;
                if (clientCount >= 2) {
                    for (ServerSocketThread client : clients) {
                        client.pw.println("start");
                        try {
                            Thread.sleep(10);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            String content;
            while ((content = in.readLine()) != null) {
                System.out.println(this+content);
                if (content.equals("gameover")) {
                    gameOverFlags.put(this, true);
                    System.out.println(gameOverFlags+"!!!!");
                    broadcastGameStatus();
                }
                else if(content.equals("shutdown")){
                    break;
                }
                else{
                    try {
                        int score = Integer.parseInt(content);
                        scores.put(this, score);
                        gameOverFlags.put(this, false);
                        broadcastScores();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(pw!=null){
                    pw.close();
                }
                if(in!=null){
                    in.close();
                }
                if(socket!=null){
                    socket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    private void broadcastScores() {
        synchronized (clients) {
            for (ServerSocketThread client : clients) {
                if (client != this) {
                    int opponentScore = scores.getOrDefault(this, 10);
                    client.pw.println("score:" + opponentScore);
                    try {
                        Thread.sleep(10);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private void broadcastGameStatus() {
        synchronized (clients) {
            boolean allGameOver = true;
            System.out.println("broadcastGameStatus");
            for(boolean value : gameOverFlags.values()){
                if(!value || gameOverFlags.size()<2){
                    allGameOver = false;
                    break;
                }
            }
            if (allGameOver) {
                for (ServerSocketThread client : clients) {
                    client.pw.println("end");
                    System.out.println(client+"end");
                    try {
                        Thread.sleep(50);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
         }
    }
}
