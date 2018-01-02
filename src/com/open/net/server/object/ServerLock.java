package com.open.net.server.object;

/**
 * author       :   long
 * created on   :   2017/11/30
 * description  :   服务器锁
 */

public class ServerLock {

    public synchronized void waitEnding()
    {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void notifytEnding()
    {
        this.notifyAll();
    }

}
