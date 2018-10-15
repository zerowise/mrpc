package com.github.zerowise.codec;

import com.github.zerowise.tools.ClazzUtil;
import com.github.zerowise.tools.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import jdk.nashorn.internal.runtime.logging.Loggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 ** @createtime : 2018/10/12上午11:24
 **/
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final int maxFrameLength = 0xffff;

    public RpcMessageDecoder() {
        super(maxFrameLength, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf) super.decode(ctx, in);
        if (byteBuf == null) {
            return null;
        }

        int clsNameLen = byteBuf.readByte() & 0xff;

        String clazzName = (String) byteBuf.readCharSequence(clsNameLen, Charset.defaultCharset());

        Class clazz = ClazzUtil.findClazz(clazzName);
        if (clazz == null) {
            return null;
        }
        byte[] b1;
        byteBuf.readBytes((b1 = new byte[byteBuf.readableBytes()]));
        return ProtostuffUtil.newInstance(clazz, b1);

    }
}
