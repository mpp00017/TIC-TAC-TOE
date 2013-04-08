/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
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
    protected AID[] getJugadores(){return jugadores;}
    boolean finish = false;
    String lastMov = "";
    int player = 0;
    boolean respuesta=false;
    int movimientos=0;
    
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
    }
    
    private class StartGame extends Behaviour{
        StartGame(AID[] jugadores){};
        
        @Override
        public void action() {
            myGUI.setTextPlayers(jugadores[0].getLocalName(), jugadores[1].getLocalName());
            
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            
            msg.addReceiver(jugadores[0]);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            msg.setContent("");
            
            addBehaviour(new ManejadorInitiator(this,msg));
            
        }

        @Override
        public boolean done() {
            return finish || movimientos==9;
        }
        
    }
    
    private class CreateGame extends ProposeInitiator{
        
        private int vacantes = 2;
        
        CreateGame(Agent a, ACLMessage message){
            super(a,message);
        }
                
        @Override
        protected void handleAcceptProposal(ACLMessage accepted) {
            if(vacantes == 2){
                jugadores[0] = accepted.getSender();
                vacantes--;
            }else if(vacantes == 1){
                jugadores[1] = accepted.getSender();
                vacantes--;
                myAgent.addBehaviour(new StartGame(jugadores));
            }
        }
        
    }
    
    class ManejadorInitiator extends AchieveREInitiator
    {
        public ManejadorInitiator(Agent a,ACLMessage msg) {
            super(a,msg);
        }
 
        @Override
        protected void handleAgree(ACLMessage agree)
        {
            respuesta=true;
        }
 
        @Override
        protected void handleRefuse(ACLMessage refuse)
        {
            finish=true;
            
            if(player==0)
                myGUI.setTextConsole1(refuse.getSender().getLocalName(), "Abandono");
            
            else
                myGUI.setTextConsole2(refuse.getSender().getLocalName(), "Abandono");
            
            myGUI.popPupMessage(refuse.getSender().getLocalName() + " abandona!");

            
        }
 
        /*
        @Override
        protected void handleNotUnderstood(ACLMessage notUnderstood)
        {
            System.out.println("Hospital " + notUnderstood.getSender().getName()
                    + " no puede entender el mensaje.");
        }*/
 
        @Override
        protected void handleInform(ACLMessage msg)
        {
            System.out.println(msg.getContent()+" "+msg.getSender().getLocalName());
            if(msg.getContent().length() == 1){
                lastMov = msg.getContent();
                myGUI.setMovement(lastMov, player);
                
                if(player==0)
                    myGUI.setTextConsole1(msg.getSender().getLocalName(), lastMov);
                
                else
                    myGUI.setTextConsole2(msg.getSender().getLocalName(), lastMov);
                
                movimientos++;

                if(movimientos==9)
                    myGUI.popPupMessage("DRAW");

                if(player==1)
                    player=0;
                else
                    player=1;

            }else if(msg.getContent().length() == 4){
                //ha ganado
                char[] lastMovChar;
                lastMovChar=msg.getContent().toCharArray();
                lastMov="";
                lastMov=lastMov+lastMovChar[0];

                myGUI.setMovement(lastMov, player);
                if(player==0)
                    myGUI.setTextConsole1(msg.getSender().getLocalName(), lastMov);
                else 
                    myGUI.setTextConsole2(msg.getSender().getLocalName(), lastMov);

                movimientos++;
                myGUI.setMovementV(String.valueOf(lastMovChar[1]), player);
                myGUI.setMovementV(String.valueOf(lastMovChar[2]), player);
                myGUI.setMovementV(String.valueOf(lastMovChar[3]), player);
                myGUI.popPupMessage(msg.getSender().getLocalName()+ " wins!");
                finish = true;
            }
        }
        
        /*
        @Override
        protected void handleFailure(ACLMessage fallo)
        {
            if (fallo.getSender().equals(myAgent.getAMS())) {
                System.out.println("Alguna de los hospitales no existe");
            }
            else
            {
                System.out.println("Fallo en el hospital " + fallo.getSender().getName()
                        + ": " + fallo.getContent().substring(1, fallo.getContent().length()-1));
            }
        }
        */
    }
}