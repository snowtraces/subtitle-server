package org.xinyo.subtitle.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    static final EventExecutorGroup EVENT_EXECUTORS = new DefaultEventExecutorGroup(16);

@Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(EVENT_EXECUTORS, new HttpUploadHandler());
        pipeline.addLast(new HttpObjectAggregator(2 * 1024 *1024));
        pipeline.addLast(EVENT_EXECUTORS, new HttpServerHandler());
    }
}
