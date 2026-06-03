package dk.ek.gameapi.soap;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.Properties;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    // WSDL generated at GET /ws/games.wsdl
    @Bean(name = "games")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema gamesSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("GamesPort");
        wsdl.setLocationUri("/ws");
        wsdl.setTargetNamespace("http://gameapi.ek.dk/soap");
        wsdl.setSchema(gamesSchema);
        return wsdl;
    }

    @Bean
    public XsdSchema gamesSchema() {
        return new SimpleXsdSchema(new ClassPathResource("games.xsd"));
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("dk.ek.gameapi.soap.gen");
        return marshaller;
    }

    // 2 Faults. Mappings to the same used by REST
    @Bean
    public SoapFaultMappingExceptionResolver exceptionResolver() {
        SoapFaultMappingExceptionResolver resolver = new SoapFaultMappingExceptionResolver();

        Properties mappings = new Properties();
        mappings.setProperty("dk.ek.gameapi.exception.ResourceNotFoundException", "CLIENT,Game not found");
        mappings.setProperty("java.lang.IllegalArgumentException", "CLIENT,Invalid request");
        resolver.setExceptionMappings(mappings);

        SoapFaultDefinition defaultFault = new SoapFaultDefinition();
        defaultFault.setFaultCode(SoapFaultDefinition.SERVER);
        defaultFault.setFaultStringOrReason("Internal server error");
        resolver.setDefaultFault(defaultFault);

        resolver.setOrder(1);
        return resolver;
    }
}
