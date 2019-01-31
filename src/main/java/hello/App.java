package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("*")
    public List<Map<String, Object>> foo(@RequestBody List<Map<String, Object>> body, HttpServletRequest req) {
        return body.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("foo", m);
                    map.put("number", 1);
                    map.put("path", req.getRequestURI());
                    return map;
                })
                .collect(Collectors.toList());
    }
}