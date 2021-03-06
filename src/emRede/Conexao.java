/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emRede;


import interfaces.Interface;
import java.io.DataInputStream;
import java.net.UnknownHostException;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

// Endereco.newBuilder();

/**
 *
 * @author venturini
 */
public class Conexao {

    private String nick;
    private String ip;
    private int porta;

    private String ipGrupo;
    private int portaGrupo;
    private int id;     // id do jogo no servidor

    // objetos para trocar objetos e mensagens em rede
    MulticastSocket multcastSocket;
    InetAddress group;
    Socket socket;

    // protocolo de troca de mensagem com o servidor
    String assistir = "@ASSISTIR";  // + id do jogo
    String fimJogo = "@FIMJOGO";    // + id jogo
    String novoJogo = "@NOVO";

    // troca de mensagem entre os clientes
    String revela = "@REVELA";
    String comeca = "@NEW";

    Interface inte;

    private static HashMap<javax.swing.JButton, String> botoesNomes = new HashMap<>();

    public Conexao(String ip, int porta, String nick, Interface inte){
        this.porta = porta;
        this.inte = inte;
        this.ip = ip;
    }

    private void conecta() throws UnknownHostException, IOException {
        System.out.println("Criando objetos de socket, leitura e escrita");
        socket = new Socket(InetAddress.getByName(ip), porta);
    }

    private void desconecta() throws IOException{
        socket.close();
    }

    // neste endereco sera o MultcastSocket do jogo
    private void recebeEndereco(String stringParaServidor) throws IOException, ClassNotFoundException{

        System.out.println("Enviado a string");
        // cria uma mensagem para o servidor
        MemoryGame.Conecta mensagem = criaMensagem(stringParaServidor);
        System.out.println("Crou a mensagem, agora vamos enviar");
        // escreve o objeto para o servidor
        mensagem.writeTo(socket.getOutputStream());

        DataInputStream recebe = new DataInputStream(socket.getInputStream());

        // recebe o compromisso do servidor e faz o Parse
        MemoryGame.Endereco endereco = MemoryGame.Endereco.parseFrom(recebe);
        System.out.println("Endereco: " + endereco.getEndereco() + ". Id: " + endereco.getId() + ". Porta: " + endereco.getPorta());

        ipGrupo = endereco.getEndereco();
        portaGrupo = endereco.getPorta();
        id = endereco.getId();
    }

    // cria um socket multcast neste endereco e se conecta
    private void conectaMultcast() throws UnknownHostException, IOException{

        // cria, se ja nao houver, e conecta
        group = InetAddress.getByName(ipGrupo);
        multcastSocket = new MulticastSocket(portaGrupo);
        multcastSocket.joinGroup(group);
    }

    // pode ter um cliente esperando para comecar o jogo, entao envia uma mensagem avisando
    public void enviaNoGrupo(String mensagem) throws IOException{

        byte[] mensagemBytes = mensagem.getBytes();
        DatagramPacket messageOut = new DatagramPacket(mensagemBytes, mensagemBytes.length, group, portaGrupo);
	/* envia o datagrama como multicast */
	multcastSocket.send(messageOut);
    }

    public int Assistir(int id){
        return returnIdEmbaralhamento(assistir + " " + Integer.toString(id));
    }

    public int Jogar(){
        return returnIdEmbaralhamento(novoJogo);
    }

    public void FimJogo(){
        envia(fimJogo + " " + inte.getSeed());
    }

    public void resolvido(String botao){
        try {
            // so vai avisar o servidor o ultimo que jogou
            if(!inte.getVez() || inte.assistindo()){
                return;
            }

            conecta();
            criaResolvido(botao).writeTo(socket.getOutputStream());
            desconecta();
        } catch (Exception ex) {
            System.out.println("Erro ao enviar: " + ex);
        }
    }

    private void envia(String mensagem){
        try {
            // so vai avisar o servidor o ultimo que jogou
            if(!inte.getVez() || inte.assistindo()){
                return;
            }

            conecta();
            // '@FIMJOGO IDJOGO'
            criaMensagem(mensagem).writeTo(socket.getOutputStream());
            desconecta();
        } catch (Exception ex) {
            System.out.println("Erro ao enviar: " + ex);
        }
    }

    // string complemento do comando, por exemplo, para assistir precisamos do @ASSISTIR + id
    // entao o Id sera este complemento
    public int returnIdEmbaralhamento(String stringParaServidor){
        try{
            conecta();
            recebeEndereco(stringParaServidor);
            desconecta();
            conectaMultcast();

            // se eu nao estiver assistindo
            // diz que tem um jogador pronto
            if(!inte.assistindo()){
                // @NEW + nick quer dizer que o jogador esta pronto
                enviaNoGrupo("@USER " + inte.getNick() + "@");
            }

            // comeca a ouvir as mensagens no grupo
            ouve();
        } catch (Exception ex) {
            System.out.println("Erro ao inicializar o jogo: " + ex);
            // -1 quer dizer que o jogo nao foi iniciado
            id = -1;
        }

        // semente do embaralhamento
        return id;
    }


    public MemoryGame.Conecta criaMensagem(String msg) {
        System.out.println("Will try to greet " + msg + " ...");

        MemoryGame.Conecta.Builder builder = MemoryGame.Conecta.newBuilder();
        builder.setMensagem(msg);
        builder.setId(-1);

        MemoryGame.Conecta conecta = builder.build();
        return conecta;
    }

    public MemoryGame.Resolvido criaResolvido(String botao) {
        System.out.println("Will try to greet " + botao + " ...");

        MemoryGame.Resolvido.Builder builder = MemoryGame.Resolvido.newBuilder();
        builder.setBotao(botao);
        builder.setIdJogo(inte.getSeed());

        MemoryGame.Resolvido resolvido = builder.build();
        return resolvido;
    }

    public void getResolvidos(){
        LinkedList<MemoryGame.Resolvido> resolvidos = new LinkedList<>();

        try {
            conecta();
            criaMensagem("@GETREVELADOS " + Integer.toString(inte.getSeed())).writeTo(socket.getOutputStream());
            DataInputStream recebe = new DataInputStream(socket.getInputStream());

            while(true){

                // sempre vindo do servidor sera de 15 bytes
                byte[] buffer = new byte[15];
                recebe.read(buffer);
                MemoryGame.Resolvido resolvido = MemoryGame.Resolvido.parseFrom(buffer);
                resolvidos.add(resolvido);
            }

        // vai gerar uma excessao quando nao houver mais nada para receber
        } catch (Exception ex) {
            // revela todos
            inte.apenasRevela(resolvidos);
        }
    }

    // fica ouvindo
    private void ouve(){
        new Thread(){
            @Override
            public void run(){
                while(true){
                    try{
                        byte[] buffer = new byte[1024];
                        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                        multcastSocket.receive(messageIn);

                        processaMensagem(new String(buffer));
                    } catch (Exception ex) {
                        System.out.println("Erro na thread ouve multcast");
                    }
                }
            }

            private String pegaDado(String mensagem, int indexEspaco){
                int indexFimBotao = mensagem.indexOf("@", indexEspaco+1);
                String botaoClicado = mensagem.substring(indexEspaco+1, indexFimBotao);
                System.out.println("Foi clicado o botao: " + botaoClicado);

                return botaoClicado;
            }

            private void processaMensagem(String mensagem){
                int indexEspaco = mensagem.indexOf(" ");
                String comando = mensagem.substring(0, indexEspaco);
                System.out.println("Chegou o comando: " + comando);
                String nomeUsuario;

                switch(comando){

                    case "@REVELA":
                        // se foi eu mesmo que cliquei
                        if(inte.getVez()){
                            return;
                        }

                        String botaoClicado = pegaDado(mensagem, indexEspaco);

                        inte.revelaBotao(botaoClicado);
                        break;

                    case "@USER":
                        nomeUsuario = pegaDado(mensagem, indexEspaco);
                        // se for o meu pacote
                        if(nomeUsuario.equals(inte.getNick())){
                            return;
                        }

                        try{
                            // nao sou eu
                            // entao eu comeco
                            inte.alteraSegundoUsuario(nomeUsuario);
                            inte.trocaVez();
                            inte.trocaStatus("Aguardando adversario entrar no jogo.");
                            // envio o meu nick
                            enviaNoGrupo("@USERR " + inte.getNick() + "@");
                        } catch (Exception ex) {
                            System.out.println("Erro ao processar nome do segundo usuario na classe conexao: " + ex);
                        }

                        break;

                    case "@USERR":
                        nomeUsuario = pegaDado(mensagem, indexEspaco);
                        // se for o meu pacote
                        if(nomeUsuario.equals(inte.getNick())){
                            return;
                        }

                        inte.alteraSegundoUsuario(nomeUsuario);
                        inte.trocaStatus("Vez do adversario.");
                    // se nao for um comando eh uma mensagem normal
                    default:
                        inte.areaBatePapo.append(mensagem + "\n");
                }
            }
        }.start();
    }
}
