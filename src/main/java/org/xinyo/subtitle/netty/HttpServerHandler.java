package org.xinyo.subtitle.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.netty.util.HttpUtils;

@Component
@Log4j2
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        HttpServerDispatchHandler.Result result = HttpServerDispatchHandler.dispatch(msg);

        HttpUtils.response(ctx, result);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
