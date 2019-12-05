package org.xinyo.subtitle.netty;

import com.google.common.io.Files;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xinyo.subtitle.entity.SRTSubtitleUnit;
import org.xinyo.subtitle.entity.UploadFile;
import org.xinyo.subtitle.netty.util.HttpUtils;
import org.xinyo.subtitle.service.SubtitleService;
import org.xinyo.subtitle.util.FileUtils;
import org.xinyo.subtitle.util.SpringContextHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private HttpRequest request;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject)
            throws Exception {
        if (httpObject instanceof HttpRequest) {
            request = (HttpRequest) httpObject;
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

    private void writeChunk(ChannelHandlerContext ctx) throws IOException {
        while (httpDecoder.hasNext()) {
            InterfaceHttpData data = httpDecoder.next();
            if (data != null && HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                final FileUpload fileUpload = (FileUpload) data;
                UploadFile uploadFile = FileUtils.createFullPath(fileUpload.getFilename());
                final File file = new File(uploadFile.getFullPath() + uploadFile.getFileName());
                log.info("upload file: {}", file);
                try (
                    FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                    FileChannel outputChannel = new FileOutputStream(file).getChannel()
                ) {
                    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());

                    if (FileUtils.isAsciiText(file)) {
                        List<String> lines = Files.readLines(file, StandardCharsets.UTF_8);
                        if (FileUtils.isSubtitle(file)) {
                            SubtitleService subtitleService = SpringContextHolder.getBean(SubtitleService.class);
                            List<SRTSubtitleUnit> list = subtitleService.readSubtitle(lines);
                            HttpUtils.response(ctx, list, HttpResponseStatus.OK);
                        } else {
                            HttpUtils.response(ctx, lines, HttpResponseStatus.OK);
                        }
                    } else {
                        HttpUtils.response(ctx, HttpResponseStatus.OK);
                    }
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("{}", cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (httpDecoder != null) {
            httpDecoder.cleanFiles();
        }
    }

}
