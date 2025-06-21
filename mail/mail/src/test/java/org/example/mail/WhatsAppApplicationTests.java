package org.example.mail;

import org.example.mail.controller.WhatsAppController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WhatsAppApplicationTests {
    @Autowired
    WhatsAppController whatsAppController;

    /*修改里面的手机号码和你想发送的消息
    此外还需要注意，该手机号码需要加入（你的或我的）Twilio Sandbox，否则消息无法正常发送
     */
    @Test
    void Test() {
        whatsAppController.sendMessage("+447570559250","Hello World");
    }
}
