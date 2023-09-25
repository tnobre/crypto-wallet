package com.crypto.wallet.locker;

import com.crypto.wallet.engine.dto.WalletStatus;

import java.util.concurrent.locks.ReentrantLock;

public class WalletStatusLocker {
    private WalletStatus resource = new WalletStatus();
    private final ReentrantLock lock = new ReentrantLock(true);

    public WalletStatus readResource() {
        lock.lock();
        return resource;
    }

    public WalletStatus readResourceAndUnlock() {
        lock.lock();
        try {
            return resource;
        } finally {
            lock.unlock();
        }
    }

    public void unlockReadResource() {
        lock.unlock();
    }

    public void writeResource(WalletStatus value) {
        lock.lock();
        try {
            resource = value;
        } finally {
            lock.unlock();
        }
    }

}