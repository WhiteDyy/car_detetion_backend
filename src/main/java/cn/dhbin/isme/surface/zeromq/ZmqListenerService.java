package cn.dhbin.isme.surface.zeromq;

import cn.dhbin.isme.surface.service.ImageProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

@Component
public class ZmqListenerService implements CommandLineRunner {

    @Autowired
    private ImageProcessingService imageProcessingService; // 这是你处理图片、生成URL、调用SSE的服务

    @Override
    public void run(String... args) throws Exception {
        // 在一个新线程中运行监听，避免阻塞主线程
        new Thread(() -> {
            try (ZContext context = new ZContext()) {
                // 创建一个PULL类型的套接字
                ZMQ.Socket socket = context.createSocket(ZMQ.PULL);
                // 绑定到端口5555
                socket.bind("tcp://*:6666");
                System.out.println("ZeroMQ PULL socket listening on tcp://*:6666");

                while (!Thread.currentThread().isInterrupted()) {
                    // 阻塞式等待并接收消息（图像的二进制数据）
                    byte[] imageData = socket.recv(0);

                    // 将处理任务交给另一个服务
                    // 这是为了快速释放ZMQ线程，让它可以接收下一条消息
                    imageProcessingService.processAndBroadcast(imageData);
                }
            }
        }).start();
    }
}