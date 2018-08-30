package com.wangchun.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetAddress;

/**
 * Created by Administrator on 2018/8/29.
 */
public class NettyServer {
    public static void main(String[] args) {
        int port = 9430;
        //boss线程池
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //work线程池
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            //服务器启动器
            ServerBootstrap bootstrap = new ServerBootstrap();
            //指定netty的Boss线程和work线程
            bootstrap.group(bossGroup,workGroup);
            //设置服务器通道类
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel channel) throws Exception {
                    channel.pipeline().addLast("split",new DelimiterBasedFrameDecoder(1000, Delimiters.lineDelimiter()));
                    channel.pipeline().addLast("decoder",new StringDecoder());//对字符串进行处理  解码器
                    channel.pipeline().addLast("encoder",new StringEncoder());//对字符串进行处理  编码器
                    channel.pipeline().addLast("hander",new FirstServerHandler());//自定义的处理器
                }
            });
                ChannelFuture future  = bootstrap.bind(port).sync();//设置绑定的端口
                future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}

class FirstServerHandler extends SimpleChannelInboundHandler<String> {
    /**
     * 消息过来后执行此方法
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+":"+msg);
        ctx.writeAndFlush("received your message:"+msg);
    }
    /**
     * 通道被客户端激活时执行此方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("RamoteAddress : " + ctx.channel().remoteAddress() + " active !");//通道激活
        ctx.writeAndFlush( "Welcome to " + InetAddress.getLocalHost().getHostName() + " service!\n");//回送进入服务系统
    }

}
