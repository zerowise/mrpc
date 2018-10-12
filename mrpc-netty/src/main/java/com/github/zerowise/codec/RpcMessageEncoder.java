package com.github.zerowise.codec;

import com.github.zerowise.tools.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

/**
 ** @createtime : 2018/10/12上午11:36
 **/

@ChannelHandler.Sharable
public class RpcMessageEncoder extends MessageToByteEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        String clazzName = msg.getClass().getName();

        int len = msg.getClass().getName().getBytes(Charset.defaultCharset()).length;
        byte[] bytes = ProtostuffUtil.toByteArray(msg);
        out.writeInt(
                1                       // classname len
                        + len           // classname
                        + bytes.length  // 数据
        ).writeByte(len).writeCharSequence(clazzName, Charset.defaultCharset());
        out.writeBytes(bytes);
    }
}
