/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emRede;

import io.grpc.ManagedChannel;
import java.util.Random;
import java.util.logging.Logger;

/**
 *
 * @author venturini
 */
public class Conexao {

    private static String ip;
    private static int porta;
    //private static final Logger logger = Logger.getLogger(RouteGuideClient.class.getName());

    private final ManagedChannel channel;
    //private final RouteGuideBlockingStub blockingStub;
    //private final RouteGuideStub asyncStub;

    private Random random = new Random();
    //private TestHelper testHelper;

    public Conexao(String ip, int porta){
        this(ManagedChannelBuilder.forAddress(ip, porta).usePlaintext(true));

        this.ip = ip;
        this.porta = porta;
    }

    private void conecta(){
        
    }

    public void RouteGuideClient(String host, int port) {
    }

    public void greet(String name) {
        System.out.println("Will try to greet " + name + " ...");

        // cria um builder
        AgendaOuterClass.Contato.Builder builder = AgendaOuterClass.Contato.newBuilder();
        // adicionando o que tiver de adicionar
        builder.setNome("Danielzin");
        builder.setTelefone("4499737489");

        // constroi seja lah o que ele estah construindo
        AgendaOuterClass.Contato contato = builder.build();

        // o que o servidor responder vai ser guardado aqui
        AgendaOuterClass.Contato response;


      /*try {
        response = blockingStub.sayHello(request);
      } catch (StatusRuntimeException e) {
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        return;
      }
      logger.info("Greeting: " + response.getMessage());
      try {
        response = blockingStub.sayHelloAgain(request);
      } catch (StatusRuntimeException e) {
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        return;
      }
      logger.info("Greeting: " + response.getMessage());*/
    }
}