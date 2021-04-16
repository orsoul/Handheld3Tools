package com.fanfull.libjava.io.socketClient.netty;

import com.fanfull.libjava.util.Logs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by haoxy on 2018/11/15.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 */
public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {
  private int byteRead;
  private volatile int start = 0;
  private volatile int lastLength = 0;
  public RandomAccessFile randomAccessFile;
  private FileUploadFile fileUploadFile;

  public FileUploadClientHandler(FileUploadFile ef) {
    if (ef.getFile().exists()) {
      if (!ef.getFile().isFile()) {
        System.out.println("Not a file :" + ef.getFile());
        return;
      }
    }
    this.fileUploadFile = ef;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
  }

  public void channelActive(ChannelHandlerContext ctx) {
    Logs.out("正在执行channelActive()方法.....");
    try {
      randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(),
          "r");
      randomAccessFile.seek(fileUploadFile.getStarPos());
      // lastLength = (int) randomAccessFile.length() / 10;
      lastLength = 1024 * 10;
      byte[] bytes = new byte[lastLength];
      if ((byteRead = randomAccessFile.read(bytes)) != -1) {
        fileUploadFile.setEndPos(byteRead);
        fileUploadFile.setBytes(bytes);
        ctx.writeAndFlush(fileUploadFile);//发送消息到服务端
      } else {
      }
      Logs.out("channelActive()文件已经读完 " + byteRead);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException i) {
      i.printStackTrace();
    }
    Logs.out("channelActive()方法执行结束");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
      throws Exception {
    if (msg instanceof Integer) {
      start = (Integer) msg;
      if (start != -1) {
        randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
        randomAccessFile.seek(start); //将文件定位到start
        Logs.out("长度：" + (randomAccessFile.length() - start));
        int a = (int) (randomAccessFile.length() - start);
        int b = (int) (randomAccessFile.length() / 1024 * 2);
        if (a < lastLength) {
          lastLength = a;
        }
        Logs.out("文件长度："
            + (randomAccessFile.length())
            + ",start:"
            + start
            + ",a:"
            + a
            + ",b:"
            + b
            + ",lastLength:"
            + lastLength);
        byte[] bytes = new byte[lastLength];
        Logs.out("bytes的长度是=" + bytes.length);
        if ((byteRead = randomAccessFile.read(bytes)) != -1
            && (randomAccessFile.length() - start) > 0) {
          Logs.out("byteRead = " + byteRead);
          fileUploadFile.setEndPos(byteRead);
          fileUploadFile.setBytes(bytes);
          try {
            ctx.writeAndFlush(fileUploadFile);
          } catch (Exception e) {
            e.printStackTrace();
          }
        } else {
          randomAccessFile.close();
          ctx.close();
          Logs.out("文件已经读完channelRead()--------" + byteRead);
        }
      }
    }
  }

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}