package april.spring.mvcframework.v2.servlet;

import april.spring.mvcframework.annotation.MiniAutowired;
import april.spring.mvcframework.annotation.MiniController;
import april.spring.mvcframework.annotation.MiniRequestMapping;
import april.spring.mvcframework.annotation.MiniService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author yanzx
 */
public class MiniDispatchServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MiniDispatchServlet.class);

    private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    private static final String SCAN_PACKAGE_KEY = "scanPackage";

    private Properties contextConfig = new Properties();

    /**
     * 享元模式，缓存容器中的类名称
     */
    private List<String> classNames = new ArrayList<>();

    /**
     * IoC容器
     */
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * 处理器映射器
     */
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        // 01.加载配置文件
        doLoadConfig(config.getInitParameter(CONTEXT_CONFIG_LOCATION));

        // 02.使用包扫描器收集所需的类
        doScanner(contextConfig.getProperty(SCAN_PACKAGE_KEY));

        // 03.初始化IoC容器
        doInstance();
        // AOP在这里执行，生成代理类，后续DI的对象是代理类

        // 04.依赖注入
        doAutowired();

        // 05.初始化处理器映射器HandlerMapping
        doInitHandlerMapping();

        log.info("Mini Spring finish init! ");
    }

    /**
     * 初始化处理器映射器HandlerMapping
     */
    private void doInitHandlerMapping() {

        if (ioc.isEmpty()) {
            log.info("IoC容器为空！无法初始化HandlerMapping！");
            return;
        }

        for (Map.Entry<String, Object> bean : ioc.entrySet()) {

            Class<?> beanClazz = bean.getValue().getClass();

            if (!beanClazz.isAnnotationPresent(MiniController.class)) {
                continue;
            }

            String baseUrl = "";
            if (beanClazz.isAnnotationPresent(MiniRequestMapping.class)) {
                baseUrl = beanClazz.getAnnotation(MiniRequestMapping.class).value();
            }

            // 只获取public方法
            for (Method method : beanClazz.getMethods()) {

                if (!method.isAnnotationPresent(MiniRequestMapping.class)) {
                    continue;
                }

                MiniRequestMapping requestMapping = method.getAnnotation(MiniRequestMapping.class);

                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");

                handlerMapping.put(url, method);

                log.info("mapping: {}, {}", url, method);
            }


        }

    }

    /**
     * 依赖注入
     */
    private void doAutowired() {

        if (ioc.isEmpty()) {

            log.info("IoC容器为空！无法进行DI！");
            return;
        }

        for (Map.Entry<String, Object> bean : ioc.entrySet()) {

            // 遍历该bean所有类型的字段
            for (Field field : bean.getValue().getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(MiniAutowired.class)) {
                    continue;
                }

                MiniAutowired miniAutowired = field.getAnnotation(MiniAutowired.class);

                String beanName = miniAutowired.value().trim();
                if (StringUtils.isEmpty(beanName)) {
                    beanName = field.getType().getName();
                }

                field.setAccessible(true);

                try {
                    field.set(bean.getValue(), ioc.get(beanName));

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * 初始化IoC容器
     */
    private void doInstance() {

        if (CollectionUtils.isEmpty(classNames)) {
            log.info("classNames为空！请检查scanPackage配置是否有误！");
            return;
        }

        classNames.forEach(className -> {

            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MiniController.class)) {

                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();

                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(MiniService.class)) {

                    // 01.如果出现不同包下同名的类，只能coder手动取一个全局唯一的名称
                    String beanName = clazz.getAnnotation(MiniService.class).value();
                    if (StringUtils.isEmpty(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();

                    ioc.put(beanName, instance);

                    // 02.如果是接口interface，只能有一个实现类，如果有多个实现类则抛出异常
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class i : interfaces) {
                        if (ioc.containsKey(i.getSimpleName())) {
                            throw new Exception("此bean在IoC容器中已存在！" + i.getSimpleName());
                        }

                        ioc.put(i.getSimpleName(), instance);
                    }

                } else {
                    // demo只例举MiniController和MiniService的处理方案
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });


    }

    /**
     * 自定义手写字母转小写函数
     *
     * @param simpleClassName
     * @return
     */
    private String toLowerFirstCase(String simpleClassName) {
        char[] chars = simpleClassName.toCharArray();
        chars[0] += 32;

        return String.valueOf(chars);
    }

    /**
     * 扫描相关的类，用于后续初始化IoC容器
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        String basePath = Objects.requireNonNull(url).getFile();

        log.info("base url: {}", basePath);

        File classPath = new File(basePath);

        // classPath为/april/spring/的项目根目录，可理解为一个文件夹
        for (File file : Objects.requireNonNull(classPath.listFiles())) {

            if (file.isDirectory()) {

                doScanner(scanPackage + "." + file.getName());

            } else {

                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                // 完整限定类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));

                classNames.add(className);
            }
        }
    }

    /**
     * 加载配置文件props
     *
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {

        try (
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        ) {
            contextConfig.load(resourceAsStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 委派，通过url映射找到对应method去invoke
        doDispatch(req, resp);
    }

    /**
     * dispatch.
     *
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {

        
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
