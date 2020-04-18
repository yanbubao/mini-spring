package april.spring.mvcframework.demo.controller;

import april.spring.mvcframework.annotation.MiniAutowired;
import april.spring.mvcframework.annotation.MiniController;
import april.spring.mvcframework.annotation.MiniRequestMapping;
import april.spring.mvcframework.annotation.MiniRequestParam;
import april.spring.mvcframework.demo.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yanzx
 * @date 2020/04/18 16:28
 */
@MiniController
@MiniRequestMapping("/demo")
public class DemoController {

    @MiniAutowired
    private IDemoService iDemoService;

    @MiniRequestMapping("/query")
    public void getName(@MiniRequestParam("userName") String userName,
                        HttpServletRequest req,
                        HttpServletResponse resp) {

        String result = iDemoService.getName(userName);

        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MiniRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @MiniRequestParam("a") Integer a, @MiniRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MiniRequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @MiniRequestParam("a") Double a, @MiniRequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MiniRequestMapping("/remove")
    public String remove(@MiniRequestParam("id") Integer id) {
        return "" + id;
    }

}
