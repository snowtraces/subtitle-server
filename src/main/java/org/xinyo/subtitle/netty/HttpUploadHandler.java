package org.xinyo.subtitle.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.SRTSubtitleUnit;
import org.xinyo.subtitle.netty.util.HttpUtils;
import org.xinyo.subtitle.util.SubtitleParseUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

@Slf4j
@Component
public class HttpUploadHandler extends SimpleChannelInboundHandler<HttpObject> {

    public HttpUploadHandler() {
        super(false);
    }

    private static final HttpDataFactory FACTORY = new DefaultHttpDataFactory(true);
    private static final String URI = "/api/fileUpload";
    private HttpPostRequestDecoder httpDecoder;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject)
            throws Exception {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if (request.uri().startsWith(URI) && request.method().equals(HttpMethod.POST)) {
                httpDecoder = new HttpPostRequestDecoder(FACTORY, request);
                httpDecoder.setDiscardThreshold(0);
            } else {
                //传递给下一个Handler
                ctx.fireChannelRead(httpObject);
            }
        }
        if (httpObject instanceof HttpContent) {
            if (httpDecoder != null) {
                final HttpContent chunk = (HttpContent) httpObject;
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    writeChunk(ctx);
                    //关闭httpDecoder
                    httpDecoder.destroy();
                    httpDecoder = null;
                }
                ReferenceCountUtil.release(httpObject);
            } else {
                ctx.fireChannelRead(httpObject);
            }
        }

    }

    private void writeChunk(ChannelHandlerContext ctx) {
        while (httpDecoder.hasNext()) {
            InterfaceHttpData data = httpDecoder.next();
            if (data != null && HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                final FileUpload fileUpload = (FileUpload) data;
                try(FileInputStream fileInputStream = new FileInputStream(fileUpload.getFile())) {
                    List<SRTSubtitleUnit> subtitleUnitList = SubtitleParseUtils.read(fileInputStream);
                    HttpUtils.response(ctx, subtitleUnitList, HttpResponseStatus.OK);
                } catch (Exception e) {
                    e.printStackTrace();
                    HttpUtils.response(ctx, HttpResponseStatus.OK);
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("请求异常：", cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (httpDecoder != null) {
            httpDecoder.cleanFiles();
        }
    }

}
