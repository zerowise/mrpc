package com.github.zerowise.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscAtomicArrayQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 ** @createtime : 2018/10/15下午8:41
 **/
public class BatchSender implements Sender{

    public static final int MAX_SEND_BUFFER_SIZE = 1024;
    public static final int MAX_SEND_LOOP_COUNT = 16;
    public static final int MAX_BATCH_SIZE = 64;

    private final Channel channel;
    private final ChannelPromise voidPromise;
    private final EventLoop eventLoop;

    private final MpscAtomicArrayQueue<Object> sendBuffer //
            = new MpscAtomicArrayQueue<>(MAX_SEND_BUFFER_SIZE);

    private final Runnable batchSendTask = () -> doBatchSend();
    public BatchSender(Channel channel) {
        this.channel = channel;
        this.voidPromise = channel.voidPromise();
        this.eventLoop = channel.eventLoop();
    }

    @Override
    public void send(Object request) {
        while (!sendBuffer.offer(request)) {
            // 已经满了，必须要清理
            eventLoop.execute(batchSendTask);
        }

        if (!sendBuffer.isEmpty()) {
            eventLoop.execute(batchSendTask);
        }
    }

    private void doBatchSend() {
        if (sendBuffer.isEmpty()) {
            return;
        }

        List batchList = new ArrayList();

        for (int r = 0; r < MAX_SEND_LOOP_COUNT; r++) {
            for (int i = 0; i < MAX_BATCH_SIZE; i++) {
                Object request = sendBuffer.poll();

                if (request != null) {
                    batchList.add(request);
                } else {
                    break;
                }
            }

            if (!batchList.isEmpty()) {
                channel.write(batchList, voidPromise);
                batchList.clear();
            }

            if (sendBuffer.isEmpty()) {
                break;
            }
        }

        channel.flush();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

}
