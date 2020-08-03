package com.fanfull.utils;

import android.util.Xml;
import com.fanfull.entity.IPConfigBean;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ParserXMLConfig {

  /**
   * @return 返回 实体类 IPConfigBean
   * @throws Exception
   * @Description: 解析 记录 IP和端口 的 XML文件
   */
  public static IPConfigBean parser(InputStream is) throws Exception {
    IPConfigBean config = new IPConfigBean();

    XmlPullParser parser = Xml.newPullParser();
    parser.setInput(is, "UTF-8");

    int eventType = parser.getEventType();
    while (eventType != XmlPullParser.END_DOCUMENT) {
      if (eventType == XmlPullParser.START_TAG) {
        if (parser.getName().endsWith("ip")) {
          parser.next();
          config.setIp(parser.getText());
        } else if (parser.getName().endsWith("prot")) {
          parser.next();
          config.setProt(parser.getText());
        }
      }
      eventType = parser.next();
    }
    return config;
  }

  public static List<IPConfigBean> getPersons(InputStream xml)
      throws Exception {
    XmlPullParser xp = Xml.newPullParser();
    xp.setInput(xml, "UTF-8"); // 为Pull解释器设置要解析的XML数据
    int type = xp.getEventType();

    List<IPConfigBean> configList = null;
    IPConfigBean config = null;
    while (type != XmlPullParser.END_DOCUMENT) {
      switch (type) {
        case XmlPullParser.START_TAG:
          if ("config".equals(xp.getName())) { // 获取当前结点的位置
            configList = new ArrayList<IPConfigBean>();
            config = new IPConfigBean();
          } else if ("ip".equals(xp.getName())) {
            String ip = xp.nextText();
            config.setIp(ip);
          } else if ("prot".equals(xp.getName())) {
            // int age = Integer.valueOf(pullParser.nextText());
            String prot = xp.nextText();
            config.setProt(prot);
          }
          break;

        case XmlPullParser.END_TAG:
          if ("config".equals(xp.getName())) {
            configList.add(config);
          }
          break;
      }

      type = xp.next();
    }
    System.err.println("config.getip" + config.getIp() + config.getProt());

    return configList;
  }

  /**
   * 保存数据到xml文件中
   *
   * @throws Exception
   */
  public static void save(List<IPConfigBean> configList, OutputStream out)
      throws Exception {
    XmlSerializer xs = Xml.newSerializer();
    xs.setOutput(out, "UTF-8");
    xs.startDocument("UTF-8", true);
    xs.startTag(null, "config");
    for (IPConfigBean config : configList) {

      xs.startTag(null, "ip");
      xs.text(config.getIp().toString());
      xs.endTag(null, "ip");
      xs.startTag(null, "prot");
      xs.text(config.getProt() + "");
      xs.endTag(null, "prot");
    }
    xs.endTag(null, "config");
    xs.endDocument();
    out.flush();
    out.close();
  }
}
