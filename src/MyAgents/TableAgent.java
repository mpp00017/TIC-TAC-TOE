/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ProposeInitiator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase de agente tablero para Tic-Tac-Toe
 * 
 * @author Manuel Pancorbo Pestaña, Juan Cazalla Estrella
 */
public class TableAgent extends Agent {
    AID playerAgents[];
    AID jugadores[];
    TicTacToeFrame myGUI;
    boolean finish;
    String lastMov;
    int player;
    String movimientos[];
    String movVictoria[];
    String poppup;
    int ganador;
    int nmov;
    int vacantes;
    
    /**
     * Iniciación del agente
     */
    @Override
    protected void setup(){
        //Inicialización de los atributos de clase
        jugadores = new AID[2];
        finish = false;
        lastMov = "";
        player = 0;
        movimientos = new String[9];
        movVictoria = new String[3];
        nmov = 0;
        vacantes = 2;
        
        System.out.println("Table Agent: "+this.getLocalName()+" is running.");
        
        //Búsqueda de los agentes jugadores
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            playerAgents = new AID[result.length];
            for(int i=0; i < result.length;i++)
                playerAgents[i] = result[i].getName();
        }catch(FIPAException fe){}
        
        myGUI = new TicTacToeFrame(this);
        
        if(playerAgents.length>=2){
            System.out.println(this.getLocalName() + ": COMIENZA LA PARTIDA!");
            
            myGUI.setVisible(true);
            
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            msg.setLanguage("English");
            msg.setContent("Would you like to play?");

            for(int i=0;i<playerAgents.length;i++)
                msg.addReceiver(playerAgents[i]);

            this.addBehaviour(new CreateGame(this,msg));

            MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
            this.addBehaviour(new PlayerResponder(this,mt));
        
        }else{
            myGUI.popPupMessage("No hay suficientes jugadores");
            this.doDelete();
        }
    }
    
    /**
     * Tarea que contestará las consultas de los jugadores
     */
    private class PlayerResponder extends AchieveREResponder{
        
        /**
         * Constructor de la tarea PlayerResponder
         * 
         * @param agent agente que lanza la tarea
         * @param mt plantilla de mensaje que aceptará
         */
        public PlayerResponder(Agent agent, MessageTemplate mt){
            super(agent,mt);
        }
        
        /**
         * Manejador de consultas de los jugadores
         * 
         * @param request consulta recibida
         * @return 
         */
        @Override
        protected ACLMessage handleRequest (ACLMessage request){
            ACLMessage agree = request.createReply();
            agree.setPerformative(ACLMessage.AGREE);
            return agree;
        }
        
        /**
         * Método que construye la respuesta a la consulta recibida
         * 
         * @param request consulta recibida
         * @param response
         * @return inform mensaje de respuesta a la consulta
         */
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
            
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            
            if(request.getSender().compareTo(jugadores[0])==0 || request.getSender().compareTo(jugadores[1])==0)
                inform.setContent("YES");
            else
                inform.setContent("NO");
            
            return inform;
        }
    }
    
    /**
     * Tarea que implementa el protocolo CFP para seleccionar los jugadores 
     * que jugarán la partida
     */
    private class CreateGame extends ProposeInitiator{
        
        /**
         * Constructor de la clase CreateGame
         * 
         * @param a agente que lanza la tarea
         * @param message mensaje que se enviará para iniciar el protocolo
         */
        CreateGame(Agent a, ACLMessage message){
            super(a,message);
        }
        
        /**
         * Este método se invocará una vez se hayan recibido todas las 
         * respuestas esperadas o bien cuando finalice un tiempo determinado 
         * previmente establecido. Si hay menos de 2 respuestas no se tomará 
         * ninguna pues no se podrá empezar partida alguna.
         * 
         * @param responses vector con todas las respuestas recibidas
         */
        @Override
        protected void handleAllResponses(java.util.Vector responses){
            if(responses.size()<=1) {
                jugadores = new AID[2];
                vacantes = 2;
            }         
        }
        
        /**
         * Método que maneja los mensajes de aceptación
         * 
         * @param accepted Mensaje de aceptación
         */
        @Override
        protected void handleAcceptProposal(ACLMessage accepted){
            if(vacantes == 2){
                jugadores[0] = accepted.getSender();
                vacantes--;
            }else if(vacantes == 1){
                jugadores[1] = accepted.getSender();
                vacantes--;
                myGUI.setTextPlayers(jugadores[0].getLocalName(), jugadores[1].getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg.setLanguage("English");
                msg.setContent(lastMov);
                msg.addReceiver(jugadores[player]);
                myAgent.addBehaviour(new MoveRequest(myAgent, msg));
            }
        }
    }
    
    /**
     * Tarea que implementa el protocolo de comunicación CFP que mientras que 
     * la partida no haya terminado manda peticiones de movimientos 
     * alternativamente a los jugadores
     */
    class MoveRequest extends AchieveREInitiator{
        
        /**
         * Constructor de MoveRequest
         * 
         * @param a agente que lanza la tarea
         * @param msg mensaje que se mandará como inicio del protocolo de 
         * comunicación CFP
         */
        MoveRequest(Agent a, ACLMessage msg){
            super(a,msg);
        }

        /**
         * Manejador de mensajes de rechazo.
         * Cuando un jugador rechaza el mensaje se considera que se ha rendido
         * 
         * @param agree Mensaje de rechazo
         */
        @Override
        protected void handleRefuse(ACLMessage agree){
              finish = true;
              myGUI.popPupMessage(jugadores[(player+1)%2]+" surrenders!");
          }

        /**
         * Manejador que se lanza cuando todas las respuestas se han recibido o 
         * cuando ha expirado el quantum de tiempo. 
         * En caso de no recibirse ninguna respuesta dentro del quantum, se da 
         * la partida por ganada al jugador contrario
         * 
         * @param responses Vector con las respuestas
         */
        @Override
        protected void handleAllResponses(java.util.Vector responses){
            if(responses.isEmpty()){
               finish = true;
               myGUI.popPupMessage(jugadores[(player+1)%2]+" wins!");
            }
        }

        /**
         * Manejador de mensajes inform.
         * En este método, el tablero controla la partida. Mientras la partida
         * no esté acabada solicita movimientos a los jugadores. Cuando acaba
         * la partida llamará a un método de la interfaz para representarla
         * 
         * @param msg Mensaje inform recibido
         */
        @Override
        protected void handleInform(ACLMessage msg){
            boolean empate=false;
            System.out.println(msg.getSender().getLocalName()+": "+msg.getContent());
            if(msg.getContent().length() == 1){
                lastMov = msg.getContent();
                movimientos[nmov]=lastMov;
                nmov++;

                if(nmov==9) {
                    finish = true;
                    empate=true;
                    poppup="DRAW";

                    msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    msg.setLanguage("English");
                    msg.setContent("Empate");
                    msg.addReceiver(jugadores[0]);
                    msg.addReceiver(jugadores[1]);
                    myAgent.addBehaviour(new MoveRequest(myAgent, msg));
                }
            }else if(msg.getContent().length() == 4){
                char[] lastMovChar;
                lastMovChar=msg.getContent().toCharArray();
                lastMov="";
                lastMov=lastMov+lastMovChar[0];

                movimientos[nmov]=lastMov;

                nmov++;
                ganador = player;
                movVictoria[0]=String.valueOf(lastMovChar[1]);
                movVictoria[1]=String.valueOf(lastMovChar[2]);
                movVictoria[2]=String.valueOf(lastMovChar[3]);
                poppup= msg.getSender().getLocalName()+ " wins!";
                finish = true;

                lastMov=msg.getContent();

                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg.setLanguage("English");
                msg.setContent(lastMov);
                msg.addReceiver(jugadores[(player+1)%2]);
                myAgent.addBehaviour(new MoveRequest(myAgent, msg));
            } 

            if(finish == false){
                if(player==1)
                    player=0;
                else
                    player=1;

                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg.setLanguage("English");
                msg.setContent(lastMov);
                msg.addReceiver(jugadores[player]);
                myAgent.addBehaviour(new MoveRequest(myAgent, msg));
            }else if(msg.getContent().length()==4 || empate)
                try {
                    myGUI.setInterface(movimientos,movVictoria,ganador,poppup,nmov,jugadores[0].getLocalName(),jugadores[1].getLocalName());
                } catch (InterruptedException ex) {
                    Logger.getLogger(TableAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
}