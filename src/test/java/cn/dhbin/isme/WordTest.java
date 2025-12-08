package cn.dhbin.isme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.deepoove.poi.XWPFTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class WordTest {
    @Test
    public void wordTest() throws IOException {
        // Using classpath resource instead of file path
        InputStream templateStream = new FileInputStream("D:\\daima\\template.docx");
        
        if (templateStream == null) {
            System.err.println("Template file not found in classpath. Creating a sample template for demonstration.");
            Map<String, Object> title = new HashMap<String, Object>();
            title.put("title", "Hi, poi-tl Word模板引擎");
            return;
        }
        
        String OUT_PATH = "D:\\daima\\output.docx";
        Map<String, Object> title = new HashMap<String, Object>();
        title.put("title", "Hi, poi-tl Word模板引擎");
        XWPFTemplate template = XWPFTemplate.compile(templateStream).render(title);
        template.writeAndClose(Files.newOutputStream(Paths.get(OUT_PATH)));
    }
}