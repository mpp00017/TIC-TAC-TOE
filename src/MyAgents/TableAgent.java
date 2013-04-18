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
 *
 * @author Manuel
 */
public class TableAgent extends Agent {
    AID playerAgents[];
    AID jugadores[] = new AID[2];
    TicTacToeFrame myGUI;
    boolean finish = false;
    String lastMov = "";
    int player = 0;
    String movimientos[] = new String[9];
    String movVictoria[] = new String[3];
    String poppup;
    int ganador;
    int nmov=0;
    
    @Override
    protected void setup(){
        
        myGUI = new TicTacToeFrame(this);
        myGUI.setVisible(true);
        
        System.out.println("Table Agent: "+this.getLocalName()+" is running.");
        
        
        //Search Player Agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            playerAgents = new AID[result.length];
            for(int i=0; i < result.length;i++){
                playerAgents[i] = result[i].getName();
            }
        }catch(FIPAException fe){}
        
        
         ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
         msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
         msg.setLanguage("English");
         msg.setContent("Would you like to play?");
         
        for(int i=0;i<playerAgents.length;i++){
            msg.addReceiver(playerAgents[i]);
        }
        
        this.addBehaviour(new CreateGame(this,msg));
        
        MessageTemplate mt = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_QUERY);
        this.addBehaviour(new PlayerResponder(this,mt));
        
    }
    
    private class PlayerResponder extends AchieveREResponder{
        public PlayerResponder(Agent agent, MessageTemplate mt){
            super(agent,mt);
        }
        
        @Override
        protected ACLMessage handleRequest (ACLMessage request){
            ACLMessage agree = request.createReply();
            agree.setPerformative(ACLMessage.AGREE);
            return agree;
        }
        
        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            System.out.println("PRUEBA");
            System.out.println(jugadores[0]);
            System.out.println(jugadores[1]);
            if(request.getSender().equals(jugadores[0]) || request.getSender().equals(jugadores[1])){
                inform.setContent("YES");
                System.out.println("A jugar!!");
            }else {
                inform.setContent("NO");
            }
            
            return inform;
            
        }
        
    }
    
    private class CreateGame extends ProposeInitiator{
        
        private int vacantes = 2;
        
        CreateGame(Agent a, ACLMessage message){
            super(a,message);
        }
        
        /*@Override
        protected void handleAllResponses(java.util.Vector responses){
            if(responses.size()<=1)
        }*/
                
        @Override
        protected void handleAcceptProposal(ACLMessage accepted) {
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
 class MoveRequest extends AchieveREInitiator{
        private boolean termina=true;
     
     MoveRequest(Agent a, ACLMessage msg){
         super(a,msg);
     }
     
     @Override
     protected void handleRefuse(ACLMessage agree)
       {
           finish = true;
           myGUI.popPupMessage(jugadores[(player+1)%2]+" surrenders!");
       }
        
     @Override
     protected void handleAllResponses(java.util.Vector responses){
         
         if(responses.isEmpty()){
            finish = true;
            myGUI.popPupMessage(jugadores[(player+1)%2]+" wins!");
         }
         
     }
     
     @Override
     protected void handleInform(ACLMessage msg){
         
         System.out.println(msg.getSender()+": "+msg.getContent());
                if(msg.getContent().length() == 1){
                    lastMov = msg.getContent();
                    movimientos[nmov]=lastMov;
                    nmov++;
                    
                    if(nmov==9) {
                        finish = true;
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
                
                
                
                if(player==1) {
                        player=0;
                    }
                    else {
                        player=1;
                    }
                
                if(finish == false){
                    msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    msg.setLanguage("English");
                    msg.setContent(lastMov);
                    msg.addReceiver(jugadores[player]);
                    myAgent.addBehaviour(new MoveRequest(myAgent, msg));
                }else if(msg.getContent().length()==4 || (msg.getContent().length()<=4 &&nmov==9)){
                    try {
                        myGUI.setInterface(movimientos,movVictoria,ganador,poppup,nmov,jugadores[0].getLocalName(),jugadores[1].getLocalName());
                    } catch (InterruptedException ex) {
                    Logger.getLogger(TableAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
         
     }
     
    }
}