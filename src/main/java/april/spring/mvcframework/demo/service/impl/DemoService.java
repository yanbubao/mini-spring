package april.spring.mvcframework.demo.service.impl;

import april.spring.mvcframework.annotation.MiniService;
import april.spring.mvcframework.demo.service.IDemoService;

/**
 * @author yanzx
 * @date 2020/04/18 16:30
 */
@MiniService
public class DemoService implements IDemoService {


    @Override
    public String getName(String userName) {
        return "My name is " + userName;
    }
}
